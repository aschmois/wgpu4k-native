
import com.google.gson.JsonParser
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

allprojects {

	repositories {
		mavenLocal()
		google()
		mavenCentral()
	}

	group = "cool.avocado"
	version = System.getenv("VERSION")?.takeIf { it.isNotBlank() } ?: "v29.0.0-SNAPSHOT"
}

val publicationVerificationRepository = layout.buildDirectory
	.dir("publication-verification/repository")

val cleanPublicationVerificationRepository by tasks.registering(Delete::class) {
	delete(publicationVerificationRepository)
}

val verifyPublicationMetadata by tasks.registering {
	group = "verification"
	dependsOn(
		":kffi:publishAllPublicationsToPublicationVerificationRepository",
		":wgpu4k-native:publishAllPublicationsToPublicationVerificationRepository",
	)
	doLast {
		val repository = publicationVerificationRepository.get().asFile
		val coordinatePath = project.group.toString().replace('.', '/')
		val publishedVersion = project.version.toString()
		fun uniquePublishedFile(artifact: String, extension: String): File {
			val versionDirectory = repository.resolve("$coordinatePath/$artifact/$publishedVersion")
			val candidates = versionDirectory.listFiles { file ->
				file.isFile &&
					file.name.startsWith("$artifact-") &&
					file.name.endsWith(".$extension")
			}?.toList().orEmpty()
			require(candidates.size == 1) {
				"Expected exactly one $artifact .$extension file in $versionDirectory, " +
					"but found ${candidates.map { it.name }.sorted()}"
			}
			return candidates.single()
		}

		uniquePublishedFile("kffi-jvm", "module")
		val metadataFile = uniquePublishedFile("wgpu4k-native-jvm", "module")
		val root = JsonParser.parseString(metadataFile.readText()).asJsonObject
		val dependencies = root.getAsJsonArray("variants")
			.flatMap { variant ->
				variant.asJsonObject.getAsJsonArray("dependencies")?.map { it.asJsonObject }.orEmpty()
			}
		val kffiDependencies = dependencies.filter { candidate ->
			candidate["group"].asString == project.group.toString() &&
				candidate["module"].asString == "kffi"
		}
		require(kffiDependencies.isNotEmpty()) {
			"Expected wgpu4k-native-jvm metadata to depend on ${project.group}:kffi"
		}
		val publishedDependencyVersions = kffiDependencies.map { dependency ->
			dependency.getAsJsonObject("version").let { version ->
				version.get("requires")?.asString ?: version.get("strictly")?.asString
			}
		}
		require(publishedDependencyVersions.all { it == publishedVersion }) {
			"Expected every wgpu4k-native-jvm metadata edge to ${project.group}:kffi " +
				"to use $publishedVersion, but found $publishedDependencyVersions"
		}

		val pomFile = uniquePublishedFile("wgpu4k-native-jvm", "pom")
		val pom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pomFile)
		val pomDependencies = pom.getElementsByTagName("dependency")
		val kffiJvmDependencies = (0 until pomDependencies.length)
			.map { pomDependencies.item(it) as Element }
			.filter { dependency ->
				dependency.getElementsByTagName("groupId").item(0).textContent == project.group.toString() &&
					dependency.getElementsByTagName("artifactId").item(0).textContent == "kffi-jvm"
			}
		require(kffiJvmDependencies.size == 1) {
			"Expected exactly one ${project.group}:kffi-jvm dependency in $pomFile, " +
				"but found ${kffiJvmDependencies.size}"
		}
		val pomDependencyVersion = kffiJvmDependencies.single()
			.getElementsByTagName("version")
			.item(0)
			.textContent
		require(pomDependencyVersion == publishedVersion) {
			"Expected wgpu4k-native-jvm POM to depend on kffi-jvm $publishedVersion, " +
				"but found $pomDependencyVersion"
		}
	}
}

tasks.register<GradleBuild>("verifyPublishedConsumer") {
	group = "verification"
	description = "Publishes KFFI and wgpu4k-native locally, then compiles an isolated consumer."
	dependsOn(verifyPublicationMetadata)
	dir = file("gradle/publication-consumer")
	tasks = listOf("clean", "compileJava")
	startParameter.projectProperties = mapOf(
		"verificationRepository" to publicationVerificationRepository.get().asFile.toURI().toString(),
		"wgpu4kVersion" to project.version.toString(),
	)
}

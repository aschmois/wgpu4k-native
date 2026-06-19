import publish.PublishingType
import publish.centralPortalPublish

plugins {
  `maven-publish`
  signing
  id("org.jetbrains.dokka")
}

val libraryDescription = "wgpu4k kotlin native binding."

val signingKey = System.getenv("JRELEASER_GPG_SECRET_KEY")
val signingPassword = System.getenv("JRELEASER_GPG_PASSPHRASE")

if (!isSnapshot()) {
  signing {
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
  }
}

project.centralPortalPublish {
  username = System.getenv("JRELEASER_MAVENCENTRAL_USERNAME")
  password = System.getenv("JRELEASER_MAVENCENTRAL_PASSWORD")
  publishingType = PublishingType.USER_MANAGED
  url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
}

val javadocJar by
  tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    // Dokka V1 `dokkaHtml` is incompatible with the V2 mode this project enables, so ship a
    // manifest-only javadoc jar to satisfy Maven Central's "must include javadoc" requirement.
    // A proper Dokka V2 migration (`dokkaGeneratePublicationHtml`) can restore rendered docs later.
  }

publishing {
  publications {
    withType<MavenPublication> {
      artifact(javadocJar)
      pom {
        name.set(project.name)
        description.set(libraryDescription)
        url.set("https://github.com/aschmois/wgpu4k-native")
        inceptionYear.set("2024")
        licenses {
          license {
            name.set("MIT")
            url.set("https://opensource.org/license/MIT")
          }
        }
        developers {
          developer {
            id.set("amommers")
            name.set("Alexandre Mommers")
          }
          developer {
            id.set("aschmois")
            name.set("Andres Schmois")
          }
        }
        scm {
          connection.set("scm:git:https://github.com/aschmois/wgpu4k-native.git")
          developerConnection.set("scm:git:https://github.com/aschmois/wgpu4k-native.git")
          url.set("https://github.com/aschmois/wgpu4k-native")
        }
      }
    }
  }

  repositories {
    maven {
      if (isSnapshot()) {
        logger.info("publishing is configure as snapshot")
        name = "GitLab"
        url = uri("https://gitlab.com/api/v4/projects/25805863/packages/maven")
        credentials(HttpHeaderCredentials::class) {
          name = "Authorization"
          value = "Bearer ${System.getenv("GITLAB_TOKEN")}"
        }
        authentication { create<HttpHeaderAuthentication>("header") }
      } else {
        name = "Local"
        logger.info("publishing is configure as release")
        url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        logger.info("publishing path is ${url.path}")
      }
    }
  }
}

if (!isSnapshot()) {
  val signingTasks = tasks.withType<Sign>()
  tasks.withType<AbstractPublishToMaven>().configureEach { dependsOn(signingTasks) }
}

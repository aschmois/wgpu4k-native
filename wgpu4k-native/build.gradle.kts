import com.android.build.gradle.tasks.MergeSourceSetFolders
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import java.nio.file.Files

plugins {
    `kotlin-multiplatform`
    publish
    com.android.library
    alias(libs.plugins.kotest)
    alias(libs.plugins.ksp)
}

val buildNativeResourcesDirectory = project.file("build").resolve("native")
val jvmLibResourcesDirectory = project.file("build").resolve("generated").resolve("resources")

kotlin {

    val nativeTargets = listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosArm64(),
        macosX64(),
        linuxArm64(),
        linuxX64(),
        mingwX64(),
        androidNativeArm64(),
        androidNativeX64(),
    )

    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }

        android {
            namespace = "io.ygdrasil.wgpu"
            compileSdk = 36

            defaultConfig {
                minSdk = 28
            }

            sourceSets {
                getByName("main") {
                    jniLibs.srcDirs(jniBasePath().absolutePath)
                }

            }
        }
        publishLibraryVariants("release", "debug")
    }

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_24
        }
    }

    nativeTargets.forEach { target ->
        val main by target.compilations.getting {
            cinterops.create("webgpu") {
                packageName = "webgpu.native"
                header(buildNativeResourcesDirectory.resolve("wgpu.h"))
            }
        }
    }

    compilerOptions {
        // Fix generator before reactive this
        //allWarningsAsErrors = true
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":kffi"))
            }
        }

        androidMain {
            dependencies {
                val jna = libs.jna.get()
                api("${jna.module.group}:${jna.module.name}:${jna.versionConstraint}:@aar")
            }
        }

        commonTest {
            dependencies {
                implementation(libs.bundles.kotest)
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        jvmMain {
            sourceSets {
                resources.srcDirs(jvmLibResourcesDirectory.absolutePath)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.junit5)
                implementation(libs.kotlin.reflect)
            }
        }

        androidUnitTest {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }
    }
}

configureDownloadTasks {
    baseUrl = "${project.properties["wgpu.base.url"]}${libs.versions.wgpu.get()}/"

    /*** Macos ***/
    download("wgpu-macos-aarch64-release.zip") {
        extract("wgpu-native-meta/webgpu.yml", buildNativeResourcesDirectory.resolve("webgpu.yml")).doLast {
            Files.move(
                buildNativeResourcesDirectory.resolve("wgpu-native-meta").resolve("webgpu.yml").toPath(),
                buildNativeResourcesDirectory.resolve("webgpu.yml").toPath()
            )
            buildNativeResourcesDirectory.resolve("wgpu-native-meta").deleteRecursively()
        }
        extract("include/webgpu/webgpu.h", buildNativeResourcesDirectory.resolve("webgpu.h")).doLast {
            Files.move(
                buildNativeResourcesDirectory.resolve("include").resolve("webgpu").resolve("webgpu.h").toPath(),
                buildNativeResourcesDirectory.resolve("webgpu.h").toPath()
            )
            buildNativeResourcesDirectory.resolve("include").deleteRecursively()
        }
        extract("include/webgpu/wgpu.h", buildNativeResourcesDirectory.resolve("wgpu.h")).doLast {
            Files.move(
                buildNativeResourcesDirectory.resolve("include").resolve("webgpu").resolve("wgpu.h").toPath(),
                buildNativeResourcesDirectory.resolve("wgpu.h").toPath()
            )
            buildNativeResourcesDirectory.resolve("include").deleteRecursively()
        }
        extract("lib/libwgpu_native.a", buildNativeResourcesDirectory.resolve("darwin-aarch64").resolve("libWGPU.a"))
        extract("lib/libwgpu_native.dylib", jvmLibResourcesDirectory.resolve("darwin-aarch64").resolve("libwgpu_native.dylib")).doLast {
            val basePath = jvmLibResourcesDirectory.resolve("darwin-aarch64")
            Files.move(
                basePath.resolve("lib").resolve("libwgpu_native.dylib").toPath(),
                basePath.resolve("libwgpu_native.dylib").toPath()
            )
            basePath.resolve("lib").deleteRecursively()
        }
    }
    download("wgpu-macos-x86_64-release.zip") {
        extract("lib/libwgpu_native.a", buildNativeResourcesDirectory.resolve("darwin-x64").resolve("libWGPU.a"))
        extract("lib/libwgpu_native.dylib", jvmLibResourcesDirectory.resolve("darwin-x86-64").resolve("libwgpu_native.dylib")).doLast {
            val basePath = jvmLibResourcesDirectory.resolve("darwin-x86-64")
            Files.move(
                basePath.resolve("lib").resolve("libwgpu_native.dylib").toPath(),
                basePath.resolve("libwgpu_native.dylib").toPath()
            )
            basePath.resolve("lib").deleteRecursively()
        }
    }

    /*** Windows ***/
    download("wgpu-windows-x86_64-gnu-release.zip") {
        extract("lib/libwgpu_native.a", buildNativeResourcesDirectory.resolve("windows-x64").resolve("wgpu.a"))
    }
    download("wgpu-windows-x86_64-msvc-release.zip") {
        extract("lib/wgpu_native.lib", buildNativeResourcesDirectory.resolve("windows-x64").resolve("wgpu.lib"))
        extract("lib/wgpu_native.dll", jvmLibResourcesDirectory.resolve("win32-x86-64").resolve("wgpu_native.dll")).doLast {
            val basePath = jvmLibResourcesDirectory.resolve("win32-x86-64")
            Files.move(
                basePath.resolve("lib").resolve("wgpu_native.dll").toPath(),
                basePath.resolve("wgpu_native.dll").toPath()
            )
            basePath.resolve("lib").deleteRecursively()
        }
    }

    /*** Linux ***/
    download("wgpu-linux-x86_64-release.zip") {
        extract("lib/libwgpu_native.a", buildNativeResourcesDirectory.resolve("linux-x64").resolve("libWGPU.a"))
        extract("lib/libwgpu_native.so", jvmLibResourcesDirectory.resolve("linux-x86-64").resolve("libwgpu_native.so")).doLast {
            val basePath = jvmLibResourcesDirectory.resolve("linux-x86-64")
            Files.move(
                basePath.resolve("lib").resolve("libwgpu_native.so").toPath(),
                basePath.resolve("libwgpu_native.so").toPath()
            )
            basePath.resolve("lib").deleteRecursively()
        }
    }
    download("wgpu-linux-aarch64-release.zip") {
        extract("lib/libwgpu_native.a", buildNativeResourcesDirectory.resolve("linux-aarch64").resolve("libWGPU.a"))
        extract("lib/libwgpu_native.so", jvmLibResourcesDirectory.resolve("linux-aarch64").resolve("libwgpu_native.so")).doLast {
            val basePath = jvmLibResourcesDirectory.resolve("linux-aarch64")
            Files.move(
                basePath.resolve("lib").resolve("libwgpu_native.so").toPath(),
                basePath.resolve("libwgpu_native.so").toPath()
            )
            basePath.resolve("lib").deleteRecursively()
        }
    }

    /*** iOS ***/
    download("wgpu-ios-x86_64-simulator-release.zip") {
        extract("lib/libwgpu_native.a", buildNativeResourcesDirectory.resolve("ios-simulator-x64").resolve("libWGPU.a"))
    }
    download("wgpu-ios-aarch64-simulator-release.zip") {
        extract("lib/libwgpu_native.a", buildNativeResourcesDirectory.resolve("ios-simulator-aarch64").resolve("libWGPU.a"))
    }
    download("wgpu-ios-aarch64-release.zip") {
        extract("lib/libwgpu_native.a", buildNativeResourcesDirectory.resolve("ios-aarch64").resolve("libWGPU.a"))
    }

    /*** Android ***/
    /**** Release ****/
    download("wgpu-android-x86_64-release.zip") {
        extract("lib/libwgpu_native.so", buildNativeResourcesDirectory.resolve("libs").resolve("x86_64").resolve("libwgpu4k.so")).doLast {
            Files.move(
                buildNativeResourcesDirectory.resolve("libs").resolve("x86_64").resolve("lib").resolve("libwgpu4k.so").toPath(),
                buildNativeResourcesDirectory.resolve("libs").resolve("x86_64").resolve("libwgpu4k.so").toPath()
            )
            buildNativeResourcesDirectory.resolve("libs").resolve("x86_64").resolve("lib").deleteRecursively()
        }
        extract("lib/libwgpu_native.a", buildNativeResourcesDirectory.resolve("android-x64").resolve("libWGPU.a"))
    }
    download("wgpu-android-aarch64-release.zip") {
        extract("lib/libwgpu_native.so", buildNativeResourcesDirectory.resolve("libs").resolve("arm64-v8a").resolve("libwgpu4k.so")).doLast {
            Files.move(
                buildNativeResourcesDirectory.resolve("libs").resolve("arm64-v8a").resolve("lib").resolve("libwgpu4k.so").toPath(),
                buildNativeResourcesDirectory.resolve("libs").resolve("arm64-v8a").resolve("libwgpu4k.so").toPath()
            )
            buildNativeResourcesDirectory.resolve("libs").resolve("arm64-v8a").resolve("lib").deleteRecursively()
        }
        extract("lib/libwgpu_native.a", buildNativeResourcesDirectory.resolve("android-aarch64").resolve("libWGPU.a"))
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

fun jniBasePath() = buildNativeResourcesDirectory.resolve("libs")

tasks.withType<Test> {
    failOnNoDiscoveredTests = false
}

tasks.named<Test>("jvmTest") {
    dependsOn("verifyJvmBootstrapBinding")
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStandardStreams = true
        events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.withType(MergeSourceSetFolders::class.java).configureEach {
    dependsOn("fetch-native-dependencies")
}
tasks.withType(CInteropProcess::class.java).configureEach {
    dependsOn("fetch-native-dependencies")
}

tasks.register<Copy>("copyDocsToRoot") {
    dependsOn("dokkaGfm", "dokkaHtml")
    from(project.layout.buildDirectory.dir("dokka"))
    into(rootDir.resolve("doc"))
}

tasks.register<Task>("generateDocs") {
    group = "documentation"
    description = "Generates the documentation in HTML and Markdown formats, then copies the files into the 'doc' folder."
    dependsOn("copyDocsToRoot")
}

val actualBindingGenerationHost = when (Platform.os) {
    Os.MacOs -> "macos"
    Os.Linux -> "linux"
    Os.Windows -> "windows"
}
val bindingGenerationHost = providers.gradleProperty("wgpu4k.bindingGeneration.hostForTest")
    .orNull
    ?.lowercase()
    ?: actualBindingGenerationHost
require(bindingGenerationHost in setOf("macos", "linux", "windows"))

val kextractDistribution = project(":kextract").layout.buildDirectory.dir("kextract")
val kextractLauncher = kextractDistribution.map { distribution ->
    distribution.file(if (bindingGenerationHost == "windows") "bin/kextract.bat" else "bin/kextract")
}
val generatedJvmBinding = project.file(
    "src/jvmMain/kotlin/io/ygdrasil/wgpu/wgpu_hJvm.kt",
)
val genericJvmLookupImport = "import io.ygdrasil.kffi.findOrThrow"
val generatedJvmBootstrapResolver = "KextractNativeBootstrap.resolve("
val jvmNativeResourceTasks = listOf(
    "unzip-libwgpu_native.dylib-from-wgpu-macos-aarch64-release.zip",
    "unzip-libwgpu_native.dylib-from-wgpu-macos-x86_64-release.zip",
    "unzip-wgpu_native.dll-from-wgpu-windows-x86_64-msvc-release.zip",
    "unzip-libwgpu_native.so-from-wgpu-linux-x86_64-release.zip",
    "unzip-libwgpu_native.so-from-wgpu-linux-aarch64-release.zip",
)
val jvmBindingNativeDependencyTasks = listOf(
    "unzip-webgpu.h-from-wgpu-macos-aarch64-release.zip",
    "unzip-wgpu.h-from-wgpu-macos-aarch64-release.zip",
) + jvmNativeResourceTasks

tasks.named("jvmProcessResources") {
    dependsOn(*jvmNativeResourceTasks.toTypedArray())
}
val generatedAndroidBindings = project.file(
    "src/androidMain/kotlin/io/ygdrasil/wgpu/wgpu_hAndroid.kt",
)

tasks.register<Exec>("generateBindingsFromHeader") {
    group = "generation"
    description = "Generates unified KMP bindings from webgpu.h using kextract CLI"
    dependsOn(":kextract:createKextractImage")
    dependsOn(*jvmBindingNativeDependencyTasks.toTypedArray())

    val callbackBindings = project(":wgpu4k-native-specs")
        .file("src/jvmMain/resources/callback-bindings.yml")
    val nativeHeader = project.file("build/native/wgpu.h")
    val webGpuHeader = project.file("build/native/webgpu.h")

    inputs.dir(kextractDistribution)
        .withPropertyName("kextractDistribution")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.property(
        "kextractDistributionPath",
        kextractDistribution.map { distribution ->
            distribution.asFile.toPath().toAbsolutePath().normalize().toString()
        },
    )
    inputs.file(kextractLauncher)
        .withPropertyName("kextractLauncher")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(callbackBindings).withPropertyName("callbackBindings")
    inputs.file(nativeHeader).withPropertyName("nativeHeader")
    inputs.file(webGpuHeader).withPropertyName("webGpuHeader")
    inputs.dir(jvmLibResourcesDirectory)
        .withPropertyName("jvmNativeResources")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dirs(
        project.file("src/commonMain/kotlin"),
        project.file("src/jvmMain/kotlin"),
        project.file("src/nativeMain/kotlin"),
        project.file("src/androidMain/kotlin"),
    )

    executable = kextractLauncher.get().asFile.absolutePath

    doFirst {
        require(bindingGenerationHost == actualBindingGenerationHost) {
            "wgpu4k.bindingGeneration.hostForTest is configuration-only; " +
                "cannot execute generateBindingsFromHeader for configured host '$bindingGenerationHost' " +
                "on actual host '$actualBindingGenerationHost'."
        }
    }

    val isMac = System.getProperty("os.name").contains("Mac", ignoreCase = true)
    val isLinux = System.getProperty("os.name").contains("Linux", ignoreCase = true)
    val clangArgs = mutableListOf<String>()
    if (isMac) {
        val sdkPath = providers.exec {
            commandLine("xcrun", "--show-sdk-path")
        }.standardOutput.asText.get().trim()
        clangArgs.addAll(
            listOf("-D", "__MATH_H__", "-A", "-ffreestanding", "-A", "-isysroot", "-A", sdkPath),
        )
    } else if (isLinux) {
        // Linux libclang searches /usr/include, leaking glibc/clang system types (__fsid_t,
        // max_align_t) into the bindings that macOS's -isysroot excludes. Parse -ffreestanding
        // + -nostdinc, re-adding only clang's own resource headers (stdint/stddef) so the wgpu
        // headers still resolve size_t/uint32_t, and predefine the guards for the couple of
        // types clang would still emit. The resource-include dir is host-specific (e.g. Kotlin/
        // Native's bundled LLVM: ~/.konan/dependencies/llvm-*/lib/clang/<v>/include), so it is
        // supplied via -Pwgpu4k.clangResourceDir=... or the WGPU4K_CLANG_RESOURCE_DIR env var.
        // A missing resource dir must not fail task CREATION. Gradle realizes this task whenever
        // it builds a task graph that reaches it (`build` -> `allTests`), so erroring here takes
        // the whole Linux build down even when nothing regenerates bindings. Only an actual run
        // of this task needs the dir, so the check belongs in the execution phase.
        val clangResourceInclude =
            providers.gradleProperty("wgpu4k.clangResourceDir")
                .orElse(providers.environmentVariable("WGPU4K_CLANG_RESOURCE_DIR"))
                .orNull
        doFirst {
            checkNotNull(clangResourceInclude) {
                "Linux binding generation needs clang's resource include dir; set " +
                    "-Pwgpu4k.clangResourceDir=<llvm>/lib/clang/<version>/include " +
                    "(or the WGPU4K_CLANG_RESOURCE_DIR env var)."
            }
        }
        if (clangResourceInclude != null) {
            clangArgs.addAll(
                listOf(
                    "-D", "__MATH_H__",
                    "-D", "__CLANG_MAX_ALIGN_T_DEFINED",
                    "-A", "-ffreestanding",
                    "-A", "-nostdinc",
                    "-A", "-isystem", "-A", clangResourceInclude,
                ),
            )
        }
    }

    args = listOf(
        "--multiplatform",
        "--target-package", "io.ygdrasil.wgpu",
        "--output", project.file("src").absolutePath,
        "--library", "wgpu4k",
        "--jvm-native-library", "wgpu_native",
        "--jvm-native-resources", jvmLibResourcesDirectory.absolutePath,
        "--callback-bindings",
        callbackBindings.absolutePath,
        "-D", "WGPU_SKIP_PROCS",
        nativeHeader.absolutePath,
    ) + clangArgs

    doLast {
        // kextract currently exposes struct pointer parameters as raw JNA pointers, so JNA cannot
        // auto-read output structures after the native call. Keep this downstream correction
        // deterministic until the generator emits the readbacks itself.
        fun replaceGeneratedFunction(
            source: String,
            generatedFunction: String,
            patchedFunction: String,
            functionName: String,
        ): String {
            val firstMatch = source.indexOf(generatedFunction)
            require(firstMatch >= 0) {
                "Could not apply the Android JNA output readback for $functionName: generated function changed."
            }
            require(firstMatch == source.lastIndexOf(generatedFunction)) {
                "Could not apply the Android JNA output readback for $functionName: generated function is ambiguous."
            }
            return source.replaceRange(
                firstMatch,
                firstMatch + generatedFunction.length,
                patchedFunction,
            )
        }

        val generatedSource = generatedAndroidBindings.readText()
        val withCapabilitiesReadback = replaceGeneratedFunction(
            generatedSource,
            """actual fun wgpuSurfaceGetCapabilities(surface: WGPUSurface?, adapter: WGPUAdapter?, capabilities: WGPUSurfaceCapabilities?): WGPUStatus {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSurfaceGetCapabilities(surface?.handler, adapter?.handler, capabilities?.handler)).toUInt()
}""",
            """actual fun wgpuSurfaceGetCapabilities(surface: WGPUSurface?, adapter: WGPUAdapter?, capabilities: WGPUSurfaceCapabilities?): WGPUStatus {
    val status = io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSurfaceGetCapabilities(surface?.handler, adapter?.handler, capabilities?.handler)
    when (capabilities) {
        is WGPUSurfaceCapabilities.ByReference -> capabilities.handle.read()
        is WGPUSurfaceCapabilities.ByValue -> capabilities.handle.read()
        null -> Unit
    }
    return status.toUInt()
}""",
            "wgpuSurfaceGetCapabilities",
        )
        val withSurfaceTextureReadback = replaceGeneratedFunction(
            withCapabilitiesReadback,
            """actual fun wgpuSurfaceGetCurrentTexture(surface: WGPUSurface?, surfaceTexture: WGPUSurfaceTexture?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSurfaceGetCurrentTexture(surface?.handler, surfaceTexture?.handler)
    return
}""",
            """actual fun wgpuSurfaceGetCurrentTexture(surface: WGPUSurface?, surfaceTexture: WGPUSurfaceTexture?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSurfaceGetCurrentTexture(surface?.handler, surfaceTexture?.handler)
    when (surfaceTexture) {
        is WGPUSurfaceTexture.ByReference -> surfaceTexture.handle.read()
        is WGPUSurfaceTexture.ByValue -> surfaceTexture.handle.read()
        null -> Unit
    }
    return
}""",
            "wgpuSurfaceGetCurrentTexture",
        )
        generatedAndroidBindings.writeText(withSurfaceTextureReadback.trimEnd() + "\n")
    }
}

tasks.register("verifyJvmBootstrapBinding") {
    group = "verification"
    inputs.file(generatedJvmBinding)
    doLast {
        val source = generatedJvmBinding.readText()
        require(source.lineSequence().count { it == genericJvmLookupImport } == 1) {
            "Expected exactly one kffi lookup import in $generatedJvmBinding"
        }
        require(generatedJvmBootstrapResolver in source) {
            "$generatedJvmBinding must route symbol lookup through the generated native bootstrap"
        }
    }
}

tasks.register("verifyBindingGenerationConfiguration") {
    group = "verification"
    description = "Verifies that binding generation has portable dependencies, inputs, and launcher configuration."

    doLast {
        val generationTask = tasks.named<Exec>("generateBindingsFromHeader").get()
        val directDependencies = generationTask.dependsOn
            .map { dependency ->
                when (dependency) {
                    is Task -> dependency.path
                    else -> dependency.toString().let { path ->
                        if (path.startsWith(":")) path else "${generationTask.project.path}:$path"
                    }
                }
            }
            .toSet()
        require(":kextract:createKextractImage" in directDependencies) {
            "generateBindingsFromHeader must depend directly on :kextract:createKextractImage; found $directDependencies"
        }
        val expectedNativeDependencies = jvmBindingNativeDependencyTasks
            .map { taskName -> ":wgpu4k-native:$taskName" }
            .toSet()
        require(expectedNativeDependencies.all { it in directDependencies }) {
            "generateBindingsFromHeader must depend on JVM/header native inputs; found $directDependencies"
        }
        require(":wgpu4k-native:fetch-native-dependencies" !in directDependencies) {
            "generateBindingsFromHeader must not download out-of-scope native targets"
        }
        val configuredArgs = generationTask.args.map(Any::toString)
        require(configuredArgs.windowed(2).any { it == listOf("--library", "wgpu4k") }) {
            "Android/platform generation must keep the wgpu4k library name; found $configuredArgs"
        }
        require(configuredArgs.windowed(2).any { it == listOf("--jvm-native-library", "wgpu_native") }) {
            "JVM generation must load wgpu_native; found $configuredArgs"
        }
        require(configuredArgs.windowed(2).any { it.firstOrNull() == "--jvm-native-resources" }) {
            "JVM generation must declare its native resource root; found $configuredArgs"
        }

        val verificationTask = tasks.named("verifyGeneratedBindingsClean").get()
        val verificationDependencies = verificationTask.taskDependencies
            .getDependencies(verificationTask)
            .map { it.path }
            .toSet()
        require(generationTask.path in verificationDependencies) {
            "verifyGeneratedBindingsClean must depend on ${generationTask.path}; found $verificationDependencies"
        }

        val expectedLauncherSuffix = if (bindingGenerationHost == "windows") {
            "bin/kextract.bat"
        } else {
            "bin/kextract"
        }
        val configuredExecutable = generationTask.executable.orEmpty().replace('\\', '/')
        require(configuredExecutable.endsWith(expectedLauncherSuffix)) {
            "Expected launcher suffix $expectedLauncherSuffix for $bindingGenerationHost; found $configuredExecutable"
        }

        val callbackBindings = project(":wgpu4k-native-specs")
            .file("src/jvmMain/resources/callback-bindings.yml")
            .absoluteFile
        val nativeHeader = project.file("build/native/wgpu.h").absoluteFile
        val webGpuHeader = project.file("build/native/webgpu.h").absoluteFile
        val declaredInputs = generationTask.inputs.files.files.map { it.absoluteFile }.toSet()
        val expectedDistributionPath = kextractDistribution.get().asFile
            .toPath()
            .toAbsolutePath()
            .normalize()
            .toString()
        val configuredDistributionPath = generationTask.inputs
            .properties["kextractDistributionPath"]
            ?.toString()
        require(configuredDistributionPath == expectedDistributionPath) {
            "Expected kextractDistributionPath $expectedDistributionPath; found $configuredDistributionPath"
        }
        val expectedDistributionFiles = kextractDistribution.get().asFile
            .walkTopDown()
            .filter { it.isFile }
            .map { it.absoluteFile }
            .toSet()
        require(expectedDistributionFiles.all { it in declaredInputs }) {
            "Kextract distribution files are not all declared inputs"
        }
        require(callbackBindings in declaredInputs) { "callback-bindings.yml is not a declared input" }
        require(nativeHeader in declaredInputs) { "wgpu.h is not a declared input" }
        require(webGpuHeader in declaredInputs) { "webgpu.h is not a declared input" }
        require(kextractLauncher.get().asFile.absoluteFile in declaredInputs) {
            "Kextract launcher is not a declared input"
        }
    }
}

tasks.register("verifyGeneratedBindingsClean") {
    group = "verification"
    description = "Verifies that generated WebGPU sources have no tracked or untracked changes."
    dependsOn("generateBindingsFromHeader")

    doLast {
        fun runGit(vararg arguments: String): Pair<Int, String> {
            val process = ProcessBuilder(listOf("git") + arguments)
                .directory(rootDir)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            return process.waitFor() to output
        }

        val (stagedDiffExitCode, stagedDiffOutput) = runGit(
            "diff",
            "--cached",
            "--exit-code",
            "--",
            "wgpu4k-native/src",
        )
        if (stagedDiffExitCode != 0) {
            throw GradleException("Staged generated WebGPU sources differ from HEAD:\n$stagedDiffOutput")
        }

        val (worktreeDiffExitCode, worktreeDiffOutput) = runGit(
            "diff",
            "--exit-code",
            "--",
            "wgpu4k-native/src",
        )
        if (worktreeDiffExitCode != 0) {
            throw GradleException("Generated WebGPU sources differ from the index:\n$worktreeDiffOutput")
        }

        val (untrackedExitCode, untrackedOutput) = runGit(
            "ls-files",
            "--others",
            "--exclude-standard",
            "--",
            "wgpu4k-native/src",
        )
        if (untrackedExitCode != 0) {
            throw GradleException("Could not inspect untracked generated WebGPU sources:\n$untrackedOutput")
        }
        if (untrackedOutput.isNotBlank()) {
            throw GradleException("Untracked generated WebGPU sources:\n$untrackedOutput")
        }
    }
}

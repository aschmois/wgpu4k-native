import com.android.build.gradle.tasks.MergeSourceSetFolders
import com.android.build.api.attributes.BuildTypeAttr
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
            jvmTarget = JvmTarget.fromTarget("25")
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
                // kffi est publié par Graphiks-org/kffi ; l'artifact racine
                // org.graphiks:kffi résout la variante de plateforme (jvm/android/native).
                api("org.graphiks:kffi:1.0.0-SNAPSHOT") {
                    // The published debug snapshot may lag behind the release
                    // native artifact. Keep Android consumers on the tested
                    // release variant until the debug publication is refreshed.
                    attributes {
                        attribute(BuildTypeAttr.ATTRIBUTE, objects.named("release"))
                    }
                }
            }
        }

        androidMain {
            dependencies {
                // Android bindings use kffi for both downcalls and callback trampolines.
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

// Android unit tests run on the host JVM through AGP's unit-test runner; the Kotest
// specs in androidUnitTest need the JUnit Platform engine, mirroring jvmTest above.
// AGP registers testDebugUnitTest lazily, so match by name with a live collection.
tasks.withType<Test>().matching { it.name == "testDebugUnitTest" }.configureEach {
    useJUnitPlatform()
}

tasks.withType(MergeSourceSetFolders::class.java).configureEach {
    dependsOn("fetch-native-dependencies")
}
tasks.withType(CInteropProcess::class.java).configureEach {
    dependsOn("fetch-native-dependencies")
}

tasks.register<Sync>("copyDocsToRoot") {
    dependsOn("dokkaGeneratePublicationHtml")
    from(project.layout.buildDirectory.dir("dokka/html"))
    into(rootDir.resolve("doc/html"))
}

tasks.register<Task>("generateDocs") {
    group = "documentation"
    description = "Generates the documentation in HTML format, then copies the files into the 'doc' folder."
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
val genericJvmLookupImport = "import org.graphiks.kffi.findOrThrow"
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

// kextract must see the same WebGPU ABI on every generation host. The platform stdlib headers
// behind stdint.h/stddef.h/math.h are not part of the WebGPU API and leak host-specific types
// such as `long` and Windows' private `_Wchar` record into libclang. Replace them only in the
// generator input with the fixed-width types and macros WebGPU actually uses. The original
// downloaded headers remain untouched for cinterop and native compilation.
val sanitizedNativeHeadersDirectory = project.file("build/native/sanitized")
val sanitizedNativeHeader = sanitizedNativeHeadersDirectory.resolve("wgpu.h")
val sanitizedWebGpuHeader = sanitizedNativeHeadersDirectory.resolve("webgpu.h")

val kextractWebGpuAbiPrelude = """
    typedef unsigned char uint8_t;
    typedef unsigned short uint16_t;
    typedef signed int int32_t;
    typedef unsigned int uint32_t;
    typedef signed long long int64_t;
    typedef unsigned long long uint64_t;
    typedef unsigned long long size_t;
    #define UINT32_MAX 4294967295U
    #define UINT64_MAX 18446744073709551615ULL
    #define SIZE_MAX 18446744073709551615ULL
    #define NULL ((void*)0)
    #define NAN (0.0f / 0.0f)
""".trimIndent()

tasks.register("sanitizeNativeHeaders") {
    group = "generation"
    description = "Rewrites WebGPU's system-header dependencies to fixed-width generator definitions"
    dependsOn(*jvmBindingNativeDependencyTasks.toTypedArray())
    inputs.file(project.file("build/native/wgpu.h"))
    inputs.file(project.file("build/native/webgpu.h"))
    outputs.file(sanitizedNativeHeader)
    outputs.file(sanitizedWebGpuHeader)
    doLast {
        fun sanitize(source: File, target: File) {
            target.parentFile.mkdirs()
            val header = source.readText()
            target.writeText(
                if (source.name == "webgpu.h") {
                    header
                        .replace("#include <stdint.h>", kextractWebGpuAbiPrelude)
                        .replace("#include <stddef.h>", "")
                        .replace("#include <math.h>", "")
                } else {
                    header
                },
            )
        }
        sanitize(project.file("build/native/wgpu.h"), sanitizedNativeHeader)
        sanitize(project.file("build/native/webgpu.h"), sanitizedWebGpuHeader)
    }
}

tasks.register("verifySanitizedNativeHeaders") {
    group = "verification"
    description = "Verifies that kextract reads a host-independent WebGPU header surface."
    dependsOn("sanitizeNativeHeaders")
    inputs.file(sanitizedWebGpuHeader)

    doLast {
        val header = sanitizedWebGpuHeader.readText()
        require("#include <stdint.h>" !in header) {
            "The kextract header must not import the host stdint.h implementation"
        }
        require("#include <stddef.h>" !in header) {
            "The kextract header must not import the host stddef.h implementation"
        }
        require("#include <math.h>" !in header) {
            "The kextract header must not import the host math.h implementation"
        }
        require("typedef unsigned long long uint64_t;" in header) {
            "The kextract header must define uint64_t with a fixed 64-bit ABI"
        }
        require("typedef unsigned long long size_t;" in header) {
            "The kextract header must define size_t with the WebGPU 64-bit ABI"
        }
    }
}

tasks.register<Exec>("generateBindingsFromHeader") {
    group = "generation"
    description = "Generates unified KMP bindings from webgpu.h using kextract CLI"
    dependsOn(":kextract:createKextractImage")
    dependsOn(*jvmBindingNativeDependencyTasks.toTypedArray())
    dependsOn("sanitizeNativeHeaders")
    dependsOn("verifySanitizedNativeHeaders")

    val callbackBindings = project(":wgpu4k-native-specs")
        .file("src/jvmMain/resources/callback-bindings.yml")
    val nativeHeader = sanitizedNativeHeader
    val webGpuHeader = sanitizedWebGpuHeader

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
        val clangResourceInclude =
            providers.gradleProperty("wgpu4k.clangResourceDir")
                .orElse(providers.environmentVariable("WGPU4K_CLANG_RESOURCE_DIR"))
                .orNull
                ?: error(
                    "Linux binding generation needs clang's resource include dir; set " +
                        "-Pwgpu4k.clangResourceDir=<llvm>/lib/clang/<version>/include " +
                        "(or the WGPU4K_CLANG_RESOURCE_DIR env var).",
                )
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

// Binding sources are versioned. Normal compilation, packaging, and tests consume the
// committed files; run generateBindingsFromHeader explicitly when kffi, kextract, or the
// WebGPU header changes, then review and commit the regenerated bindings.

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

        val buildConsumers = tasks.filter { task ->
            task.name == "compileKotlinJvm" ||
                (task.name.startsWith("compile") && task.name.endsWith("KotlinAndroid")) ||
                task.name.startsWith("compileKotlin") ||
                task.name.endsWith("sourcesJar", ignoreCase = true) ||
                task.name in setOf(
                    "compileCommonMainKotlinMetadata",
                    "compileNativeMainKotlinMetadata",
                    "dokkaGeneratePublicationHtml",
                )
        }
        buildConsumers.forEach { task ->
            val dependencies = task.taskDependencies
                .getDependencies(task)
                .map { it.path }
                .toSet()
            require(generationTask.path !in dependencies) {
                "${task.path} must compile committed bindings without regenerating them; found $dependencies"
            }
        }

        val testWorkflow = rootDir.resolve(".github/workflows/test.yml")
        require("verifyGeneratedBindingsClean" !in testWorkflow.readText()) {
            "The normal test workflow must not regenerate versioned WebGPU bindings"
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
        val nativeHeader = sanitizedNativeHeader.absoluteFile
        val webGpuHeader = sanitizedWebGpuHeader.absoluteFile
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

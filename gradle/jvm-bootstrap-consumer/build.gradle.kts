plugins {
    kotlin("jvm") version "2.3.21"
    application
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// This is a standalone build, so it cannot read the binding project's coordinates directly and has
// to restate them. The group follows this fork's `cool.avocado` namespace rather than upstream's
// `io.ygdrasil`, and the version mirrors the same VERSION-env-or-SNAPSHOT rule the root build uses,
// so a release-tagged run resolves the artifact the publish step actually produced.
val bindingGroup: String = providers.gradleProperty("wgpu4k.bindingGroup").getOrElse("cool.avocado")
val bindingVersion: String =
    providers.gradleProperty("wgpu4k.bindingVersion")
        .orElse(providers.environmentVariable("VERSION").map { it.ifBlank { "v29.0.0-SNAPSHOT" } })
        .getOrElse("v29.0.0-SNAPSHOT")

dependencies {
    implementation("$bindingGroup:wgpu4k-native-jvm:$bindingVersion")
}

application {
    mainClass = "MainKt"
}

tasks.named<JavaExec>("run") {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

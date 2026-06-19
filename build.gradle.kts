allprojects {
  repositories {
    mavenLocal()
    google()
    mavenCentral()
  }

  group = "cool.avocado"
  version = System.getenv("VERSION")?.takeIf { it.isNotBlank() } ?: "v29.0.0-SNAPSHOT"
}

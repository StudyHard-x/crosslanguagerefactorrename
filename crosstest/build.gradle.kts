plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.0"
}


group = "com.cross"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://www.jetbrains.com/intellij-repository/releases") }
    maven { url = uri("https://plugins.jetbrains.com/maven/") }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
//    localPath.set("D:\\intellij2023\\IntelliJ IDEA 2022.3.2")
    version.set("2022.3.2")
    type.set("IU")
    plugins.set(listOf("java", "properties","JavaScript"))
}

dependencies {
    implementation("com.github.javaparser:javaparser-core:3.25.3");
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.encoding= "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set("223")
        untilBuild.set("231.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }


}

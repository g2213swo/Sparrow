import java.io.ByteArrayOutputStream

plugins {
    id("java")
}

subprojects {

    apply(plugin = "java")
    apply(plugin = "java-library")

    repositories {
        mavenCentral()
    }

    tasks.processResources {
        filteringCharset = "UTF-8"
        filesMatching(arrayListOf("library-version.properties")) {
            expand(rootProject.properties)
        }

        val commitHash = versionBanner()
        filesMatching(arrayListOf("plugin.yml", "*.yml", "*/*.yml")) {
            expand(
                Pair("git_version", commitHash),
                Pair("project_version", rootProject.properties["project_version"].toString() + "-" + commitHash)
            )
        }
    }

    tasks.compileJava {
        dependsOn(tasks.clean)
    }
}

fun versionBanner(): String {
    val os = ByteArrayOutputStream()
    project.exec {
        commandLine = "git rev-parse --short=8 HEAD".split(" ")
        standardOutput = os
    }
    return String(os.toByteArray()).trim()
}
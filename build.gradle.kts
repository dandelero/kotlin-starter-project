val kotlinVersion by extra("1.3.61")

val snapshotRepoUrl = uri("http://localhost:8081/artifactory/libs-snapshot-local")
val releaseRepoUrl = uri("http://localhost:8081/artifactory/libs-release-local")

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.3.61"
    id("com.diffplug.gradle.spotless") version "3.27.1"
}

repositories {
    mavenCentral()
    jcenter()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

artifacts {
    add("archives", sourcesJar)
}

publishing {
    repositories {
        maven {
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotRepoUrl else releaseRepoUrl
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.apache.commons:commons-text:1.8")
}

spotless {
    java {
        target("**/*.java")
        googleJavaFormat() // .aosp()

        importOrder("java", "javax", "org", "com") // A sequence of package names
//        importOrderFile 'spotless.importorder'				// An import ordering file, exported from Eclipse
        // As before, you can't specify both importOrder and importOrderFile at the same time
        // You probably want an empty string at the end - all of the imports you didn't specify
        // explicitly will go there.
        paddedCell()
        indentWithSpaces()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()

//        importOrderFile 'config/spotless/my-eclipse.importorder'
//        eclipse().configFile 'config/spotless/my-eclipse-format.xml'
//        eclipse().configFile 'spotless.eclipseformat.xml'	// XML file dumped out by the Eclipse formatter
        // If you have Eclipse preference or property files, you can use them too.
        // eclipse('4.7.1') to specify a specific version of eclipse,
        // available versions are: https://github.com/diffplug/spotless/tree/master/lib-extra/src/main/resources/com/diffplug/spotless/extra/eclipse_jdt_formatter
    }

    kotlin {
        target("**/*.kt")
        targetExclude("**/.gradle/**")
        indentWithSpaces()
        trimTrailingWhitespace()
        endWithNewline()
        ktlint().userData(mapOf("max_line_length" to "120", "insert_final_newline" to "true"))
    }

    kotlinGradle {
        // same as kotlin, but for .gradle.kts files (defaults to '*.gradle.kts')
        target("**/*.gradle.kts")

        indentWithSpaces()
        trimTrailingWhitespace()
        endWithNewline()
        // Optional user arguments can be set as such:
        ktlint().userData(mapOf("max_line_length" to "120", "insert_final_newline" to "true"))

        // doesn't support licenseHeader, because scripts don't have a package statement
        // to clearly mark where the license should go
    }
}

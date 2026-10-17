plugins {
    idea
    signing
    groovy
    `maven-publish`
}

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
}

val sourceJar = tasks.register<Jar>("sourceJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
}

val groovydocJar = tasks.register<Jar>("groovydocJar") {
    dependsOn(tasks.named("groovydoc"))
    from(tasks.named<Groovydoc>("groovydoc").map { it.destinationDir })
    archiveClassifier.set("javadoc")
}

tasks.register<Copy>("exportJavadoc") {
    dependsOn(tasks.named("groovydoc"))
    from(tasks.named<Groovydoc>("groovydoc").map { it.destinationDir })
    into("javadoc")
}

artifacts {
    add("archives", tasks.named("jar"))
    add("archives", sourceJar)
    add("archives", groovydocJar)
}

if (
    gradle.startParameter.taskNames.contains("uploadArchives") ||
    gradle.startParameter.taskNames.contains("publishPlugins")
) {
    signing {
        sign(configurations["archives"])
    }
} else {
    tasks.register("signArchives")
}

group = "com.github.rholder.gradle"

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])
            artifact(sourceJar)
        }
    }
}

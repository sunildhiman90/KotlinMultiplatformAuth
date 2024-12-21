import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `maven-publish`
    signing
}

publishing {
    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        // Provide artifacts information required by Maven Central
        pom {
            name = "KotlinMultiplatformAuth"
            description = "Kotlin Multiplatform Authentication Librar targetting all platforms."
            inceptionYear = "2024"
            url = "https://github.com/sunildhiman90/KotlinMultiplatformAuth/"
            licenses {
                license {
                    name = "Apache-2.0"
                    url = "https://opensource.org/licenses/Apache-2.0"
                }
            }
            developers {
                developer {
                    id = "sunildhiman90"
                    name = "Sunil Kumar"
                    url = "https://github.com/sunildhiman90"
                }
            }
            scm {
                url = "https://github.com/sunildhiman90/KotlinMultiplatformAuth"
                connection = "https://github.com/sunildhiman90/KotlinMultiplatformAuth.git"
                developerConnection = "https://github.com/sunildhiman90"
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(publishing.publications)
    }
}

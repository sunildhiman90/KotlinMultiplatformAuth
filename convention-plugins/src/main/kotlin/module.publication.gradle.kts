import com.vanniktech.maven.publish.SonatypeHost
import gradle.kotlin.dsl.accessors._abffafc6a1518578ac0f8e7eb295d049.signing

plugins {
    id("com.vanniktech.maven.publish")
    signing
}

mavenPublishing {

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    //signAllPublications() //this is not working,so applied it manually below mavenPublishing block

    coordinates("io.github.sunildhiman90", project.name, "0.0.1-alpha")

    pom {
        name.set("KotlinMultiplatformAuth")
        description.set("Kotlin Multiplatform Authentication Library targetting all platforms")
        inceptionYear.set("2025")
        url.set("https://github.com/sunildhiman90/KotlinMultiplatformAuth/")

        signing {
            if (project.hasProperty("signing.gnupg.keyName")) {
                //useGpgCmd()
                useInMemoryPgpKeys(
                    System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyId"),
                    System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey"),
                    System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword")
                )
                sign(publishing.publications)
            }
        }

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("sunildhiman90")
                name.set("Sunil Kumar")
                url.set("https://github.com/sunildhiman90/")
            }
        }
        scm {
            url.set("https://github.com/sunildhiman90/KotlinMultiplatformAuth/")
            connection.set("scm:git:git://github.com/sunildhiman90/sunildhiman90.git")
            developerConnection.set("scm:git:ssh://git@github.com/sunildhiman90/sunildhiman90.git")
        }
    }
}



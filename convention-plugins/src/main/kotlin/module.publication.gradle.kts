import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("io.github.sunildhiman90", project.name, "0.0.1-alpha")

    pom {
        name.set("KotlinMultiplatformAuth")
        description.set("Kotlin Multiplatform Authentication Library targetting all platforms")
        inceptionYear.set("2025")
        url.set("https://github.com/sunildhiman90/KotlinMultiplatformAuth/")

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

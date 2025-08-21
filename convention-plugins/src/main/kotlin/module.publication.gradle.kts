plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {

    signAllPublications()

    val version: String by project
    val name: String by project
    val group: String by project
    coordinates(group, name, version.toString())

    pom {
        this.name.set("KotlinMultiplatformAuth")
        description.set("Kotlin Multiplatform Authentication Library targetting all platforms")
        inceptionYear.set("2025")
        url.set("https://github.com/sunildhiman90/KotlinMultiplatformAuth/")

        licenses {
            license {
                this.name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("sunildhiman90")
                this.name.set("Sunil Kumar")
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

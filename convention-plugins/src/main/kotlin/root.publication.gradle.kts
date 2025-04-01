plugins {
    id("com.vanniktech.maven.publish") apply false
}

// these will be used for all projects ie. kmauth-core, kmauth-google etc.
allprojects {
    group = "io.github.sunildhiman90"
    version = "0.0.3-alpha02"
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.ksp) apply false
}

allprojects {
    group = "com.earnergy"
    version = "0.1.0"
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

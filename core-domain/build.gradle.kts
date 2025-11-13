import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvmToolchain(21)

    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.coroutines.core)
            }
        }
        val jvmMain by getting
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    kotlinOptions.jvmTarget = "21"
}

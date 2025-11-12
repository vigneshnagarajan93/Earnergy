import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.coroutines.core)
            }
        }
        val jvmMain by getting
    }

    targets.withType<KotlinJvmTarget>().configureEach {
        compilations.configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }
    }
}

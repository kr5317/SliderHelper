object Apps {
    const val version = "1.0.0"
    const val code = 10000

    const val group = "com.kr5317.verify"
    const val applicationId = "com.kr5317.verify"
    const val compileSdkVersion = 31
    const val minSdkVersion = 21
    const val targetSdkVersion = 30
}

object Versions {
    const val kotlin = "1.5.31"
    const val compose = "1.0.0"
    const val androidTools = "4.2.0"
}

object Deps {
    object JetBrains {
        object Kotlin {
            const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
        }

        object Compose {
            const val gradlePlugin = "org.jetbrains.compose:compose-gradle-plugin:${Versions.compose}"
        }
    }

    object Android {
        object Tools {
            const val gradlePlugin = "com.android.tools.build:gradle:${Versions.androidTools}"
        }
    }
}

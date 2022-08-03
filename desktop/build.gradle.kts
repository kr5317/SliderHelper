import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.openjfx.javafxplugin") version "0.0.8"
}

javafx {
    version = "11"
    modules("javafx.controls", "javafx.fxml", "javafx.web", "javafx.swing")
}

kotlin {
    jvm {
        withJava()
    }

    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.kr5317.verify.MainKt"

        nativeDistributions {
            modules(*jdkModules)

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "verify"
            packageVersion = Apps.version
            vendor = "verify"
            description = "verify"
            copyright = "verify copyright"

            windows {
                shortcut = true
                upgradeUuid = "13E9A954-B4AB-49B2-80B3-56A01B2239D5"
                installationPath = "verify"
                iconFile.set(project.file("icon.ico"))
            }

            macOS {
                bundleID = "com.kr5317.verify"
            }

            linux {
            }
        }
    }
}

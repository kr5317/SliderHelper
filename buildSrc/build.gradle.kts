plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(Deps.JetBrains.Compose.gradlePlugin)
    implementation(Deps.JetBrains.Kotlin.gradlePlugin)
    implementation(Deps.Android.Tools.gradlePlugin)
}

kotlin {
    sourceSets.getByName("main").kotlin.srcDir("src/main/config")
}
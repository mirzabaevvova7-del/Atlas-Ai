plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.megapolis.atlasai"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.megapolis.atlasai"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}

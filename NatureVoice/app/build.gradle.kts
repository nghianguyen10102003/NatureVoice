plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "vn.edu.usth.naturevoice"
    compileSdk = 34

    defaultConfig {
        applicationId = "vn.edu.usth.naturevoice"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }


}
dependencies {
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.1")
    implementation ("androidx.room:room-runtime:2.5.0")
    annotationProcessor ("androidx.room:room-compiler:2.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation ("io.socket:socket.io-client:2.0.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
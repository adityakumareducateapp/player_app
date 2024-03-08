plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.playerapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.playerapp"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.media3:media3-ui:1.2.1")

    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")

    // Media3
    implementation("org.videolan.android:libvlc-all:3.3.12")

    implementation ("com.google.android.exoplayer:exoplayer-core:2.16.1")
    implementation ("com.google.android.exoplayer:exoplayer-dash:2.16.1")
    implementation ("com.google.android.exoplayer:exoplayer-ui:2.16.1")


    implementation("com.google.android.exoplayer:exoplayer-hls:2.16.1")
    implementation ("com.google.android.exoplayer:exoplayer-smoothstreaming:2.16.1")
    implementation ("com.google.android.exoplayer:extension-mediasession:2.16.1")
    implementation ("com.google.android.exoplayer:extension-okhttp:2.16.1")
    implementation ("com.google.android.exoplayer:extension-rtmp:2.16.1")


    implementation ("androidx.media3:media3-common:1.1.0")

    implementation ("com.jakewharton.timber:timber:5.0.1")


    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.2.1")
    implementation ("androidx.media3:media3-session:1.2.1")


    implementation("com.github.anilbeesetti.nextlib:nextlib-media3ext:0.6.0") // To add media3 software decoders and extensions
    implementation("com.github.anilbeesetti.nextlib:nextlib-mediainfo:0.6.0") // To get media info through ffmpeg
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("com.googlecode.juniversalchardet:juniversalchardet:1.0.3")

}
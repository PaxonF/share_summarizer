plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.paxonf.sharesummarizer"
    compileSdk = 35 // Stable API level for current setup

    defaultConfig {
        applicationId = "com.paxonf.sharesummarizer"
        minSdk = 26 // Material 3 recommends API 21+, some features might need higher
        targetSdk = 35
        versionCode = 4
        versionName = "1.3-alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        freeCompilerArgs += listOf("-Xskip-metadata-version-check")
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8" // For Kotlin 1.9.22
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        baseline = file("lint-baseline.xml")
        disable += "InvalidPackage"
        disable += "CoroutineCreationDuringComposition"
        abortOnError = false
    }
}

dependencies {
    // Explicitly define Kotlin version to avoid conflicts
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")
    
    // Core dependencies for API and JSON processing
    implementation("org.json:json:20250107")
    implementation("com.squareup.okhttp3:okhttp:4.12.0") 
    
    // JetBrains Markdown library
    implementation("org.jetbrains:markdown:0.5.0")
    
    // Compose UI dependencies - use a recent stable BOM version that includes SliderState
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material") // For icons
    implementation("androidx.compose.material:material-icons-extended:1.6.8") // Added for more icons like ArrowDropUp

    // Android core dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core-splashscreen:1.0.1") // Added for splash screen
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // For ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // For Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Added Readability4J and Jsoup dependencies
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("net.dankito.readability4j:readability4j:1.0.8")

    implementation("com.github.jeziellago:compose-markdown:0.4.0")
}
plugins {
      id("com.android.application")
      id("org.jetbrains.kotlin.android")
      id("com.google.dagger.hilt.android")
      id("com.google.devtools.ksp")
  }

  android {
      namespace = "com.aivos.echovault"
      compileSdk = 34

      defaultConfig {
          applicationId = "com.aivos.echovault"
          minSdk = 26
          targetSdk = 34
          versionCode = 1
          versionName = "1.0.0"
          testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
          vectorDrawables { useSupportLibrary = true }
      }

      signingConfigs {
          create("release") {
              storeFile = file("keystore.jks")
              storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "echovault123"
              keyAlias = System.getenv("KEY_ALIAS") ?: "echovault"
              keyPassword = System.getenv("KEY_PASSWORD") ?: "echovault123"
          }
      }

      buildTypes {
          release {
              isMinifyEnabled = true
              isShrinkResources = true
              proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
              signingConfig = signingConfigs.getByName("release")
          }
          debug {
              applicationIdSuffix = ".debug"
              isDebuggable = true
          }
      }

      compileOptions {
          sourceCompatibility = JavaVersion.VERSION_17
          targetCompatibility = JavaVersion.VERSION_17
      }
      kotlinOptions { jvmTarget = "17" }
      buildFeatures { compose = true }
      composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
      packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
  }

  dependencies {
      val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
      implementation(composeBom)
      implementation("androidx.compose.ui:ui")
      implementation("androidx.compose.ui:ui-graphics")
      implementation("androidx.compose.ui:ui-tooling-preview")
      implementation("androidx.compose.material3:material3")
      implementation("androidx.compose.material:material-icons-extended")
      implementation("androidx.compose.animation:animation")
      implementation("androidx.activity:activity-compose:1.9.0")
      implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
      implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
      implementation("androidx.navigation:navigation-compose:2.7.7")

      // Hilt
      implementation("com.google.dagger:hilt-android:2.51.1")
      ksp("com.google.dagger:hilt-android-compiler:2.51.1")
      implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

      // Room
      implementation("androidx.room:room-runtime:2.6.1")
      implementation("androidx.room:room-ktx:2.6.1")
      ksp("androidx.room:room-compiler:2.6.1")

      // Security Crypto
      implementation("androidx.security:security-crypto:1.1.0-alpha06")

      // Biometric
      implementation("androidx.biometric:biometric:1.1.0")

      // WorkManager
      implementation("androidx.work:work-runtime-ktx:2.9.0")
      implementation("androidx.hilt:hilt-work:1.2.0")
      ksp("androidx.hilt:hilt-compiler:1.2.0")

      // DataStore
      implementation("androidx.datastore:datastore-preferences:1.1.1")

      // Coroutines
      implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

      // Gson
      implementation("com.google.code.gson:gson:2.11.0")

      // Accompanist
      implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

      // Splashscreen
      implementation("androidx.core:core-splashscreen:1.0.1")

      testImplementation("junit:junit:4.13.2")
      androidTestImplementation("androidx.test.ext:junit:1.2.1")
      androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
      androidTestImplementation(composeBom)
      androidTestImplementation("androidx.compose.ui:ui-test-junit4")
      debugImplementation("androidx.compose.ui:ui-tooling")
      debugImplementation("androidx.compose.ui:ui-test-manifest")
  }
  
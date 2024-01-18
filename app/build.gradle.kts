plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace="com.bravoromeo.wallet_functionality"
    compileSdk=34

    defaultConfig {
        applicationId="com.bravoromeo.wallet_functionality"
        minSdk=25
        targetSdk=34
        versionCode=1
        versionName="1.0"

        testInstrumentationRunner="androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary=true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled=false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility=JavaVersion.VERSION_1_8
        targetCompatibility=JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget="1.8"
    }
    buildFeatures {
        compose=true
    }
    composeOptions {
        kotlinCompilerExtensionVersion="1.5.1"
    }
    packaging {
        resources {
            excludes+="/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")//2.6.2 originally
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


    //google play services integration
    implementation("com.google.android.gms:play-services-pay:16.4.0")
    //Retrofit and Gson converter for Retrofit. Necessary to use Wallet API
    //CURRENTLY NOT IN USE
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //Google Okhttp3 library to access Wallet API REST
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:okhttp-oauth:4.9.1")
    //Google OAuth2 library. Necessary for authentication to the Wallet REST API
    implementation ("com.auth0:java-jwt:3.19.1")
    implementation ("com.auth0:jwks-rsa:0.9.0")
    implementation ("com.fasterxml.jackson.core:jackson-databind:2.13.4.2")
    implementation ("com.google.apis:google-api-services-oauth2:v2-rev20200213-2.0.0")
    implementation ("com.google.api-client:google-api-client:1.25.0")
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.10.0")
    implementation ("com.squareup.okhttp3:okhttp:4.3.1")
    implementation ("javax.json:javax.json-api:1.1")
    implementation ("org.glassfish:javax.json:1.1")
    //Google Wallet API Client library
    implementation(files("C:/Users/dbombino/AndroidStudioProjects/research/wallet_functionality/wallet_functionality/app/src/main/java/com/bravoromeo/wallet_functionality/repositories/google_wallet/google-walletobjects-v1-rev_20230821-java.jar"))
}
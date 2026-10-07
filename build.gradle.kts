plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("com.google.gms.google-services") }

android { namespace="com.epilan.app"; compileSdk=35
 defaultConfig { applicationId="com.epilan.app"; minSdk=26; targetSdk=35; versionCode=1; versionName="1.0.0" }
}

dependencies {
 implementation(platform("androidx.compose:compose-bom:2024.12.01"))
 implementation("androidx.activity:activity-compose:1.10.0")
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.ui:ui-tooling-preview")
 debugImplementation("androidx.compose.ui:ui-tooling")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
 implementation("androidx.work:work-runtime-ktx:2.10.0")
 implementation("com.google.firebase:firebase-messaging:24.1.0")
 implementation("io.ktor:ktor-client-okhttp:3.0.3")
 implementation("io.ktor:ktor-client-content-negotiation:3.0.3")
 implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
 implementation("androidx.datastore:datastore-preferences:1.1.1")
}

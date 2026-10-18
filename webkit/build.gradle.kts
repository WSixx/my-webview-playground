plugins {
    id("com.android.library")
}

android {
    namespace = "androidx.webkit"
    compileSdk = 37

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation("org.chromium:fetch_boundary_interfaces:1.12.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.core:core:1.15.0")
    implementation("androidx.concurrent:concurrent-futures:1.2.0")
    compileOnly("org.jspecify:jspecify:1.0.0")
}

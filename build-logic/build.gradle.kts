plugins {
    `kotlin-dsl`
}

group = "br.com.lucad.mywebviewplayground.buildlogic"

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "mywebviewplayground.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidCompose") {
            id = "mywebviewplayground.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
    }
}

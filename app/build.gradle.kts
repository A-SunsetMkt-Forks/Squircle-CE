/*
 * Copyright 2025 Squircle CE contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.blacksquircle.application")
    alias(libs.plugins.android.baselineprofile)
}

android {
    namespace = "com.blacksquircle.ui"

    defaultConfig {
        applicationId = "com.blacksquircle.ui"
        versionCode = 10024
        versionName = "2025.1.0"
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    packaging {
        resources {
            excludes += "META-INF/versions/9/OSGI-INF/**"
        }
    }
}

dependencies {

    // Core
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.timber)
    coreLibraryDesugaring(libs.android.desugaring)

    // Google Play
    googlePlayImplementation(libs.appupdate)

    // UI
    implementation(libs.androidx.appcompat)
    implementation(libs.materialdesign)

    // AAC
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.navigation)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // DI
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    // Modules
    implementation(project(":feature-changelog"))
    implementation(project(":feature-editor:api"))
    implementation(project(":feature-editor:impl"))
    implementation(project(":feature-explorer:api"))
    implementation(project(":feature-explorer:impl"))
    implementation(project(":feature-fonts:api"))
    implementation(project(":feature-fonts:impl"))
    implementation(project(":feature-servers:api"))
    implementation(project(":feature-servers:impl"))
    implementation(project(":feature-settings"))
    implementation(project(":feature-shortcuts:api"))
    implementation(project(":feature-shortcuts:impl"))
    implementation(project(":feature-themes:api"))
    implementation(project(":feature-themes:impl"))
    implementation(project(":common-core"))
    implementation(project(":common-ui"))

    // Baseline Profile
    baselineProfile(project(":benchmark"))

    // Tests
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.junit.ext)
    androidTestImplementation(libs.test.runner)
}
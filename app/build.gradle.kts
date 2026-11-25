plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "fr.localspellcheck"
    compileSdk = 35

    defaultConfig {
        applicationId = "fr.localspellcheck"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    packaging {
        resources {
            // Some transitive libraries ship identical metadata/license resources.
            pickFirsts += "META-INF/AL2.0"
            pickFirsts += "META-INF/LGPL2.1"
            pickFirsts += "META-INF/mailcap.default"
            pickFirsts += "META-INF/LICENSE.md"
        }
    }
}

// Resolve duplicate classes pulled in by LanguageTool's legacy JAXB dependency graph.
configurations.configureEach {
    exclude(group = "com.sun.istack", module = "istack-commons-runtime")
    exclude(group = "com.sun.xml.bind", module = "jaxb-core")
    exclude(group = "org.glassfish.jaxb", module = "txw2")
    exclude(group = "javax.activation", module = "javax.activation-api")
    exclude(group = "com.intellij", module = "annotations")
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // LanguageTool embarqué : aucun endpoint distant n'est utilisé par cette application.
    // Compatibilité Android à valider sur appareil : certaines dépendances transitives
    // de LanguageTool peuvent nécessiter des adaptations ou exclusions.
    implementation("org.languagetool:language-fr:6.6")
    implementation("org.languagetool:language-en:6.6")

    debugImplementation("androidx.compose.ui:ui-tooling")
}

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
  id("delta.lint.kotlin")
  id("delta.lint.kts")
}

val mockitoAgent = configurations.create("mockitoAgent")

android {
  namespace = "dev.shadoe.delta.domain"
  compileSdk = 35

  defaultConfig {
    minSdk = 30
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  kotlinOptions { jvmTarget = "21" }

  @Suppress("UnstableApiUsage")
  testOptions {
    unitTests { all { it.jvmArgs("-javaagent:${mockitoAgent.asPath}") } }
  }
}

dependencies {
  implementation(project(path = ":api"))
  implementation(project(path = ":data"))
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
  mockitoAgent(libs.mockito.core) { isTransitive = false }
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(project(path = ":system-api-stubs"))
}

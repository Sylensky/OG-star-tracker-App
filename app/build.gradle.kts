import java.io.FileInputStream
import java.util.Properties

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.jetbrains.kotlin.android)
}

android {
	val packageName = "og.ogstartracker"

	namespace = packageName
	compileSdk = 34

	// Tracker base URL - can be overridden with -PtrackerUrl or TRACKER_URL env var
	// Ensure trailing slash for Retrofit baseUrl
	val defaultTrackerUrl = "http://tracker.local/"
	val defaultTrackerFallback = "http://192.168.4.1/"
	val propertyTrackerUrl = (project.findProperty("trackerUrl") as String?) ?: System.getenv("TRACKER_URL")
	val buildTrackerUrl = propertyTrackerUrl ?: defaultTrackerUrl

	val buildVersionCode = Integer.parseInt(System.getenv("VERSION_CODE") ?: "7")
	val buildVersionName = System.getenv("VERSION_NAME") ?: "1.0.6-beta01"

	defaultConfig {
		applicationId = packageName
		minSdk = 26
		targetSdk = 34
		versionCode = buildVersionCode
		versionName = buildVersionName

		// Expose TRACKER_URL and a fallback URL to the app via BuildConfig for all build variants
		buildConfigField("String", "TRACKER_URL", "\"$buildTrackerUrl\"")
		val propertyTrackerFallback = (project.findProperty("trackerFallback") as String?) ?: System.getenv("TRACKER_URL_FALLBACK")
		val buildTrackerFallback = propertyTrackerFallback ?: defaultTrackerFallback
		buildConfigField("String", "TRACKER_URL_FALLBACK", "\"$buildTrackerFallback\"")

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

		resourceConfigurations.addAll(listOf("en", "cs", "de"))
	}

	signingConfigs {
		getByName("debug") { }

		create("release") {
			val secretsPropertiesFile = rootProject.file("secrets.properties")
			val secretProperties = Properties()
			if (secretsPropertiesFile.exists()) {
				secretProperties.load(
					FileInputStream(
						secretsPropertiesFile
					)
				)
			}
			val releaseStorePassword = "${secretProperties["secretStorePassword"]}"
			val releaseKeyPassword = "${secretProperties["secretKeyPassword"]}"
			storeFile = file("prod.keystore")
			storePassword = releaseStorePassword
			keyAlias = "key0"
			keyPassword = releaseKeyPassword
		}
	}

	flavorDimensions.add("environment")

	buildTypes {
		debug {
			applicationIdSuffix = ".debug"
		}
		release {
			isMinifyEnabled = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
			signingConfig = signingConfigs.getByName("release")
		}
	}

	productFlavors {
		create("dev") { }
	}
	
	applicationVariants.all {
		outputs.all {
			val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
			val variantName = name
			val newApkName = "OGStarTrackerApp-${variantName}.apk"
			outputImpl.outputFileName = newApkName
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = JavaVersion.VERSION_17.toString()
	}
	buildFeatures {
		compose = true
		buildConfig = true
	}
	composeOptions {
		kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtension.get()
	}
}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.ui)
	implementation(libs.androidx.ui.graphics)
	implementation(libs.androidx.ui.tooling.preview)
	implementation(libs.material)
	implementation(libs.androidx.material3)
	implementation(libs.androidx.navigation.runtime.ktx)
	implementation(libs.androidx.navigation.compose)
	implementation(libs.datastore)
	implementation(libs.permissions)
	implementation(libs.markdown)
	implementation(libs.retrofit)
	implementation(libs.retrofitScalars)
	implementation(libs.retrofitConverter)
	implementation(libs.moshi)
	implementation(libs.moshiKotlin)
	implementation(libs.okhttp)
	implementation(libs.timber)
	implementation(libs.okhttpInterceptor)
	implementation(libs.splashScreen)
	implementation(libs.preferences)
	debugImplementation(libs.androidx.ui.tooling)

	implementation(libs.koinCore)
	implementation(libs.koinAndroid)
	implementation(libs.koinCompose)

	implementation(libs.accompanistSystemUiController)
}
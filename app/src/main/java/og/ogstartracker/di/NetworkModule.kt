package og.ogstartracker.di

import com.squareup.moshi.Moshi
import og.ogstartracker.BuildConfig
import og.ogstartracker.network.ArduinoApi
import og.ogstartracker.repository.ArduinoRepository
import og.ogstartracker.repository.ArduinoRepositoryImpl
import og.ogstartracker.repository.DataStoreRepository
import og.ogstartracker.repository.DataStoreRepositoryImpl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

const val NETWORK_REQUEST_TIMEOUT = 30L
const val HTTP_LOGGING_INTERCEPTOR = "HTTP_LOGGING_INTERCEPTOR"

val networkModule = module {
	// Always use fallback IP for tracker hotspot connections
	// tracker.local requires mDNS which is unreliable on Android, especially
	// when the phone is on a hotspot without internet. The tracker device
	// always uses 192.168.4.1 on its AP, so we use that directly.
	fun getEffectiveBaseUrl(): String {
		val fallback = BuildConfig.TRACKER_URL_FALLBACK
		Timber.tag("NetworkModule").d("Using tracker IP directly: %s (bypassing DNS)", fallback)
		return fallback
	}

	fun createOkHttpClient(interceptors: List<Interceptor>): OkHttpClient {
		val builder = OkHttpClient.Builder()
		interceptors.forEach { builder.addInterceptor(it) }

		// Retry interceptor: if DNS resolution for tracker host fails, retry once with fallback host
		val retryInterceptor = Interceptor { chain ->
			try {
				return@Interceptor chain.proceed(chain.request())
			} catch (e: java.net.UnknownHostException) {
				Timber.tag("OkHttp").w(e, "UnknownHostException - attempting fallback host")
				val original = chain.request()
				val fallbackBase = BuildConfig.TRACKER_URL_FALLBACK
				val parsed = fallbackBase.toHttpUrlOrNull()
				if (parsed == null) throw e

				val newUrl = original.url.newBuilder()
					.scheme(parsed.scheme)
					.host(parsed.host)
					.port(parsed.port)
					.build()

				val newRequest = original.newBuilder().url(newUrl).build()
				return@Interceptor chain.proceed(newRequest)
			}
		}

		builder.addInterceptor(retryInterceptor)
		// Shorter timeouts for faster failure detection on hotspot
		builder.connectTimeout(10L, TimeUnit.SECONDS)
		builder.readTimeout(15L, TimeUnit.SECONDS)
		builder.writeTimeout(15L, TimeUnit.SECONDS)

		return builder.build()
	}

	fun provideRetrofit(
		url: String,
		interceptors: List<Interceptor>,
	): Retrofit {
		val client = createOkHttpClient(interceptors)

		return Retrofit.Builder()
			.baseUrl(url)
			.client(client)
			.addConverterFactory(ScalarsConverterFactory.create())
			.build()
	}

	fun provideRetrofitWithMoshi(
		url: String,
		interceptors: List<Interceptor>,
		moshi: Moshi
	): Retrofit {
		val client = createOkHttpClient(interceptors)

		return Retrofit.Builder()
			.baseUrl(url)
			.client(client)
			.addConverterFactory(ScalarsConverterFactory.create())
			.addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create(moshi))
			.build()
	}

	single {
		val effective = getEffectiveBaseUrl()
		provideRetrofitWithMoshi(
			url = effective,
			interceptors = listOf(
				get(named(HTTP_LOGGING_INTERCEPTOR))
			),
			moshi = get()
		).create(ArduinoApi::class.java)
	}

	single(named(HTTP_LOGGING_INTERCEPTOR)) {
		val logging = HttpLoggingInterceptor.Logger { message ->
			Timber.tag("OkHttp").d(message)
		}
		HttpLoggingInterceptor(logging).apply {
			level = HttpLoggingInterceptor.Level.BODY
		}
	} bind Interceptor::class

	single {
		OkHttpClient.Builder()
			.apply {
				addInterceptor(get<HttpLoggingInterceptor>())
			}
			.build()
	}

	single {
		HttpLoggingInterceptor().apply {
			level = HttpLoggingInterceptor.Level.BODY
		}
	}

	single<ArduinoRepository> {
		ArduinoRepositoryImpl(
			arduinoApi = get()
		)
	}

	single<DataStoreRepository> {
		DataStoreRepositoryImpl(
			context = androidContext()
		)
	}

	single {
		Moshi.Builder()
			.add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
			.build()
	}
}
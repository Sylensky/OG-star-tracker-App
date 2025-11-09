package og.ogstartracker.network

import og.ogstartracker.network.models.PositionResponse
import og.ogstartracker.network.models.PresetResponse
import og.ogstartracker.network.models.StatusResponse
import og.ogstartracker.network.models.TrackingRatesResponse
import og.ogstartracker.network.models.VersionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ArduinoApi {

	// Tracking Control
	@GET("on")
	suspend fun startSiderealTracking(
		@Query("direction") direction: Int,
		@Query("trackingSpeed") trackingSpeed: Int,
	): Response<String>

	@GET("off")
	suspend fun stopSiderealTracking(): Response<String>

	// Slewing Control
	@GET("startslew")
	suspend fun startSlew(
		@Query("speed") speed: Int,
		@Query("direction") direction: Int
	): Response<String>

	@GET("stopslew")
	suspend fun stopSlew(): Response<String>

	// Goto Control
	@GET("gotoRA")
	suspend fun gotoRA(
		@Query("currentRA") currentRA: String,
		@Query("targetRA") targetRA: String,
		@Query("speed") speed: Int
	): Response<String>

	@GET("abort-goto-ra")
	suspend fun abortGotoRA(): Response<String>

	// Position Management
	@GET("setPosition")
	suspend fun setPosition(
		@Query("currentRA") currentRA: String
	): Response<String>

	@GET("getCurrentPosition")
	suspend fun getCurrentPosition(): Response<PositionResponse>

	// Intervalometer Control
	@GET("setCurrent")
	suspend fun setCurrent(
		@Query("mode") mode: Int,
		@Query("preset") preset: Int,
		@Query("captureMode") captureMode: Int,
		@Query("exposureTime") exposureTime: Int,
		@Query("exposures") exposures: Int,
		@Query("preDelay") preDelay: Int,
		@Query("delay") delay: Int,
		@Query("frames") frames: Int,
		@Query("panAngle") panAngle: Int,
		@Query("panDirection") panDirection: Int,
		@Query("enableTracking") enableTracking: Int,
		@Query("ditherChoice") ditherChoice: Int,
		@Query("ditherFrequency") ditherFrequency: Int,
		@Query("focalLength") focalLength: Int,
		@Query("pixelSize") pixelSize: Int
	): Response<String>

	@GET("readPreset")
	suspend fun readPreset(
		@Query("preset") preset: Int
	): Response<PresetResponse>

	@GET("abort")
	suspend fun abortCapture(): Response<String>

	// Tracking Rates
	@GET("getTrackingRates")
	suspend fun getTrackingRates(): Response<TrackingRatesResponse>

	@GET("saveTrackingRatePreset")
	suspend fun saveTrackingRatePreset(
		@Query("preset") preset: Int,
		@Query("type") type: Int,
		@Query("customRate") customRate: Int
	): Response<String>

	@GET("loadTrackingRatePreset")
	suspend fun loadTrackingRatePreset(
		@Query("preset") preset: Int
	): Response<TrackingRatesResponse>

	// Status & Info
	@GET("status")
	suspend fun getStatus(): Response<StatusResponse>

	@GET("version")
	suspend fun getVersion(): Response<VersionResponse>

	// Catalog Search
	@GET("starSearch")
	suspend fun starSearch(
		@Query("catalog") catalog: Int,
		@Query("query") query: String
	): Response<String>

	// Settings
	@GET("setlang")
	suspend fun setLanguage(
		@Query("lang") lang: Int
	): Response<String>
}
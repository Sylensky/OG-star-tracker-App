package og.ogstartracker.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ArduinoApi {

	// ==================== TRACKING CONTROL ====================

	/**
	 * Enable sidereal tracking at specified rate and direction
	 * @param direction Hemisphere/direction (0=northern/clockwise, 1=southern/counter-clockwise)
	 * @param trackingSpeed Tracking rate value (depends on tracking rate type selected)
	 */
	@GET("on")
	suspend fun startSiderealTracking(
		@Query("direction") direction: Int,
		@Query("trackingSpeed") trackingSpeed: Int,
	): Response<String>

	/**
	 * Disable sidereal tracking
	 */
	@GET("off")
	suspend fun stopSiderealTracking(): Response<String>

	// ==================== SLEWING CONTROL ====================

	/**
	 * Start manual slewing left/west
	 * @param speed Slew speed multiplier (2-400, lower=faster)
	 * @deprecated Use startSlew with direction parameter instead
	 */
	@GET("left")
	suspend fun turnLeft(
		@Query("speed") speed: Int
	): Response<String>

	/**
	 * Start manual slewing right/east
	 * @param speed Slew speed multiplier (2-400, lower=faster)
	 * @deprecated Use startSlew with direction parameter instead
	 */
	@GET("right")
	suspend fun turnRight(
		@Query("speed") speed: Int
	): Response<String>

	/**
	 * Start manual slewing at specified speed and direction
	 * @param speed Slew speed multiplier (2-400, lower=faster)
	 * @param direction Direction (0=left/west, 1=right/east)
	 */
	@GET("startslew")
	suspend fun startSlew(
		@Query("speed") speed: Int,
		@Query("direction") direction: Int,
	): Response<String>

	/**
	 * Stop current slewing operation
	 */
	@GET("stopslew")
	suspend fun stopSlew(): Response<String>

	// ==================== GOTO CONTROL ====================

	/**
	 * Move mount to target RA position
	 * @param currentRA Current RA position (HH:MM:SS format)
	 * @param targetRA Target RA position (HH:MM:SS format)
	 * @param speed Goto speed multiplier (2-400, lower=faster)
	 */
	@GET("gotoRA")
	suspend fun gotoRA(
		@Query("currentRA") currentRA: String,
		@Query("targetRA") targetRA: String,
		@Query("speed") speed: Int,
	): Response<String>

	/**
	 * Abort current goto RA operation
	 */
	@GET("abort-goto-ra")
	suspend fun abortGotoRA(): Response<String>

	// ==================== POSITION MANAGEMENT ====================

	/**
	 * Set current mount position
	 * @param currentRA Current RA position (HH:MM:SS format)
	 */
	@GET("setPosition")
	suspend fun setPosition(
		@Query("currentRA") currentRA: String,
	): Response<String>

	/**
	 * Get current mount position in steps
	 */
	@GET("getCurrentPosition")
	suspend fun getCurrentPosition(): Response<PositionResponse>

	// ==================== INTERVALOMETER CONTROL ====================

	/**
	 * Configure and start intervalometer capture sequence
	 * @deprecated Use setCurrent with all parameters instead
	 */
	@GET("start")
	suspend fun startCapture(
		@Query("exposure") exposure: Int,
		@Query("numExposures") numExposures: Int,
		@Query("focalLength") focalLength: Int,
		@Query("pixSize") pixSize: Int,
		@Query("ditherEnabled") ditherEnabled: Int,
		@Query("disableTracking") disableTracking: Int,
	): Response<String>

	/**
	 * Configure and start intervalometer capture sequence
	 * @param mode 0=start capture, 1=save to preset
	 * @param preset Preset number (0-4)
	 * @param captureMode 0=LONG_EXPOSURE_STILL, 1=LONG_EXPOSURE_MOVIE, 2=TIMELAPSE, 3=TIMELAPSE_PAN
	 * @param exposureTime Exposure duration in seconds
	 * @param exposures Number of exposures
	 * @param preDelay Pre-delay in seconds
	 * @param delay Delay between exposures in seconds
	 * @param frames Number of frames (for MOVIE mode)
	 * @param panAngle Pan angle in degrees × 100
	 * @param panDirection 0=left/west, 1=right/east
	 * @param enableTracking 0=off, 1=on
	 * @param ditherChoice 0=off, 1=on
	 * @param ditherFrequency Dither every N exposures
	 * @param focalLength Focal length in mm
	 * @param pixelSize Pixel size in microns × 100
	 */
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
		@Query("pixelSize") pixelSize: Int,
	): Response<String>

	/**
	 * Load intervalometer preset settings
	 * @param preset Preset number (0-4)
	 */
	@GET("readPreset")
	suspend fun readPreset(
		@Query("preset") preset: Int,
	): Response<String>

	/**
	 * Abort current intervalometer capture sequence
	 */
	@GET("abort")
	suspend fun abortCapture(): Response<String>

	// ==================== TRACKING RATES ====================

	/**
	 * Get current tracking rate configuration
	 * Types: 0=SIDEREAL, 1=LUNAR, 2=SOLAR, 3=KING, 4=CUSTOM
	 */
	@GET("getTrackingRates")
	suspend fun getTrackingRates(): Response<TrackingRatesResponse>

	/**
	 * Save tracking rate to preset
	 * @param preset Preset number (0-4)
	 * @param type 0=SIDEREAL, 1=LUNAR, 2=SOLAR, 3=KING, 4=CUSTOM
	 * @param customRate Custom rate value (required if type=4)
	 */
	@GET("saveTrackingRatePreset")
	suspend fun saveTrackingRatePreset(
		@Query("preset") preset: Int,
		@Query("type") type: Int,
		@Query("customRate") customRate: Int? = null,
	): Response<String>

	/**
	 * Load tracking rate from preset
	 * @param preset Preset number (0-4)
	 */
	@GET("loadTrackingRatePreset")
	suspend fun loadTrackingRatePreset(
		@Query("preset") preset: Int,
	): Response<TrackingRatesResponse>

	// ==================== STATUS & INFO ====================

	/**
	 * Get device status information
	 */
	@GET("status")
	suspend fun getStatus(): Response<StatusResponse>

	/**
	 * Get firmware version information
	 */
	@GET("version")
	suspend fun getVersion(): Response<VersionResponse>

	// ==================== CATALOG SEARCH ====================

	/**
	 * Search star/object catalog by name
	 * @param catalog 0=NGC2000, 1=NGC2000_COMPACT, 2=BSC5, 3=BSC5_COMPACT
	 * @param query Search query string (star/object name)
	 */
	@GET("starSearch")
	suspend fun starSearch(
		@Query("catalog") catalog: Int,
		@Query("query") query: String,
	): Response<CatalogSearchResponse>

	// ==================== SETTINGS ====================

	/**
	 * Set web interface language
	 * @param lang 0=English, 1=German, 2=Chinese
	 */
	@GET("setlang")
	suspend fun setLanguage(
		@Query("lang") lang: Int,
	): Response<String>
}
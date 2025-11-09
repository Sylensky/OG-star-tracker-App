package og.ogstartracker.repository

import kotlinx.coroutines.flow.StateFlow
import og.ogstartracker.domain.models.CaptureMode
import og.ogstartracker.domain.models.Hemisphere
import og.ogstartracker.domain.models.SlewDirection
import og.ogstartracker.domain.models.TrackingMode
import og.ogstartracker.network.Resource
import og.ogstartracker.network.models.PositionResponse
import og.ogstartracker.network.models.PresetResponse
import og.ogstartracker.network.models.StatusResponse
import og.ogstartracker.network.models.TrackingRatesResponse
import og.ogstartracker.network.models.VersionResponse

interface ArduinoRepository {

	/**
	 * Last message is cached so that message is not shown multiple times.
	 */
	val lastArduinoMessage: StateFlow<String?>

	// Tracking Control
	/**
	 * Starts the sidereal tracking in the specified direction.
	 */
	suspend fun startSideRealTracking(
		direction: Hemisphere,
		trackingMode: TrackingMode,
	): Resource<String>

	/**
	 * Stops the sidereal tracking.
	 */
	suspend fun stopSideRealTracking(): Resource<String>

	// Slewing Control
	/**
	 * Starts slewing at specified speed and direction.
	 */
	suspend fun startSlew(speed: Int, direction: SlewDirection): Resource<String>

	/**
	 * Stops the current slew operation.
	 */
	suspend fun stopSlew(): Resource<String>

	// Goto Control
	/**
	 * Move mount to target RA position.
	 */
	suspend fun gotoRA(currentRA: String, targetRA: String, speed: Int): Resource<String>

	/**
	 * Abort current goto RA operation.
	 */
	suspend fun abortGotoRA(): Resource<String>

	// Position Management
	/**
	 * Set current mount position.
	 */
	suspend fun setPosition(currentRA: String): Resource<String>

	/**
	 * Get current mount position in steps.
	 */
	suspend fun getCurrentPosition(): Resource<PositionResponse>

	// Intervalometer Control
	/**
	 * Configure and start intervalometer capture sequence.
	 */
	suspend fun setCurrent(
		mode: Int,
		preset: Int,
		captureMode: CaptureMode,
		exposureTime: Int,
		exposures: Int,
		preDelay: Int,
		delay: Int,
		frames: Int,
		panAngle: Int,
		panDirection: Int,
		enableTracking: Int,
		ditherChoice: Int,
		ditherFrequency: Int,
		focalLength: Int,
		pixelSize: Int
	): Resource<String>

	/**
	 * Load intervalometer preset settings.
	 */
	suspend fun readPreset(preset: Int): Resource<PresetResponse>

	/**
	 * Aborts the current capture process.
	 */
	suspend fun abortCapture(): Resource<String>

	// Tracking Rates
	/**
	 * Get current tracking rate configuration.
	 */
	suspend fun getTrackingRates(): Resource<TrackingRatesResponse>

	/**
	 * Save tracking rate to preset.
	 */
	suspend fun saveTrackingRatePreset(preset: Int, type: Int, customRate: Int): Resource<String>

	/**
	 * Load tracking rate from preset.
	 */
	suspend fun loadTrackingRatePreset(preset: Int): Resource<TrackingRatesResponse>

	// Status & Info
	/**
	 * Retrieves the current status of the tracker.
	 */
	suspend fun getStatus(showInUI: Boolean): Resource<StatusResponse>

	/**
	 * Resets the last message sent by the tracker.
	 */
	suspend fun resetLastMessage()

	/**
	 * Retrieves the version of the tracker firmware.
	 */
	suspend fun getVersion(): Resource<VersionResponse>

	// Catalog Search
	/**
	 * Search star/object catalog by name.
	 */
	suspend fun starSearch(catalog: Int, query: String): Resource<String>

	// Settings
	/**
	 * Set web interface language.
	 */
	suspend fun setLanguage(lang: Int): Resource<String>
}
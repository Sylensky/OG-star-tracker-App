package og.ogstartracker.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import og.ogstartracker.domain.models.CaptureMode
import og.ogstartracker.domain.models.Hemisphere
import og.ogstartracker.domain.models.SlewDirection
import og.ogstartracker.domain.models.TrackingMode
import og.ogstartracker.network.ArduinoApi
import og.ogstartracker.network.models.PositionResponse
import og.ogstartracker.network.models.PresetResponse
import og.ogstartracker.network.models.StatusResponse
import og.ogstartracker.network.models.TrackingRatesResponse
import og.ogstartracker.network.models.VersionResponse
import og.ogstartracker.utils.onSuccess
import og.ogstartracker.utils.tryOnline
import timber.log.Timber

class ArduinoRepositoryImpl constructor(
	private val arduinoApi: ArduinoApi,
) : ArduinoRepository {

	private val _lastArduinoMessage = MutableStateFlow<String?>(null)
	override val lastArduinoMessage = _lastArduinoMessage.asStateFlow()

	// Tracking Control
	override suspend fun startSideRealTracking(
		direction: Hemisphere,
		trackingMode: TrackingMode,
	) = tryOnline {
		// The trackingSpeed parameter expects the actual speed value
		// For built-in rates (SIDEREAL, LUNAR, SOLAR, KING), use the pre-calculated speed
		// For CUSTOM rate, this value should be set via saveTrackingRatePreset first
		arduinoApi.startSiderealTracking(direction.arduinoValue, trackingMode.trackingSpeed)
	}.onSuccess { message ->
		_lastArduinoMessage.value = message
	}

	override suspend fun stopSideRealTracking() = tryOnline {
		arduinoApi.stopSiderealTracking()
	}.onSuccess { message ->
		_lastArduinoMessage.value = message
	}

	// Slewing Control
	override suspend fun startSlew(speed: Int, direction: SlewDirection) = tryOnline {
		arduinoApi.startSlew(speed, direction.value)
	}.onSuccess { message ->
		_lastArduinoMessage.value = message
	}

	override suspend fun stopSlew() = tryOnline {
		arduinoApi.stopSlew()
	}.onSuccess { message ->
		_lastArduinoMessage.value = message
	}

	// Goto Control
	override suspend fun gotoRA(currentRA: String, targetRA: String, speed: Int) = tryOnline {
		arduinoApi.gotoRA(currentRA, targetRA, speed)
	}.onSuccess { message ->
		_lastArduinoMessage.value = message
	}

	override suspend fun abortGotoRA() = tryOnline {
		arduinoApi.abortGotoRA()
	}.onSuccess { message ->
		_lastArduinoMessage.value = message
	}

	// Position Management
	override suspend fun setPosition(currentRA: String) = tryOnline {
		arduinoApi.setPosition(currentRA)
	}.onSuccess { message ->
		_lastArduinoMessage.value = message
	}

	override suspend fun getCurrentPosition() = tryOnline {
		arduinoApi.getCurrentPosition()
	}

	// Intervalometer Control
	override suspend fun setCurrent(
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
	) = tryOnline {
		arduinoApi.setCurrent(
			mode = mode,
			preset = preset,
			captureMode = captureMode.value,
			exposureTime = exposureTime,
			exposures = exposures,
			preDelay = preDelay,
			delay = delay,
			frames = frames,
			panAngle = panAngle,
			panDirection = panDirection,
			enableTracking = enableTracking,
			ditherChoice = ditherChoice,
			ditherFrequency = ditherFrequency,
			focalLength = focalLength,
			pixelSize = pixelSize
		)
	}.onSuccess { message ->
		_lastArduinoMessage.value = message
	}

	override suspend fun readPreset(preset: Int) = tryOnline {
		arduinoApi.readPreset(preset)
	}

	override suspend fun abortCapture() = tryOnline {
		arduinoApi.abortCapture()
	}.onSuccess { message ->
		_lastArduinoMessage.value = message
	}

	// Tracking Rates
	override suspend fun getTrackingRates() = tryOnline {
		arduinoApi.getTrackingRates()
	}

	override suspend fun saveTrackingRatePreset(preset: Int, type: Int, customRate: Int) = tryOnline {
		arduinoApi.saveTrackingRatePreset(preset, type, customRate)
	}.onSuccess { message ->
		_lastArduinoMessage.value = message
	}

	override suspend fun loadTrackingRatePreset(preset: Int) = tryOnline {
		arduinoApi.loadTrackingRatePreset(preset)
	}

	// Status & Info
	override suspend fun getStatus(showInUI: Boolean) = tryOnline {
		Timber.d("ArduinoRepositoryImpl: calling /status (showInUI=$showInUI)")
		val resp = arduinoApi.getStatus()
		Timber.d("ArduinoRepositoryImpl: /status response raw: $resp")
		resp
	}

	override suspend fun resetLastMessage() {
		_lastArduinoMessage.value = null
	}

	override suspend fun getVersion() = tryOnline {
		Timber.d("ArduinoRepositoryImpl: calling /version")
		val v = arduinoApi.getVersion()
		Timber.d("ArduinoRepositoryImpl: /version response raw: $v")
		v
	}

	// Catalog Search
	override suspend fun starSearch(catalog: Int, query: String) = tryOnline {
		arduinoApi.starSearch(catalog, query)
	}.onSuccess { message ->
		_lastArduinoMessage.value = message
	}

	// Settings
	override suspend fun setLanguage(lang: Int) = tryOnline {
		arduinoApi.setLanguage(lang)
	}.onSuccess { message ->
		_lastArduinoMessage.value = message
	}
}
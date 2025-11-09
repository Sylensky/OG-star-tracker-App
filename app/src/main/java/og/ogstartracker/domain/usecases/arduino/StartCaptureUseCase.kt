package og.ogstartracker.domain.usecases.arduino

import og.ogstartracker.domain.models.CaptureMode
import og.ogstartracker.domain.usecases.base.ResourceSuspendUseCase
import og.ogstartracker.repository.ArduinoRepository

class StartCaptureUseCase constructor(
	private val repository: ArduinoRepository
) : ResourceSuspendUseCase<StartCaptureUseCase.Input, String> {

	override suspend fun invoke(input: Input) = repository.setCurrent(
		mode = input.mode,
		preset = input.preset,
		captureMode = input.captureMode,
		exposureTime = input.exposureTime,
		exposures = input.exposures,
		preDelay = input.preDelay,
		delay = input.delay,
		frames = input.frames,
		panAngle = input.panAngle,
		panDirection = input.panDirection,
		enableTracking = input.enableTracking,
		ditherChoice = input.ditherChoice,
		ditherFrequency = input.ditherFrequency,
		focalLength = input.focalLength,
		pixelSize = input.pixelSize
	)

	data class Input constructor(
		val mode: Int = 0, // 0=start capture, 1=save to preset
		val preset: Int = 0, // Preset number (0-4)
		val captureMode: CaptureMode = CaptureMode.LONG_EXPOSURE_STILL,
		val exposureTime: Int,
		val exposures: Int,
		val preDelay: Int = 5,
		val delay: Int = 2,
		val frames: Int = 1,
		val panAngle: Int = 0, // Pan angle in degrees × 100
		val panDirection: Int = 1, // 0=left/west, 1=right/east
		val enableTracking: Int = 1,
		val ditherChoice: Int,
		val ditherFrequency: Int = 1, // Dither every N exposures
		val focalLength: Int,
		val pixelSize: Int, // Pixel size in microns × 100
	)
}
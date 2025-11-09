package og.ogstartracker.domain.models

enum class CaptureMode(val value: Int) {
	LONG_EXPOSURE_STILL(0),
	LONG_EXPOSURE_MOVIE(1),
	TIMELAPSE(2),
	TIMELAPSE_PAN(3)
}

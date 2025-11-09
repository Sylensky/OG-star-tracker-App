package og.ogstartracker.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PresetResponse(
	@Json(name = "mode") val mode: Int,
	@Json(name = "exposures") val exposures: Int,
	@Json(name = "delay") val delay: Int,
	@Json(name = "preDelay") val preDelay: Int,
	@Json(name = "exposureTime") val exposureTime: Int,
	@Json(name = "panAngle") val panAngle: Int,
	@Json(name = "panDirection") val panDirection: Int,
	@Json(name = "ditherChoice") val ditherChoice: Int,
	@Json(name = "ditherFrequency") val ditherFrequency: Int,
	@Json(name = "enableTracking") val enableTracking: Int,
	@Json(name = "frames") val frames: Int,
	@Json(name = "pixelSize") val pixelSize: Int,
	@Json(name = "focalLength") val focalLength: Int
)

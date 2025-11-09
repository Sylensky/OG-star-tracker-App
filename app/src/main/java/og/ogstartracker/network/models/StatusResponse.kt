package og.ogstartracker.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatusResponse(
	@Json(name = "slewActive") val slewActive: Boolean,
	@Json(name = "trackingActive") val trackingActive: Boolean,
	@Json(name = "intervalometerActive") val intervalometerActive: Boolean,
	@Json(name = "goToTarget") val goToTarget: Boolean,
	@Json(name = "exposuresTaken") val exposuresTaken: Int,
	@Json(name = "currentExposure") val currentExposure: Int
)

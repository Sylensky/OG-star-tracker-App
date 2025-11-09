package og.ogstartracker.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PositionResponse(
	@Json(name = "ra") val ra: Long,
	@Json(name = "utcTime") val utcTime: String? = null,
	@Json(name = "longitude") val longitude: Double? = null
)

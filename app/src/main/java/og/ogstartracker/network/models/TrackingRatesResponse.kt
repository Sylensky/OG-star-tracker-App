package og.ogstartracker.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TrackingRatesResponse(
	@Json(name = "type") val type: Int, // 0=SIDEREAL, 1=LUNAR, 2=SOLAR, 3=CUSTOM
	@Json(name = "customRate") val customRate: Int
)

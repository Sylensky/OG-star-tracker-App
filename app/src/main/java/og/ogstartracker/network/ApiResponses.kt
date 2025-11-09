package og.ogstartracker.network

import com.squareup.moshi.Json

/**
 * Response from /status endpoint
 */
data class StatusResponse(
	@Json(name = "slewActive") val slewActive: Boolean,
	@Json(name = "trackingActive") val trackingActive: Boolean,
	@Json(name = "intervalometerActive") val intervalometerActive: Boolean,
	@Json(name = "goToTarget") val goToTarget: Boolean,
	@Json(name = "exposuresTaken") val exposuresTaken: Int,
	@Json(name = "currentExposure") val currentExposure: Int,
)

/**
 * Response from /version endpoint
 */
data class VersionResponse(
	@Json(name = "version") val version: String,
	@Json(name = "buildDate") val buildDate: String,
)

/**
 * Response from /getTrackingRates endpoint
 * Types: 0=SIDEREAL, 1=LUNAR, 2=SOLAR, 3=KING, 4=CUSTOM
 */
data class TrackingRatesResponse(
	@Json(name = "type") val type: Int,
	@Json(name = "customRate") val customRate: Int,
)

/**
 * Response from /getCurrentPosition endpoint
 */
data class PositionResponse(
	@Json(name = "position") val position: Long,
)

/**
 * Response from /starSearch endpoint
 */
data class CatalogSearchResponse(
	@Json(name = "results") val results: List<CatalogResult>,
)

/**
 * Individual catalog search result
 */
data class CatalogResult(
	@Json(name = "name") val name: String,
	@Json(name = "ra") val ra: String,
	@Json(name = "dec") val dec: String,
	@Json(name = "magnitude") val magnitude: Float? = null,
)

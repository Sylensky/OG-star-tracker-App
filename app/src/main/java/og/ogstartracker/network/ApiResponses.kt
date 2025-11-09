package og.ogstartracker.network

import com.google.gson.annotations.SerializedName

/**
 * Response from /status endpoint
 */
data class StatusResponse(
	@SerializedName("slewActive") val slewActive: Boolean,
	@SerializedName("trackingActive") val trackingActive: Boolean,
	@SerializedName("intervalometerActive") val intervalometerActive: Boolean,
	@SerializedName("goToTarget") val goToTarget: Boolean,
	@SerializedName("exposuresTaken") val exposuresTaken: Int,
	@SerializedName("currentExposure") val currentExposure: Int,
)

/**
 * Response from /version endpoint
 */
data class VersionResponse(
	@SerializedName("version") val version: String,
	@SerializedName("buildDate") val buildDate: String,
)

/**
 * Response from /getTrackingRates endpoint
 * Types: 0=SIDEREAL, 1=LUNAR, 2=SOLAR, 3=KING, 4=CUSTOM
 */
data class TrackingRatesResponse(
	@SerializedName("type") val type: Int,
	@SerializedName("customRate") val customRate: Int,
)

/**
 * Response from /getCurrentPosition endpoint
 */
data class PositionResponse(
	@SerializedName("position") val position: Long,
)

/**
 * Response from /starSearch endpoint
 */
data class CatalogSearchResponse(
	@SerializedName("results") val results: List<CatalogResult>,
)

/**
 * Individual catalog search result
 */
data class CatalogResult(
	@SerializedName("name") val name: String,
	@SerializedName("ra") val ra: String,
	@SerializedName("dec") val dec: String,
	@SerializedName("magnitude") val magnitude: Float? = null,
)

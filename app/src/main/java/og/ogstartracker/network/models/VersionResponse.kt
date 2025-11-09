package og.ogstartracker.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VersionResponse(
	@Json(name = "version") val version: String,
	@Json(name = "buildDate") val buildDate: String
)

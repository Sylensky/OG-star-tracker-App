package og.ogstartracker.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import og.ogstartracker.Config
import timber.log.Timber

object WiFiHelper {

	/**
	 * Check if the device is connected to a WiFi network matching the OG Star Tracker pattern
	 */
	fun isConnectedToTracker(context: Context): Boolean {
		val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
			?: return false

		if (!hasWiFiPermissions(context)) {
			Timber.w("WiFi permissions not granted")
			return false
		}

		val connectionInfo = wifiManager.connectionInfo
		val ssid = connectionInfo.ssid

		return isTrackerSSID(ssid)
	}

	/**
	 * Check if the given SSID matches the OG Star Tracker pattern
	 */
	fun isTrackerSSID(ssid: String?): Boolean {
		if (ssid.isNullOrBlank() || ssid == Config.WIFI_SSID_UNKNOWN) {
			Timber.d("WiFi: SSID is null/blank or unknown: '$ssid'")
			return false
		}

		// Remove quotes from SSID
		val cleanSSID = ssid.replace("\"", "")
		Timber.d("WiFi: Checking SSID: '$cleanSSID' (original: '$ssid')")

		// Check for new pattern: "OG StarTracker#XXXX" or alternate with space "OG Star Tracker#XXXX"
		if (cleanSSID.startsWith(Config.WIFI_SSID_PREFIX) || cleanSSID.startsWith(Config.WIFI_SSID_PREFIX_ALTERNATE)) {
			Timber.d("WiFi: SSID matches tracker pattern (with identifier)")
			return true
		}

		// Check for legacy pattern: exact "OG Star Tracker" (no identifier)
		if (cleanSSID == "OG Star Tracker") {
			Timber.d("WiFi: SSID matches legacy tracker pattern")
			return true
		}

		Timber.d("WiFi: SSID does NOT match any tracker pattern. Prefix='${Config.WIFI_SSID_PREFIX}', Alternate='${Config.WIFI_SSID_PREFIX_ALTERNATE}'")
		return false
	}

	/**
	 * Get the currently connected tracker SSID, or null if not connected to a tracker
	 */
	fun getConnectedTrackerSSID(context: Context): String? {
		val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
			?: return null

		if (!hasWiFiPermissions(context)) {
			return null
		}

		val connectionInfo = wifiManager.connectionInfo
		val ssid = connectionInfo.ssid?.replace("\"", "")

		return if (isTrackerSSID(ssid)) ssid else null
	}

	/**
	 * Scan for available OG Star Tracker networks
	 * Note: This requires location permissions and WiFi scanning to be enabled
	 */
	fun scanForTrackers(context: Context): List<String> {
		val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
			?: return emptyList()

		if (!hasWiFiPermissions(context)) {
			Timber.w("WiFi scanning permissions not granted")
			return emptyList()
		}

		// Start WiFi scan
		val scanStarted = wifiManager.startScan()
		if (!scanStarted) {
			Timber.w("WiFi scan could not be started")
		}

		// Get scan results
		val scanResults = wifiManager.scanResults
		
		return scanResults
			.mapNotNull { it.SSID }
			.filter { isTrackerSSID(it) }
			.distinct()
			.sorted()
	}

	/**
	 * Check if the app has the necessary WiFi permissions
	 */
	private fun hasWiFiPermissions(context: Context): Boolean {
		val hasAccessWifiState = ContextCompat.checkSelfPermission(
			context,
			Manifest.permission.ACCESS_WIFI_STATE
		) == PackageManager.PERMISSION_GRANTED

		// For Android 10+ (API 29+), we need location permission for WiFi scanning
		val hasLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			ContextCompat.checkSelfPermission(
				context,
				Manifest.permission.ACCESS_FINE_LOCATION
			) == PackageManager.PERMISSION_GRANTED
		} else {
			true
		}

		return hasAccessWifiState && hasLocationPermission
	}

	/**
	 * Extract the unique identifier from a tracker SSID
	 * Example: "OG StarTracker#12ab" -> "12ab"
	 */
	fun extractTrackerIdentifier(ssid: String): String? {
		val cleanSSID = ssid.replace("\"", "")
		return when {
			cleanSSID.startsWith(Config.WIFI_SSID_PREFIX) -> cleanSSID.removePrefix(Config.WIFI_SSID_PREFIX)
			cleanSSID.startsWith(Config.WIFI_SSID_PREFIX_ALTERNATE) -> cleanSSID.removePrefix(Config.WIFI_SSID_PREFIX_ALTERNATE)
			else -> null
		}
	}
}

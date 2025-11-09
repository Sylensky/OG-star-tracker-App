package og.ogstartracker

object Config {
	// Firmware compatibility
	// This app supports REST API v2.1.0-beta03+ (internal versions 7, 8, and later)
	// No explicit version checking is enforced to allow flexibility
	
	const val SLEW_MIN_VALUE = 2 // Updated range: 2-400, lower=faster
	const val SLEW_MAX_VALUE = 400

	// WiFi SSID patterns for OG Star Tracker with unique identifier
	// Primary (no space): "OG StarTracker#xxxx"
	const val WIFI_SSID_PREFIX = "OG StarTracker#"
	// Alternate (with space): "OG Star Tracker#xxxx" — accept both for compatibility
	const val WIFI_SSID_PREFIX_ALTERNATE = "OG Star Tracker#"
	const val WIFI_SSID = "\"OG Star Tracker\"" // Legacy support (no identifier)
	const val WIFI_SSID_UNKNOWN = "<unknown ssid>" // returned when the app is not running

	const val STATUS_TRACKING_ON = "Tracking ON"
	const val STATUS_IDLE = "Idle"
	const val STATUS_CAPTURING = "Captures Remaining"

	const val CAPTURING_INITIAL_DELAY = 3000L
	const val CHECK_WIFI_DURATION = 5_000L

	const val SCREEN_DASHBOARD = "dashboard"
	const val SCREEN_SETTINGS = "settings"

	const val PREFERENCES_VIBRATIONS = "vibrations"
	const val PREFERENCES_HEMISPHERE = "hemisphere"
	const val PREFERENCES_TRACKING_MODE = "tracking_mode"
	const val PREFERENCES_SELECTED_TRACKER_SSID = "selected_tracker_ssid"
}
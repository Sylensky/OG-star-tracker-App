package og.ogstartracker.domain.models

import androidx.annotation.StringRes
import og.ogstartracker.R

/**
 * Tracking rate types supported by the tracker firmware.
 * 
 * The trackingSpeed values are timer period values in microseconds that the firmware expects.
 * These values are for a 1.8° stepper motor (STEPPER_1_8 in firmware config).
 * For 0.9° steppers, these values would be halved.
 * 
 * API usage: GET /on?direction=<0|1>&trackingSpeed=<value>
 * The firmware expects the actual timer period value, not an enum index.
 */
enum class TrackingMode constructor(
	val arduinoValue: Int,
	@StringRes val text: Int,
	val trackingSpeed: Int // Timer period in microseconds for firmware (1.8° stepper)
) {
	SIDEREAL(0, R.string.settings_tracking_mode_sidereal, 5318765), // 23h 56m 4s - Standard stellar tracking
	LUNAR(1, R.string.settings_tracking_mode_lunar, 5447735),       // 24h 31m - Moon tracking  
	SOLAR(2, R.string.settings_tracking_mode_solar, 5333333),       // 24h - Sun tracking
	CUSTOM(3, R.string.settings_tracking_mode_custom, 0);           // Custom rate (rate set separately via saveTrackingRatePreset)

	companion object {
		fun fromValue(value: Int): TrackingMode = entries.find { it.arduinoValue == value } ?: SIDEREAL
	}
}
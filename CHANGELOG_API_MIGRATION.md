# CHANGELOG - OG Star Tracker App

## [Unreleased] - API v2.1.0-beta03 Migration

### 🚨 Breaking Changes

#### Hemisphere Direction Values **REVERSED**
- **CRITICAL:** Northern hemisphere changed from `1` to `0`
- **CRITICAL:** Southern hemisphere changed from `0` to `1`
- Matches new API specification (0=northern/clockwise, 1=southern/counter-clockwise)
- **Impact:** Existing hemisphere settings will work correctly, but internal values are reversed

#### API Response Format Changes
- `/status` endpoint now returns JSON `StatusResponse` instead of plain text
- `/version` endpoint now returns JSON `VersionResponse` instead of plain text
- **Impact:** Old firmware versions will not work with this app version

#### Tracking API Changes
- `startSiderealTracking()` now requires `trackingSpeed` parameter
- Speed automatically calculated from `TrackingMode` (SIDEREAL, SOLAR, LUNAR)
- **Impact:** Tracking calls now include precise speed values

#### Slewing API Unified
- Replaced `/left?speed=X` and `/right?speed=X` with single `/startslew?speed=X&direction=Y`
- Speed range changed: now `2-400` (was `0-5`)
- Lower values = faster movement (reversed logic)
- **Impact:** Slew control behaves the same but uses different underlying API

#### Capture/Intervalometer Complete Overhaul
- Old 6-parameter `/start` endpoint replaced with 15-parameter `/setCurrent`
- Parameters renamed: `exposure` → `exposureTime`, `numExposures` → `exposures`, `pixSize` → `pixelSize`
- Dithering now uses `ditherChoice` + `ditherFrequency` instead of simple `ditherEnabled`
- Added new parameters: `mode`, `preset`, `captureMode`, `preDelay`, `delay`, `frames`, `panAngle`, `panDirection`
- Logic inverted: `disableTrackingOnEnd` → `enableTracking` (opposite meaning)
- **Impact:** All capture operations use new comprehensive parameter set

### ✨ New Features

#### WiFi Network Discovery
- Added automatic detection of OG Star Tracker devices with unique identifiers
- Supports new SSID pattern: `OG StarTracker#XXXX` (where XXXX is unique device ID)
- Backward compatible with legacy SSID: `OG Star Tracker`
- New `WiFiHelper` utility class with:
  - `scanForTrackers()` - Find all available tracker networks
  - `isConnectedToTracker()` - Check connection status
  - `getConnectedTrackerSSID()` - Get current tracker SSID
  - `extractTrackerIdentifier()` - Extract device unique ID
- Added `WiFiScannerViewModel` for UI integration
- Preference to remember selected tracker SSID

#### Capture Modes
- **NEW:** `LONG_EXPOSURE_STILL` - Standard long exposure photography
- **NEW:** `LONG_EXPOSURE_MOVIE` - Long exposure movie mode
- **NEW:** `TIMELAPSE` - Timelapse photography
- **NEW:** `TIMELAPSE_PAN` - Timelapse with automatic panning

#### Goto Control (API Ready, UI Not Implemented)
- `/gotoRA` - Move mount to target RA position with speed control
- `/abort-goto-ra` - Cancel ongoing goto operation
- Parameters: `currentRA`, `targetRA`, `speed` (HH:MM:SS format)

#### Position Management (API Ready, UI Not Implemented)
- `/setPosition` - Set current mount position
- `/getCurrentPosition` - Get position in steps (returns JSON)

#### Preset System (API Ready, UI Not Implemented)
- **Intervalometer Presets:** Save/load capture configurations (5 presets: 0-4)
- **Tracking Rate Presets:** Save/load tracking rates (5 presets: 0-4)
- `/readPreset` - Load intervalometer preset
- `/saveTrackingRatePreset` - Save tracking rate
- `/loadTrackingRatePreset` - Load tracking rate

#### Star Catalog Search (API Ready, UI Not Implemented)
- `/starSearch` - Search celestial object catalogs
- Supported catalogs:
  - NGC2000 (full)
  - NGC2000_COMPACT
  - BSC5 (Bright Star Catalog)
  - BSC5_COMPACT

#### Enhanced Status Information
- Detailed status with multiple boolean flags:
  - `trackingActive` - Sidereal tracking state
  - `intervalometerActive` - Capture sequence state
  - `slewActive` - Manual slewing state
  - `goToTarget` - Goto operation state
  - `exposuresTaken` - Completed exposures count
  - `currentExposure` - Active exposure number

#### Firmware Version Display
- Now shows full version with build date: "2.1.0 (2025-11-07)"
- Version information displayed in Settings screen

### 🔧 Technical Improvements

#### Network Layer
- Added Moshi JSON parsing library
- Configured Retrofit with MoshiConverterFactory
- Created comprehensive data models with `@JsonClass` annotations
- Proper handling of mixed response types (JSON + plain text)

#### Repository Pattern
- Updated `ArduinoRepository` interface with all new methods
- Implemented new endpoints in `ArduinoRepositoryImpl`
- Added proper type safety with new domain models

#### Use Cases
- Updated `StartCaptureUseCase` with comprehensive input parameters
- Modified `GetCurrentStateUseCase` to return structured `StatusResponse`
- Updated `GetVersionUseCase` to return `VersionResponse` with build date
- Refactored slew use cases to use unified API

#### Domain Models
- Created `CaptureMode` enum for capture types
- Created `SlewDirection` enum for movement direction
- Enhanced `TrackingMode` with built-in speed calculations
- Fixed `Hemisphere` direction values to match API spec

#### Configuration
- Updated slew speed range: `SLEW_MIN_VALUE = 2`, `SLEW_MAX_VALUE = 400`
- Added WiFi SSID pattern constants
- Added preference key for selected tracker SSID

### 🐛 Bug Fixes
- Fixed hemisphere direction values (were reversed)
- Corrected slew speed range to match API specification
- Proper WiFi connection detection with pattern matching

### 📚 Documentation
- Created `API_MIGRATION_SUMMARY.md` - Complete migration guide
- Created `API_QUICK_REFERENCE.md` - Developer quick reference
- Documented all breaking changes and migration paths
- Added usage examples for all new features

### 🔨 Dependencies
- Added `com.squareup.moshi:moshi:1.15.0`
- Added `com.squareup.moshi:moshi-kotlin:1.15.0`
- Already had `com.squareup.retrofit2:converter-moshi:2.9.0`

### 📱 UI Updates
- Updated connection hint text to show new SSID pattern
- Enhanced version display in Settings (shows version + build date)
- WiFi connection checking now supports dynamic SSIDs

### ⚠️ Known Limitations
- Goto control UI not yet implemented (API ready)
- Position management UI not yet implemented (API ready)
- Preset management UI not yet implemented (API ready)
- Star catalog search UI not yet implemented (API ready)
- WiFi network selection dialog not yet implemented (ViewModel ready)

### 🧪 Testing Requirements
- [ ] Test with tracker firmware v2.1.0-beta03 or higher
- [ ] Verify tracking starts with correct hemisphere and speed
- [ ] Test slew control in both directions
- [ ] Verify capture with all new parameters
- [ ] Test WiFi detection with new SSID pattern
- [ ] Verify status JSON parsing
- [ ] Check version display with build date
- [ ] Test dithering with frequency parameter
- [ ] Verify timelapse pan mode (if firmware supports)

### 🔐 Permissions
- No new permissions required
- Uses existing:
  - `ACCESS_WIFI_STATE` - WiFi connection info
  - `ACCESS_FINE_LOCATION` - WiFi scanning (Android 10+)
  - `ACCESS_NETWORK_STATE` - Network connectivity

### 📋 Migration Guide for Developers

#### Updating Tracking Code
```kotlin
// OLD: direction only
repository.startSideRealTracking(hemisphere)

// NEW: direction + speed
repository.startSideRealTracking(hemisphere, trackingMode)
// Speed automatically calculated from trackingMode
```

#### Updating Capture Code
```kotlin
// OLD: 6 parameters
StartCaptureUseCase.Input(
    exposure = 30,
    numExposures = 10,
    focalLength = 200,
    pixSize = 350,
    ditherEnabled = 1,
    disableTrackingOnEnd = 0
)

// NEW: 15 parameters
StartCaptureUseCase.Input(
    mode = 0,
    preset = 0,
    captureMode = CaptureMode.LONG_EXPOSURE_STILL,
    exposureTime = 30,
    exposures = 10,
    preDelay = 5,
    delay = 2,
    frames = 1,
    panAngle = 0,
    panDirection = 1,
    enableTracking = 1,
    ditherChoice = 1,
    ditherFrequency = 3,
    focalLength = 200,
    pixelSize = 350
)
```

#### Updating Status Handling
```kotlin
// OLD: String response
getCurrentState().onSuccess { status: String ->
    val isTracking = status == STATUS_TRACKING_ON
}

// NEW: Structured response
getCurrentState().onSuccess { status: StatusResponse ->
    val isTracking = status.trackingActive
    val isCapturing = status.intervalometerActive
    val exposureCount = status.exposuresTaken
}
```

### 🎯 Compatibility
- **Minimum Android:** 8.0 (API 26) - unchanged
- **Target Android:** 14 (API 34) - unchanged
- **Minimum Firmware:** v2.1.0-beta03+ - **REQUIRED**
- **Backward Compatibility:** WiFi SSID detection supports legacy firmware

---

## Previous Versions
[Previous changelog entries would go here]

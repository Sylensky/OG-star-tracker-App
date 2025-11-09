# OG Star Tracker - Quick API Reference Guide

## Common Usage Examples

### 1. Start Tracking
```kotlin
// Tracking now requires speed from TrackingMode
useCases.startSiderealTracking() // Automatically uses current hemisphere & mode
// Internally: repository.startSideRealTracking(hemisphere, trackingMode)
// API call: GET /on?direction=0&trackingSpeed=15956
```

### 2. Stop Tracking
```kotlin
useCases.stopSiderealTracking()
// API call: GET /off
```

### 3. Slew Control (Left/Right)
```kotlin
// Left - speed range: 2-400 (lower = faster)
useCases.trackerLeft(speed) // Now uses startSlew internally
// API call: GET /startslew?speed=16&direction=0

// Right
useCases.trackerRight(speed)
// API call: GET /startslew?speed=16&direction=1

// Stop slewing
repository.stopSlew()
// API call: GET /stopslew
```

### 4. Start Capture (Intervalometer)
```kotlin
useCases.startCapture(
    StartCaptureUseCase.Input(
        mode = 0, // 0=start capture, 1=save to preset
        preset = 0, // 0-4
        captureMode = CaptureMode.LONG_EXPOSURE_STILL,
        exposureTime = 30, // seconds
        exposures = 10,
        preDelay = 5,
        delay = 2,
        frames = 1,
        panAngle = 0, // degrees × 100
        panDirection = 1, // 0=left, 1=right
        enableTracking = 1,
        ditherChoice = 1, // 0=off, 1=on
        ditherFrequency = 3, // dither every N exposures
        focalLength = 200, // mm
        pixelSize = 350, // microns × 100
    )
)
// API call: GET /setCurrent?mode=0&preset=0&captureMode=0&exposureTime=30...
```

### 5. Get Status
```kotlin
useCases.getCurrentState(GetCurrentStateUseCase.Input(showInUI = false))
    .onSuccess { statusResponse ->
        // statusResponse.trackingActive: Boolean
        // statusResponse.intervalometerActive: Boolean
        // statusResponse.slewActive: Boolean
        // statusResponse.goToTarget: Boolean
        // statusResponse.exposuresTaken: Int
        // statusResponse.currentExposure: Int
    }
// API call: GET /status
// Returns: JSON StatusResponse
```

### 6. Get Version
```kotlin
useCases.getVersion()
    .onSuccess { versionResponse ->
        // versionResponse.version: String (e.g., "2.1.0")
        // versionResponse.buildDate: String (e.g., "2025-11-07")
    }
// API call: GET /version
// Returns: JSON VersionResponse
```

### 7. WiFi Connection Check
```kotlin
// Check if connected to any tracker
val isConnected = WiFiHelper.isConnectedToTracker(context)

// Get connected tracker SSID
val ssid = WiFiHelper.getConnectedTrackerSSID(context)
// Returns: "OG StarTracker#12ab" or null

// Scan for available trackers
val trackers = WiFiHelper.scanForTrackers(context)
// Returns: List<String> of matching SSIDs

// Check if specific SSID is a tracker
val isTracker = WiFiHelper.isTrackerSSID("OG StarTracker#12ab") // true
val isTracker2 = WiFiHelper.isTrackerSSID("OG Star Tracker") // true (legacy)
```

## New API Endpoints (Not Yet Used in UI)

### Goto RA Position
```kotlin
repository.gotoRA(
    currentRA = "12:30:45", // HH:MM:SS format
    targetRA = "14:20:30",
    speed = 8 // 2-400, lower=faster
)
// API call: GET /gotoRA?currentRA=12:30:45&targetRA=14:20:30&speed=8
```

### Position Management
```kotlin
// Set position
repository.setPosition(currentRA = "12:30:45")
// API call: GET /setPosition?currentRA=12:30:45

// Get position
repository.getCurrentPosition()
    .onSuccess { position ->
        // position.position: Long (steps)
    }
// API call: GET /getCurrentPosition
// Returns: JSON PositionResponse
```

### Preset Management
```kotlin
// Read intervalometer preset
repository.readPreset(preset = 0)
    .onSuccess { presetData ->
        // presetData.exposureTime, exposures, delay, etc.
    }
// API call: GET /readPreset?preset=0
// Returns: JSON PresetResponse

// Save tracking rate preset
repository.saveTrackingRatePreset(
    preset = 0,
    type = 0, // 0=SIDEREAL, 1=LUNAR, 2=SOLAR, 3=CUSTOM
    customRate = 0
)
// API call: GET /saveTrackingRatePreset?preset=0&type=0&customRate=0

// Load tracking rate preset
repository.loadTrackingRatePreset(preset = 0)
// API call: GET /loadTrackingRatePreset?preset=0
// Returns: JSON TrackingRatesResponse
```

### Star Catalog Search
```kotlin
repository.starSearch(
    catalog = 0, // 0=NGC2000, 1=NGC2000_COMPACT, 2=BSC5, 3=BSC5_COMPACT
    query = "M31"
)
// API call: GET /starSearch?catalog=0&query=M31
```

## Enums Reference

### CaptureMode
- `LONG_EXPOSURE_STILL(0)` - Standard long exposure
- `LONG_EXPOSURE_MOVIE(1)` - Long exposure movie mode
- `TIMELAPSE(2)` - Timelapse photography
- `TIMELAPSE_PAN(3)` - Timelapse with panning

### SlewDirection
- `LEFT(0)` - Left/West
- `RIGHT(1)` - Right/East

### TrackingMode (with speeds)
- `SIDEREAL(0, 15956)` - 23h 56m 4s
- `SOLAR(1, 15000)` - 24h
- `LUNAR(2, 14529)` - 24h 31m

### Hemisphere
- `NORTH(0)` - Northern hemisphere (clockwise)
- `SOUTH(1)` - Southern hemisphere (counter-clockwise)

## Important Notes

### Scaled Values
Some parameters are scaled by 100 to avoid floating point in URLs:
- `pixelSize`: microns × 100 (e.g., 3.5µm = 350)
- `panAngle`: degrees × 100 (e.g., 45° = 4500)

### Speed Convention
For slewing and goto:
- Range: 2-400
- Lower values = FASTER movement
- Higher values = SLOWER movement

### Direction Convention
For all directional parameters:
- 0 = left/west
- 1 = right/east

### WiFi SSID Patterns
App supports both:
- New: "OG StarTracker#XXXX" (XXXX = unique device ID)
- Legacy: "OG Star Tracker" (for older firmware)

### Preset Numbers
All presets are numbered 0-4 (5 presets total):
- Intervalometer presets
- Tracking rate presets

## Migration From Old API

| Old Parameter | New Parameter | Notes |
|--------------|---------------|-------|
| `direction` (1=north) | `direction` (0=north) | **REVERSED!** |
| No tracking speed | `trackingSpeed` required | From TrackingMode enum |
| `/left?speed=X` | `/startslew?direction=0&speed=X` | Unified endpoint |
| `/right?speed=X` | `/startslew?direction=1&speed=X` | Unified endpoint |
| `exposure` | `exposureTime` | Renamed |
| `numExposures` | `exposures` | Renamed |
| `pixSize` | `pixelSize` | Renamed |
| `ditherEnabled` | `ditherChoice` | With `ditherFrequency` |
| `disableTrackingOnEnd` | `enableTracking` (inverted) | Logic reversed |
| `/status` (text) | `/status` (JSON) | Now returns structured data |
| `/version` (text) | `/version` (JSON) | Now includes build date |

## Error Handling

All API calls return `Resource<T>` wrapper:
```kotlin
result.onSuccess { data ->
    // Handle success
}.onError { error ->
    // Handle error
}
```

Status checking:
```kotlin
when {
    result.isSuccess() -> // Success
    result.isError() -> // Error
    result.isLoading() -> // Loading
    result.isNotStarted() -> // Not started
}
```

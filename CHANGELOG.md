# Changelog

## [Unreleased]

## [1.0.6-beta01]

### Added
- implement all new REST API endpoints from firmware feature/api-handler branch
- add tracking rate management endpoints (getTrackingRates, saveTrackingRatePreset, loadTrackingRatePreset)
- add goto control endpoints (gotoRA, abortGotoRA, setPosition, getCurrentPosition)
- add catalog search endpoint (starSearch)
- add new intervalometer endpoint (setCurrent) with full parameter set including pan angle, dither settings, and preset management
- add setLanguage endpoint for web interface language control
- add structured JSON responses for status and version endpoints with detailed information
- add exposure progress display in notification (e.g., "Capturing (5/10)")

### Changed
- update WiFi SSID pattern to support unique identifiers (e.g., "OG Star Tracker#12ab")
- update tracker base URL to use static IP address (192.168.4.1) for Android compatibility (mDNS not natively supported)
- update status endpoint to return structured JSON with slewActive, trackingActive, intervalometerActive, goToTarget, exposuresTaken, and currentExposure
- update version endpoint to return JSON with version and buildDate
- improve API parameter naming consistency (trackingSpeed instead of tspeed)
- update HardwareStatusService to use new SSID prefix pattern matching
- deprecate turnLeft/turnRight endpoints in favor of unified startSlew with direction parameter

### Fixed
- fix SSID checking in notification service to properly handle dynamic tracker identifiers

## [1.0.5-beta03]

- slewing now support picking from multiple speed multipliers

## [1.0.5-beta02]

- added foreground notification which shows status of the tracker even when the Android app is killed
  -  these states are: **Idle**, **Tracking ON** and **X Captures Remaining...**
- added new optional notification permission for the foreground notification
- intervalometer is now ending with arduino code and not the Android app
- changes in hemisphere and tracking mode are applied immediately after returning from settings screen
- fix pixel size value not saving properly
- make vibrations enabled by default

## [1.0.5-beta01]

- add multiple tracking modes (sidereal, solar and lunar)
- fixed bug when hemisphere setting was ignored (sorry about that)
- add option to disable tracking when intervalometer finishes
- tracking info on main screen now shows current settings for hemisphere and tracking mode

## [1.0.4]

- add new exposition tester feature

## [1.0.4]

- add new exposition tester feature

## [1.0.3]

- fix crash when inserting wrong separator on pixel size

## [1.0.2]

- fix time format on Photo Control start time
- fix crash when settings screen visited while not connected to the tracker
- tweak some texts

## [1.0.1]

- fix start time in photo control when capturing
- add one more item in changelog

## [1.0.0]

- first release of Android app
- contains sidereal tracking, slew control, checklist, setting and photo control with dithering
- compatible with firmware version 2

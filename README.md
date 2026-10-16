# TaxiMeterCore — Android core prototype

> **Current source status (0.8.3):** The Persian driver Activity is integrated with the Room database and location foreground service. The screen now keeps its attached view hierarchy and reconciles matching views in place, updates only while visible, shows passenger boarding/alighting times, and presents payment methods as prominent buttons. Android 6 is targeted through `minSdk = 23`. The payment options for cash/barcode/e-wallet/citizen card are UI placeholders, and the Bluetooth POS gateway is not configured. This source has **not** been compiled with the Android SDK or installed on a device in this environment; run the repository GitHub Actions workflow before using it.

This repository is an early **core prototype**, not yet a driver-ready taxi-meter application. It establishes the data model and transactional trip lifecycle for a rotating/shared taxi with up to four simultaneous passenger trips.

## Decisions encoded

- Currency is Iranian toman.
- Each passenger has an independent trip ID and distance/fare.
- Each new trip snapshots the active fare configuration; later tariff changes do not rewrite active or closed trips.
- Fare rounding is to the nearest whole toman, with exact half-toman values rounded upward.
- Unknown distance during a GPS gap is not estimated automatically. The trip is closed for seat occupancy purposes but its fare remains pending review.
- Driver fare corrections require a reason and preserve the calculated fare and adjustment history.
- Stop time does not add fare in the default distance-only fare model.

## Implemented in this prototype

- Room entities and DAO for sessions, fare versions, passenger trips, GPS points, route segments, allocations, events, fare adjustments, and audit events.
- Transactional repository operations for starting a shift, boarding a passenger, ending a trip, adjusting a finalized fare, and closing a shift.
- Shared-fare calculation using integer arithmetic.
- Pure Kotlin route-segment allocation policy and unit tests. Partial segment allocation is only allowed under a configured short-duration threshold and is explicitly marked as interpolated; otherwise affected trips require review.

## Not yet implemented / not field validated

- Driver-facing Persian UI and shift/session management screens.
- Integration of `LocationPermissionController` into the final driver Activity.
- On-device field validation of GPS thresholds and battery behavior.
- Historical route fallback workflow, agreed-fare UI, charter reservation enforcement.
- Exportable shift/trip reports and on-device installation tests.

## Build environment

Open the project in Android Studio with JDK 17 and Android SDK 35 available. The source has not been built or tested in a full Android Studio environment in this delivery; run Gradle sync and unit tests before relying on it. Any compiler or Room schema errors found there must be fixed before deployment.


## GPS foreground service (prototype added in v0.3)

`LocationTrackingService` now requests high-accuracy fused locations while running as an Android location foreground service. It persists location points and route segments, allocates valid segments to overlapping passenger trips, and flags trips for review when points are inaccurate, time gaps are too large, or a segment implies an implausible speed. It does not invent distance for a gap.

### Required app integration

1. Integrate `LocationPermissionController` into a `ComponentActivity`. Call `request()` in response to the driver's action; its callback reports whether location permission was granted. It requests `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`, and optionally asks for notification permission on Android 13+. The manifest declares these permissions plus `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_LOCATION`.
2. Create an active taxi session and an active fare configuration through the app's normal workflow.
3. After permission is granted and while the Activity is visible, call `LocationTrackingService.start(context, sessionId)`.
4. When the driver ends the shift, close all active passenger trips, close the session, and call `LocationTrackingService.stop(context)`.
5. If the app process or device is restarted, verify session state and GPS continuity before accepting more distance. A long gap is marked for review.

Example integration pattern in a `ComponentActivity` (the actual driver UI is still to be built):

```kotlin
private lateinit var locationPermissions: LocationPermissionController

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    locationPermissions = LocationPermissionController(this) { granted ->
        if (granted) {
            // Call only after the app has created/recovered an active session.
            LocationTrackingService.start(this, activeSessionId)
        } else {
            // Show an explanation and let the driver retry permission setup.
        }
    }
}

// Invoke after the driver taps the start-shift/start-tracking action:
locationPermissions.request()
```

The service's initial thresholds are deliberately conservative and are not validated fare-meter accuracy claims: maximum accepted location accuracy 35 m, maximum segment gap 15 s, maximum interpolation interval 5 s, and implausible-speed flag at 55 m/s. These values must be calibrated with field tests. A valid segment is a straight-line distance between sampled coordinates, not map-matched road distance, so urban GPS conditions may still cause undercounting or noise.

The project does not yet include a complete driver Activity to request permissions and create/manage sessions, and has not been compiled or tested on a physical Android device in this environment. Treat this as a GPS integration prototype, not a certified fare meter.

## v0.4 — Persian driver UI integration

The prototype now includes a native Android `MainActivity` with a Persian right-to-left layout. It is connected to the local Room database, transactional repository, and location foreground service.

- Start a shift after defining a configurable experimental tariff and granting location permission.
- Resume GPS tracking for an existing active shift.
- Four seat cards show free/occupied status, recorded distance, and GPS review flags.
- Boarding creates a unique trip with the active tariff version. If no fresh GPS fix is available, the trip is explicitly marked for review.
- Alighting closes only the selected passenger trip. A fare is finalized only when the stored route coverage is considered complete; otherwise the app reports review required and does not guess missing distance.
- End shift is blocked while passenger trips remain active.
- Recent trips remain visible after a shift closes.
- A finalized fare can be adjusted by the driver only with a reason; the original calculated amount and adjustment history are preserved.
- Changing a tariff creates a new version for new trips; existing trips retain their stored tariff ID.

### Important limitations

This is still a development prototype, not a certified or field-validated fare meter. No full Android/Gradle build or device/emulator test could be run in the current environment. The UI's GPS coverage check is conservative but not a proof of complete road distance; field validation is required. Trips flagged for GPS review remain unresolved until the driver explicitly reviews and records a historical/manual estimate; the app does not fabricate missing distance. Agreed-fare and charter modes are represented in the data model but their complete driver workflows are not yet implemented. Reports are shown on screen but export is not yet implemented.

## Driver UI update (2026-10-12)

- The main screen has two native-view tabs: `سفر جاری` and `مدیریت شیفت و تعرفه`.
- The current-trip tab shows a cabin-style layout (driver + four passenger seats), per-passenger allocated GPS distance, and a provisional fare estimate based on that passenger trip's stored fare configuration.
- The UI refreshes every 1 second only while the Activity is visible and the current-trip tab is selected. The scroll position is restored after refresh.
- The foreground location request interval is 3 seconds (minimum update interval 2 seconds), instead of 2 seconds / 1 second. This reduces nominal request frequency; actual battery savings must be measured on target phones.
- The estimate is explicitly provisional. GPS gaps remain unresolved and the UI marks such trips for review; no missing distance is fabricated. When GPS is uncertain, the alighting flow now offers a route-matched historical suggestion or an editable manual fallback; no missing distance is fabricated.

Build validation note: this editing environment did not include Gradle or an Android SDK, so an Android build/test could not be run here. Use the repository's GitHub Actions workflow or run `gradle testDebugUnitTest assembleDebug` in an Android SDK environment before installing on a device.

## v0.6 uncertain-fare and payment UI update

- The visible trip screen refreshes once per second so the latest allocated GPS distance and rounded fare estimate are displayed promptly. Actual distance changes still depend on valid GPS segments recorded by the foreground service.
- When a passenger alights with incomplete/uncertain GPS coverage, the app offers a driver-editable estimate. It looks for up to three previous completed trips with pickup/drop-off coordinates within 300 m of the current route endpoints and uses their mean recorded distance under the passenger's original fare configuration.
- If no comparable historical route is available, the driver can enter a manual unit count; the current prototype computes `minimum fare + (unit count × 1,000 toman)`. Here the configured minimum fare is temporarily treated as the base fare; confirm this business rule before operational use.
- The initial estimate, final fare, method and reason are retained in trip/audit records. A closed trip awaiting review can be resolved later from trip history.
- A payment/send-to-Bluetooth-POS action is shown for finalized fares, but actual POS transmission is deliberately not implemented because it depends on the exact POS model and vendor SDK/protocol. The UI clearly warns that no payment has been sent.
- Build validation is still required in Android Studio/GitHub Actions; this environment has no Gradle wrapper executable or Android SDK.

## v0.7 — POS gateway contract

- Added `payment/PosPaymentGateway.kt` with a vendor-neutral payment request/result contract.
- The UI sends a request through the gateway boundary and displays a payment-approved message only if an adapter returns an explicit `Approved` result. The default `UnconfiguredBluetoothPosGateway` safely reports that no terminal is configured; it does not simulate or claim a successful payment.
- Actual Bluetooth POS support is not generic: connect a vendor-specific implementation using the chosen terminal's official SDK or documented protocol. Confirm whether the terminal API expects toman or rial before implementing any unit conversion. Do not mark a trip as paid until an authoritative approval response and reference are validated.
- The uncertain-GPS manual formula remains `configured minimum fare (used as base fare) + entered units × 1,000 toman`, and the driver can edit the final amount before continuing to the payment interface.


## نسخه 0.8.0 — سازگاری نصب با Android 6

- حداقل نسخه Android از API 26 به API 23 (Android 6.0) کاهش یافت.
- نسخه برنامه به 0.8.0 افزایش یافت تا APK جدید با نسخه قبلی قابل تشخیص باشد.
- Workflow راه‌اندازی Android SDK را با درخواست صریح `platform-tools` انجام می‌دهد و سپس platform و build-tools موردنیاز را نصب می‌کند.
- سازگاری نصب روی دستگاه واقعی و موفقیت ساخت APK باید با اجرای GitHub Actions و آزمایش روی گوشی تأیید شود؛ این بسته در این محیط کامپایل نشده است.

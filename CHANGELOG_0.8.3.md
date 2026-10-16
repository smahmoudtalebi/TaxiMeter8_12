# TaxiMeterCore 0.8.3 — in-place UI reconciliation

- Keep the Activity content view, ScrollView, root layout, and matching child views attached across refreshes.
- Reconcile child views by position and type; update text, colors, enabled state, layout parameters, and button callbacks in place. Only replace a node when its view type changes or the visible structure requires it.
- Poll the Room-backed screen state every five seconds only while the Activity is visible; GPS tracking remains independent in the location foreground service.
- Refresh the GPS freshness indicator when the last valid fix becomes stale.
- Show passenger boarding time on occupied seat cards and boarding/alighting times in trip history.
- Replace the payment-method list with five prominent, colored action buttons: cash, barcode, POS, e-wallet, and citizen card. Methods without an integrated payment provider remain clearly identified as UI placeholders; no fake payment success is recorded.
- Set application version to 0.8.3 and retain Android 6 minimum support (`minSdk = 23`).

## Verification status

- Source archive integrity is checked with `unzip -t`.
- Kotlin source was checked for parser-level errors using the installed Kotlin compiler, but a full Android compilation, unit-test run, and Android 6 installation test could not be performed because this environment has no Android SDK or Gradle installation and network access to fetch them is unavailable. Do not treat the APK as built or device-validated.

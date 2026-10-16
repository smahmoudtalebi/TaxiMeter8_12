# TaxiMeterCore 0.8.1 — UI/payment iteration

Implemented in this source package:
- Removed the destructive three-second timer that rebuilt the complete view hierarchy and caused screen jumps. GPS collection remains owned by the location foreground service. The screen refreshes on resume and after user actions.
- Added a five-option payment-method chooser after fare confirmation: cash, barcode, POS, e-wallet, and citizen card.
- Kept barcode, cash, wallet, and citizen-card integrations explicitly marked as UI placeholders; they do not claim a transaction was completed. POS continues through the existing unconfigured gateway boundary.
- Applied a more consistent blue button palette and rounded cards, and reduced repeated passenger-seat explanatory text.
- Updated app version to 0.8.1-ui-payment; minSdk remains 23 (Android 6).

## Verification status
Source archive integrity is checked separately. An Android APK build and on-device Android 6 installation have **not** been performed in this environment. The view hierarchy is still reconstructed after explicit screen refreshes/actions; this iteration removes periodic destructive refreshes but is not yet a complete in-place view-binding refactor.

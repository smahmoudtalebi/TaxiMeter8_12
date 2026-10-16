# TaxiMeterCore 0.8.2 — screen stability pass

- Keep the existing ScrollView and root container attached rather than replacing the activity content view on every refresh.
- Skip redraws when the displayed database state has not changed.
- Keep the UI free of periodic full-screen refreshes; GPS collection remains the foreground service's responsibility.
- Tighten the management screen copy and adjust the primary color palette.
- Preserve Android 6 minimum SDK (`minSdk = 23`).

## Verification status

The source archive is structurally validated. This environment does not have an Android SDK/Gradle installation available, so an APK build and device installation test have **not** been performed. The current refresh path still reconstructs child views when the displayed data actually changes; a complete per-TextView in-place binding refactor remains future work and is not claimed as completed here.

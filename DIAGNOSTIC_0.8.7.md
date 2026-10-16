# TaxiMeterCore 0.8.7 — tariff-dialog diagnostic

- Adds a guarded handler around the “تعریف / تغییر تعرفه” button.
- If opening the dialog throws a synchronous exception, the app logs the stack trace under the `TaxiMeter` tag and displays the exception type/message instead of immediately closing.
- This is a diagnostic build, not confirmation that the root cause is fixed.
- Build/version: versionCode 7, versionName 0.8.7-android6-diagnostic.

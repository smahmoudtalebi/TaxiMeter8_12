# TaxiMeterCore 0.8.10 — button callback reliability

- Replaced in-place positional view reconciliation with a fresh screen tree when the render signature changes.
- This avoids reusing button listeners whose callbacks may capture detached views, especially form controls.
- Preserves scroll position on meaningful screen redraws. Unchanged signatures still skip rendering, and tariff editing ignores GPS telemetry changes to protect entered values.
- Build/runtime validation on Android 6 has not been performed in this environment.

# ساخت APK گیرنده با GitHub Actions

این پوشه باید به‌عنوان یک مخزن مستقل GitHub ساخته شود. نکته مهم: GitHub فقط workflowهایی را می‌شناسد که در مسیر `.github/workflows/` در ریشه مخزن باشند؛ workflow داخل پوشه فرعی را خودکار اجرا نمی‌کند.

## روش پیشنهادی
1. در GitHub یک مخزن جدید بسازید، مثلاً `TaxiMeterBluetoothReceiver`.
2. محتویات داخل این پوشه (`TaxiMeterBluetoothReceiver`) را آپلود کنید، نه خود پوشه را به‌عنوان یک زیرپوشه. در ریشه مخزن باید `settings.gradle.kts`، `build.gradle.kts`، `app/` و `.github/workflows/build-apk.yml` دیده شوند.
3. از زبانه **Actions**، workflow با نام **Build Bluetooth Receiver APK** را باز کنید.
4. اگر تأیید فعال‌سازی Actions ظاهر شد، آن را فعال کنید.
5. روی **Run workflow** بزنید؛ یا پس از commit/push، workflow به‌طور خودکار اجرا می‌شود.
6. پس از اتمام موفق، پایین صفحه اجرای workflow بخش **Artifacts** ظاهر می‌شود. فایل `TaxiMeterBluetoothReceiver-debug-apk` را دانلود و ZIP آن را باز کنید؛ داخل آن `app-debug.apk` قرار دارد.

اگر زبانه Actions همچنان workflow را نشان نمی‌دهد، بررسی کنید که مسیر دقیق در ریشه مخزن این باشد:
`.github/workflows/build-apk.yml`

APK آزمایشی است و پرداخت بانکی واقعی انجام نمی‌دهد. این workflow ساخت APK را خودکار می‌کند؛ موفقیت ساخت تا زمانی که اجرای workflow سبز نشده باشد تضمین‌شده نیست.

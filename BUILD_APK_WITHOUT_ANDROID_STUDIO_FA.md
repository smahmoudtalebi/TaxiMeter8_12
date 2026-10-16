# راهنمای ساخت APK با GitHub Actions (نسخه 0.7.1)

## محتویات و پیش‌نیاز

- این پروژه از Gradle 8.9 استفاده می‌کند و Workflow خودش Gradle و Android SDK لازم را روی runner نصب/آماده می‌کند.
- بنابراین برای ساخت APK در GitHub Actions، فایل‌های Gradle Wrapper (`gradlew` و `gradle/wrapper/gradle-wrapper.jar`) لازم نیستند. پوشه خالی `gradle/` عمداً حذف شده است.
- فایل Workflow باید در مسیر `.github/workflows/build-apk.yml` باشد.
- متد `getHistoricalTripsWithEndpoints()` در `app/src/main/java/ir/taximeter/core/data/TaxiDao.kt` تعریف شده و همراه MainActivity در این بسته قرار دارد.

## بارگذاری در مخزن GitHub

1. ZIP را در رایانه استخراج کنید.
2. وارد مخزن GitHub موردنظر شوید.
3. بارگذاری باید شامل **محتویات داخل پوشه `TaxiMeterCore`** باشد، نه اینکه یک پوشه اضافه و تو در تو به نام `TaxiMeterCore/TaxiMeterCore` ساخته شود.
4. حتماً پوشه مخفی `.github` و فایل `.github/workflows/build-apk.yml` را هم بارگذاری کنید. در صفحه اصلی مخزن باید `app/`، `build.gradle.kts`، `settings.gradle.kts` و `.github/` هم‌سطح باشند.
5. Commit و Push کنید. Workflow با هر push اجرا می‌شود؛ یا از تب **Actions**، گزینه **Build TaxiMeterCore APK** و سپس **Run workflow** را انتخاب کنید.
6. اگر اجرا موفق شد، در صفحه اجرای workflow به بخش **Artifacts** بروید و `TaxiMeterCore-debug-apk` را دانلود کنید. فایل داخل آن `app-debug.apk` است.
7. اگر اجرا ناموفق شد، APK قابل استفاده‌ای تولید نشده است. در گزارش، اولین خطای `e:` یا `FAILURE` را بررسی و ارسال کنید.

## محدودیت‌های مهم

- در این محیط فعلی Android SDK و Gradle در دسترس نبودند؛ بنابراین این بسته در اینجا کامپایل یا روی گوشی آزمایش نشده است. موفقیت فقط پس از اجرای سبز GitHub Actions تأیید می‌شود.
- خروجی `debug APK` آزمایشی است و APK انتشار امضاشده محسوب نمی‌شود.
- درگاه پوز بلوتوثی یک interface است؛ ارسال واقعی به پوز تا زمان انتخاب مدل دستگاه و اتصال SDK/protocol رسمی آن فعال نیست.

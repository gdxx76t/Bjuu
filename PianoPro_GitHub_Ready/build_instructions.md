# دستورالعمل ساخت Piano Pro

**ایده توسط S.D داده شده**

## پیش‌نیازها

### نرم‌افزارهای مورد نیاز
- Android Studio Arctic Fox یا جدیدتر
- JDK 11 یا جدیدتر
- Android SDK API 34
- Gradle 7.0+

### تنظیمات محیط توسعه
1. Android Studio را نصب کنید
2. SDK های مورد نیاز را دانلود کنید:
   - Android 14 (API 34) - Target SDK
   - Android 7.0 (API 24) - Minimum SDK
3. NDK را برای کامپایل کتابخانه‌های native نصب کنید

## مراحل ساخت

### 1. کلون کردن پروژه
```bash
git clone <repository-url>
cd PianoPro
```

### 2. باز کردن در Android Studio
1. Android Studio را باز کنید
2. "Open an existing project" را انتخاب کنید
3. پوشه PianoPro را انتخاب کنید

### 3. Sync کردن پروژه
1. Android Studio به صورت خودکار Gradle sync را شروع می‌کند
2. اگر خطایی رخ داد، "Sync Project with Gradle Files" را کلیک کنید

### 4. ساخت برنامه

#### Debug Build
```bash
./gradlew assembleDebug
```

#### Release Build
```bash
./gradlew assembleRelease
```

### 5. تست کردن

#### Unit Tests
```bash
./gradlew test
```

#### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

## فایل‌های خروجی

### Debug APK
```
app/build/outputs/apk/debug/app-debug.apk
```

### Release APK
```
app/build/outputs/apk/release/app-release.apk
```

### AAB (Android App Bundle)
```bash
./gradlew bundleRelease
```
خروجی: `app/build/outputs/bundle/release/app-release.aab`

## امضای برنامه

### ایجاد Keystore
```bash
keytool -genkey -v -keystore piano-pro.keystore -alias piano-pro -keyalg RSA -keysize 2048 -validity 10000
```

### تنظیم امضا در gradle.properties
```properties
PIANO_PRO_STORE_FILE=piano-pro.keystore
PIANO_PRO_STORE_PASSWORD=your_store_password
PIANO_PRO_KEY_ALIAS=piano-pro
PIANO_PRO_KEY_PASSWORD=your_key_password
```

## بهینه‌سازی

### ProGuard/R8
برنامه از R8 برای کوچک‌سازی و بهینه‌سازی کد استفاده می‌کند.
قوانین ProGuard در فایل `app/proguard-rules.pro` تعریف شده‌اند.

### Native Libraries
کتابخانه Oboe برای عملکرد صوتی بهینه استفاده می‌شود.

## عیب‌یابی

### مشکلات رایج

#### خطای Gradle Sync
- بررسی کنید که JDK 11+ نصب باشد
- Cache Gradle را پاک کنید: `./gradlew clean`

#### خطای NDK
- NDK را از SDK Manager نصب کنید
- مسیر NDK را در `local.properties` تنظیم کنید

#### خطای Dependencies
- اتصال اینترنت را بررسی کنید
- Repository های Maven را بررسی کنید

### لاگ‌ها
برای دیدن لاگ‌های دقیق:
```bash
adb logcat | grep "PianoPro"
```

## انتشار

### Google Play Store
1. AAB فایل را ایجاد کنید
2. در Google Play Console آپلود کنید
3. Store listing را تکمیل کنید
4. Review process را طی کنید

### سایر فروشگاه‌ها
APK فایل را برای سایر فروشگاه‌ها استفاده کنید.

---

**ایده توسط S.D داده شده**


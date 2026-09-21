<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# رَبْوَة — Android app

Kotlin + Jetpack Compose Android application. Built with Gradle; no Android Studio required.

## Prerequisites

- **JDK 17 or 21** (AGP 9.1.1 does not support JDK 25/26). Point `JAVA_HOME` at JDK 21, or set
  `org.gradle.java.home` in `~/.gradle/gradle.properties`.
- **Android SDK** with `platforms;android-36` and `build-tools;36.1.0` installed.
  `local.properties` must contain `sdk.dir=...`.

## Configuration

Create `.env` in the project root (see `.env.example`):

```
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-supabase-anon-key
GOOGLE_WEB_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
```

These are injected into `BuildConfig` by the Secrets Gradle Plugin. If `.env` is absent the build
falls back to `.env.example`, but authentication will fail at runtime without real values.

## Build

```powershell
.\gradlew.bat assembleDebug     # -> app\build\outputs\apk\debug\app-debug.apk
.\gradlew.bat assembleRelease   # -> app\build\outputs\apk\release\app-release.apk
```

Release builds require signing (`app/build.gradle.kts`). Create an upload keystore once:

```powershell
keytool -genkeypair -v -keystore my-upload-key.jks -alias upload `
  -keyalg RSA -keysize 2048 -validity 10000
```

Then provide the credentials via environment variables before `assembleRelease`:

```powershell
$env:KEYSTORE_PATH="D:\keys\my-upload-key.jks"   # defaults to <rootDir>\my-upload-key.jks
$env:STORE_PASSWORD="..."
$env:KEY_PASSWORD="..."
.\gradlew.bat assembleRelease
```

Debug builds use `debug.keystore` in the project root. Regenerate it if missing:

```powershell
keytool -genkeypair -v -keystore debug.keystore -alias androiddebugkey `
  -storepass android -keypass android -keyalg RSA -keysize 2048 -validity 10000 `
  -dname "CN=Android Debug,O=Android,C=US" -storetype JKS
```

## Run with live logs

`run.ps1` builds, installs, launches and streams logcat — the closest CLI equivalent to
`flutter run` (no hot reload; rebuild to apply Kotlin/Compose changes).

```powershell
.\run.ps1                      # build + install + launch + follow logs
.\run.ps1 -NoBuild             # relaunch the installed app only
.\run.ps1 -ErrorsOnly          # only error-level log lines
.\run.ps1 -Snapshot -Tail 100  # print recent logs and exit
.\run.ps1 -Clean               # gradlew clean, then build/install/launch
```

The device is auto-detected; if none is connected it tries BlueStacks on `127.0.0.1:5555`.
Override with `-Device <serial>` (see `adb devices`).

Equivalent manual steps:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb connect 127.0.0.1:5555
.\gradlew.bat installDebug
& $adb shell am start -n "com.aistudio.arabickeyboard.asdfgh/com.example.MainActivity"
& $adb logcat --pid=$((& $adb shell pidof -s com.aistudio.arabickeyboard.asdfgh).Trim())
```

## Notes for restricted networks

If this machine cannot reach `dl.google.com` (Google Maven) or GitHub release assets, Android
dependencies cannot be resolved directly. A machine-level Gradle init script redirects artifact
resolution to reachable mirrors:

- `~/.gradle/init.gradle` — rewrites `pluginManagement` / `dependencyResolutionManagement`
  repositories to Huawei Cloud and Tencent Cloud mirrors.
- `~/.gradle/gradle.properties` — pins `org.gradle.java.home` to a supported JDK.

This machine's JVM defaults to the `ar_SA` locale, which caused Room/KSP to emit Arabic-Indic
numerals (`٣`, `١`, `٠`) into generated Kotlin source and break compilation. `gradle.properties`
therefore forces `-Duser.language=en -Duser.country=US` for the build JVMs. If you ever see
corrupted generated code again, purge the poisoned build cache and rebuild:

```powershell
Remove-Item "$env:USERPROFILE\.gradle\caches\build-cache-1" -Recurse -Force
.\gradlew.bat clean assembleDebug
```

## Publishing

If you have already published this app in AI Studio, [request an upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset)
in Google Play Console.

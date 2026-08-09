# Building & running Snitch without Android Studio or an emulator

This covers building the debug APK from the command line on Windows and installing it
straight onto your physical phone over USB — no Android Studio IDE, no AVD/emulator.

Expo Go doesn't apply here: it's a JavaScript/React Native runtime that only loads
pre-baked native modules. It has no way to run this project's native Kotlin code
(`VpnService`, `PackageManager`, Room), so it's not an option for this app.

## 0. Check what you already have

Open PowerShell and run:

```powershell
java -version
$env:ANDROID_HOME
Test-Path "$env:LOCALAPPDATA\Android\Sdk"
```

- `java -version` just needs to be **17 or newer** (Gradle/AGP's requirement is a
  minimum of 17 — a JDK 21 or later is fine as-is, nothing to install). Only install a
  JDK in step 1 if this prints something older than 17.
- If `Test-Path` prints `True`, you already have an Android SDK on disk (most likely
  installed by Android Studio at some point, even if you never open the IDE itself) —
  that's the common case. Step 2 below reuses that folder instead of creating a new
  one. If it prints `False`, step 2 creates a fresh SDK at `C:\Android` instead.

## 1. Install a JDK (skip if `java -version` already shows 17 or newer)

Download and install the Windows MSI for **Eclipse Temurin 17** from
https://adoptium.net/temurin/releases/ (choose version 17, JDK, your architecture,
`.msi` installer — check "Set JAVA_HOME" and "Add to PATH" in the installer).

Verify in a new PowerShell window:

```powershell
java -version
```

## 2. Install the Android command-line tools

Android Studio installs the SDK's `platform-tools`/`platforms`/`build-tools`, but not
always the separate `cmdline-tools` package that `sdkmanager` and `avdmanager` live in
— that's usually the one missing piece even when `Test-Path` above was `True`.

**Pick your SDK root** based on step 0's result:

```powershell
# If Test-Path above printed True, reuse the existing SDK:
$sdkRoot = "$env:LOCALAPPDATA\Android\Sdk"

# If it printed False, use a fresh location instead:
# $sdkRoot = "C:\Android"
```

Then:

1. Go to https://developer.android.com/studio#command-tools and download the
   **Windows "Command line tools only"** zip (not the full Android Studio installer).
2. Extract it and move the tools into place so `sdkmanager.bat` ends up at
   `$sdkRoot\cmdline-tools\latest\bin\sdkmanager.bat` — the zip extracts to a folder
   literally named `cmdline-tools`, and its *contents* need to land one level under a
   folder named `latest`, which is where the trip-up usually happens. Assuming you
   downloaded to your Downloads folder:

   ```powershell
   Expand-Archive -Path "$env:USERPROFILE\Downloads\commandlinetools-win-*_latest.zip" -DestinationPath "$env:TEMP\cmdline-tools-extract"
   New-Item -ItemType Directory -Force -Path "$sdkRoot\cmdline-tools\latest" | Out-Null
   Move-Item "$env:TEMP\cmdline-tools-extract\cmdline-tools\*" "$sdkRoot\cmdline-tools\latest\"
   ```

3. Set environment variables (persists for your user account):

   ```powershell
   [Environment]::SetEnvironmentVariable("ANDROID_HOME", $sdkRoot, "User")
   [Environment]::SetEnvironmentVariable(
       "Path",
       $env:Path + ";$sdkRoot\platform-tools;$sdkRoot\cmdline-tools\latest\bin",
       "User"
   )
   ```

**Open a new PowerShell window** so the changes take effect, then verify both resolve:

```powershell
sdkmanager --version
adb version
```

## 3. Install SDK components and accept licenses

```powershell
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-36"
sdkmanager --list | Select-String "build-tools;36"
sdkmanager "build-tools;36.0.0"   # use whatever version the line above actually shows
```

These are safe to run even if you already had an SDK — anything already installed
(e.g. from Android Studio) is left alone, and this only adds what's missing (this
project's `compileSdk = 36` specifically isn't guaranteed to already be there). This
never opens a GUI license dialog — `--licenses` accepts them from the terminal.

## 4. Enable USB debugging on your phone

1. Settings → About phone → tap **Build number** 7 times to unlock Developer Options.
2. Settings → System → Developer options → enable **USB debugging**.
3. Plug the phone into this PC with a USB cable.
4. On the phone, approve the "Allow USB debugging?" RSA fingerprint prompt.
5. Verify from PowerShell:

```powershell
adb devices
```

You should see your phone's serial number listed as `device` (not `unauthorized` — if
it says that, re-check the on-phone prompt). This never touches AVD Manager or starts
an emulator — it only talks to the physical device over USB.

(Optional, cable-free alternative once the above works once: Android's built-in
**Wireless debugging** under Developer options, paired via `adb pair <ip>:<port>`, lets
you skip the cable on later runs.)

## 5. Get the code and build

```powershell
git clone https://github.com/pwd5018/snitch.git
cd snitch
.\gradlew.bat assembleDebug
```

First run downloads Gradle/AGP/dependencies and will take a few minutes. The APK lands
at `app\build\outputs\apk\debug\app-debug.apk`.

## 6. Install and launch on your phone

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.pwd5018.snitch/.MainActivity
```

(Or just tap the "Snitch" icon on the phone — no need for the `am start` command.)

## 7. Watch what the VPN skeleton is doing

```powershell
adb logcat -s SnitchVpnService
```

Toggle the tunnel on from the Traffic Inspector screen and confirm you see packet-count
log lines. See the main plan's verification section for the full checklist (foreground
notification appears, `adb shell dumpsys connectivity` shows the test-subnet route not
`0.0.0.0/0`, your real browsing is unaffected while it's running).

## Day-to-day loop

Once steps 0–4 are done once, every future change is just:

```powershell
.\run.ps1
```

This builds, installs to whatever device `adb devices` sees, launches the app, and
tails `SnitchVpnService` logs (Ctrl+C to stop watching — the app keeps running). It's
just the three commands below wrapped with a couple of sanity checks (no device
attached, build failed, etc.) — nothing it does that you couldn't type by hand:

```powershell
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.pwd5018.snitch/.MainActivity
adb logcat -s SnitchVpnService
```

Pass `-NoLogcat` to `run.ps1` to build/install/launch without tailing logs.

If Windows refuses to run the script ("running scripts is disabled on this system"),
either allow scripts for your user once:

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

or run it one-off without changing that setting:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

No Android Studio, no emulator, either time.

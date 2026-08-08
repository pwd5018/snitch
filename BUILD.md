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

- If `java -version` prints `17.x`, skip step 1.
- If `ANDROID_HOME` is set and/or the SDK path exists (e.g. you have Android Studio
  installed even if you don't want to open it), you likely already have an SDK — skip
  to step 3's `sdkmanager` calls, pointing `ANDROID_HOME` at that existing folder
  instead of installing a fresh one in step 2. Android Studio's bundled SDK works fine
  for command-line builds; you never have to open the IDE itself.

## 1. Install JDK 17 (skip if `java -version` already shows 17.x)

Download and install the Windows MSI for **Eclipse Temurin 17** from
https://adoptium.net/temurin/releases/ (choose version 17, JDK, your architecture,
`.msi` installer — check "Set JAVA_HOME" and "Add to PATH" in the installer).

Verify in a new PowerShell window:

```powershell
java -version
```

## 2. Install the Android command-line tools (skip if you already have an SDK)

1. Go to https://developer.android.com/studio#command-tools and download the
   **Windows "Command line tools only"** zip (not the full Android Studio installer).
2. Extract it, then arrange the folder so `sdkmanager.bat` ends up at
   `C:\Android\cmdline-tools\latest\bin\sdkmanager.bat` (the tools expect a `latest`
   subfolder — the zip extracts into a folder named `cmdline-tools`, so create
   `C:\Android\cmdline-tools\latest\` and move the zip's contents into it).
3. Set environment variables (System Properties → Environment Variables, or PowerShell
   as Administrator for a persistent change):

```powershell
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "C:\Android", "User")
[Environment]::SetEnvironmentVariable(
    "Path",
    $env:Path + ";C:\Android\cmdline-tools\latest\bin;C:\Android\platform-tools",
    "User"
)
```

Open a new PowerShell window afterward so the changes take effect.

## 3. Install SDK components and accept licenses

```powershell
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-36"
sdkmanager --list | Select-String "build-tools;36"
sdkmanager "build-tools;36.0.0"   # use whatever version the line above actually shows
```

This never opens a GUI license dialog — `--licenses` accepts them from the terminal.

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
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb logcat -s SnitchVpnService
```

No Android Studio, no emulator, either time.

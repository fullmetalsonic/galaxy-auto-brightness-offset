# Auto Brightness Offset v1.4.1 — Detailed Guide

[Back to README](../README.md) · [한국어 설명서](USER_GUIDE_KO.md) · [Download v1.4.1](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/tag/v1.4.1)

This guide covers every setup and operating step for a first-time Samsung Galaxy user. Exact labels can vary slightly by One UI and Shizuku version.

## Stage 0 — Understand what the app does

This app does not disable adaptive brightness or hold the display at one fixed level. It shifts the result calculated by Samsung adaptive brightness.

- Positive values: brighter at the same ambient-light level
- Negative values: darker at the same ambient-light level
- `0`: clear the temporary app override and return to system adaptive brightness
- The number is an offset strength, not a display-brightness percentage

The app does not bundle root or an ADB server. It uses a user-approved connection to the official Shizuku app when calling protected display functions.

## Stage 1 — Check the requirements

You need:

- A Samsung Galaxy phone
- Android 11 or later recommended
- Internet access only to download Shizuku and the APK
- Samsung **Adaptive brightness** enabled
- The official Shizuku app
- The Auto Brightness Offset v1.4.1 APK

Android 11 and later can start Shizuku directly on the phone through wireless debugging. Android 10 and earlier require the computer ADB method.

## Stage 2 — Install Shizuku

1. Open the official [Shizuku download page](https://shizuku.rikka.app/download/).
2. Install it from a listed Google Play or official GitHub Release source.
3. Open Shizuku once after installation.

Avoid similarly named APKs from unofficial download sites. This guide is for the official `RikkaApps/Shizuku` app.

## Stage 3 — Enable Samsung Developer options

1. Open **Settings**.
2. Choose **About phone**.
3. Choose **Software information**.
4. Tap **Build number** seven times quickly.
5. Enter the screen-lock PIN or pattern.
6. Return to the main Settings page and verify that **Developer options** appears near the bottom.

Skip this stage if Developer options is already visible.

## Stage 4 — Pair and start Shizuku with wireless debugging

These steps apply to Android 11 and later.

1. Open **Settings → Developer options**.
2. Enable **Wireless debugging**. A current Wi-Fi connection may be required.
3. Return to Shizuku.
4. In **Start via wireless debugging**, choose **Pairing**.
5. In Android, select **Pair device with pairing code**.
6. Enter the displayed six-digit code in the Shizuku notification and submit it.
7. Return to Shizuku and tap **Start**.

Pairing is normally needed only once. Starting the service is required again after every phone reboot.

![Shizuku wireless-debugging controls](images/shizuku-wireless-debugging-ko.png)

The Shizuku screenshot above follows the phone's system language. The button positions and flow are the same in English. A successful start shows a **Shizuku is running** status card.

![Shizuku running status](images/shizuku-running-ko.png)

### If Shizuku does not start

1. Turn Wireless debugging off and on again.
2. Return to Shizuku and tap Start.
3. If needed, remove the old pairing and repeat Stage 4.
4. Try another Wi-Fi network if a corporate or public network blocks local-device communication.

Official procedure: [Shizuku setup guide](https://shizuku.rikka.app/guide/setup/)

## Stage 5 — Start Shizuku over USB ADB for Wi-Fi-free use

This method also applies to Android 11 and later. Start Shizuku from a computer before leaving home, then use the offset outdoors without a Wi-Fi connection. The procedure below was physically verified with Shizuku v13.6.0 on a Galaxy Z Fold8.

1. Download the official [Android SDK Platform Tools](https://developer.android.com/tools/releases/platform-tools) on the computer.
2. Enable **USB debugging** in Developer options.
3. Connect the phone by USB and approve the phone's debugging prompt.
4. Run `adb devices` and confirm that the phone state is `device`.
5. First run the current ADB command shown by Shizuku or its official guide. The commonly documented command is:

```text
adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
```

6. If a current Shizuku build does not expose that file, use PowerShell to resolve and run its installed starter:

```powershell
$line = adb shell dumpsys package moe.shizuku.privileged.api |
    Select-String "legacyNativeLibraryDir=" |
    Select-Object -First 1
$libDir = ($line.ToString().Trim() -split "=", 2)[1]
adb shell "$libDir/arm64/libshizuku.so"
```

7. Verify that `adb shell pidof shizuku_server` prints a numeric PID.
8. Confirm the running state in the Shizuku app.
9. Open **Settings → Apps → Shizuku → Battery** and select **Unrestricted**.
10. Do the same for **Auto Brightness Offset → Battery → Unrestricted**.
11. Open **Settings → Battery → Background usage limits → Never auto sleeping apps** and add both Shizuku and Auto Brightness Offset. Exact One UI labels may vary.
12. You may unplug USB and turn both Wi-Fi and **Wireless debugging** off. The tested Fold8 kept **USB debugging** enabled.

On the tested Fold8 with One UI 9, both apps were exempted from battery restrictions before a Wi-Fi-off three-minute screen-on background run, screen lock, wake, and automatic reapply test passed. This is a measured device result, not a guarantee of permanent operation on every One UI release.

Operational limits:

- USB is required only to start Shizuku; it does not stay connected during normal use.
- Wi-Fi and Wireless debugging do not need to stay enabled after a USB start.
- Both apps must be exempted from Samsung battery restrictions so background management does not freeze Shizuku or this app.
- If Shizuku's server is alive but the app shows **Offset paused**, keep Wi-Fi off and use **open Shizuku → open Auto Brightness Offset** to refresh the connection.
- A phone reboot or terminated Shizuku server requires another start.
- Restarting away from home requires a computer, a USB ADB bridge such as a Raspberry Pi, or a root-based method.
- Shizuku's command can change by version; prefer the current command shown by the official app when available.

## Stage 6 — Install Auto Brightness Offset

1. Open the GitHub [v1.4.1 Release](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/tag/v1.4.1).
2. Download `AutoBrightnessOffset-v1.4.1-release.apk`.
3. Open the download from the browser notification or **My Files → Downloads**.
4. If Android asks, temporarily allow **Install unknown apps** for that browser or My Files.
5. You may turn that permission off again after installation.

File verification:

- Size: `3,474,839 bytes`
- SHA-256: `91B5700052DECF3BCCBC67B17684CF90DF4B7C2955FCBD9A1AD1799DA472BBD0`
- Build/signature: R8-optimized Release, Android Debug certificate, APK Signature Scheme v2 and v3

This is a debug-signed personal-test package, not a Google Play production-signed build.

## Stage 7 — First launch and permissions

1. Verify that Shizuku is running first.
2. Open **Auto Brightness Offset**.
3. Allow the app in Shizuku's permission dialog.
4. Verify that the top status reads **Ready to apply**.
5. Verify that **Shizuku access** and **Adaptive brightness** both show a ready state.
6. Allow notifications when prompted. They show active management and provide a **Clear offset** action.

![Ready-to-apply English interface](images/app-home-en-v1.4.1.png)

If you denied access accidentally, open Shizuku's authorized-app list and allow Auto Brightness Offset there.

## Stage 8 — Choose an offset

### Slider

- Range: `-100` to `+100`
- Step size: 5
- Left: darker
- Center `0`: system default
- Right: brighter

### Quick presets

Choose `-75`, `-50`, `-25`, `0`, `+25`, `+50`, or `+75` with one tap.

Start with a small value such as `+10` or `-10`. Values from `+75` to `+100` and `-75` to `-100` are intentionally strong.

## Stage 9 — Apply and verify

1. Select a value.
2. Tap **Apply selected offset**.
3. Confirm that the main button changes to **Offset +value active** or **Offset -value active**.
4. Check for the **Auto brightness offset active** notification.
5. Keep the phone in the same location and compare the display before and after applying.
6. When ambient light changes, the app reads Samsung's new adaptive target and recalculates the offset.

If the first change is subtle, move in 5- or 10-point steps. Samsung brightness animation and ambient-light stabilization can make the visual change feel slightly delayed.

## Stage 10 — Reapply after a reboot

**Reapply after reboot** remembers the last active offset. However, non-root Shizuku stops when the phone reboots.

After a reboot:

1. Open Shizuku.
2. Start Shizuku again through wireless debugging.
3. Open Auto Brightness Offset.
4. Confirm **Ready to apply** and the active value.

The offset cannot resume before Shizuku is running.

## Stage 11 — Restore the original behavior

Use any one of these methods:

- Select `0` and apply it
- Tap **Restore original** in the app
- Tap **Clear offset** in the management notification

Restoring clears the temporary display override and stops the management service, returning direct control to Samsung adaptive brightness.

If Shizuku is stopped when you request a restore, the app enters **Restore pending**. Start Shizuku and open the app once; it then clears the temporary override automatically.

![English settings and recovery actions](images/app-settings-en-v1.4.1.png)

## Stage 12 — Use Diagnostics

Tap **Diagnostics** to copy:

- App version
- Shizuku state
- Adaptive-brightness state
- Current and last offset
- Management state
- Reapply-after-reboot setting
- Pending-restore state

The report is not designed to contain accounts, passwords, photos, or screen content. Review copied text yourself before sharing it in an issue.

## Stage 13 — Troubleshooting

| Symptom | Check | Action |
|---|---|---|
| Install Shizuku first | Shizuku is missing | Install it from the official download page |
| Start Shizuku first | Service is stopped | Start Shizuku through wireless debugging or USB ADB, then open the app |
| Offset paused | Shizuku stopped during use | Start Shizuku and open the app once to reapply the saved offset |
| Restore pending | Restore was requested while disconnected | Start Shizuku and open the app to complete the restore |
| Shizuku permission required | Access was denied | Allow the app in Shizuku's authorized-app list |
| Adaptive brightness required | Manual brightness is active | Enable Samsung Adaptive brightness |
| Difference is too small | Small value or changing ambient light | Compare `+25` and `+50` in the same location |
| Display is too bright or dark | Offset is too strong | Apply `0` or choose Restore original |
| No management notification | Notification permission blocked | Allow notifications in Android app info |
| Not active after reboot | Shizuku stopped at boot | Start Shizuku, then reopen the app |
| Wireless start fails | Pairing or network issue | Toggle wireless debugging and pair again |
| Offset stops two or three minutes after Wi-Fi is disabled | Samsung background management froze Shizuku Binder delivery or the app | Set both apps to Unrestricted and add both to Never auto sleeping apps |
| Shizuku is running but the app says Offset paused | The app has not received Shizuku's Binder again | Without enabling Wi-Fi, open Shizuku and then Auto Brightness Offset |
| No outdoor Wi-Fi | Shizuku was not started over USB first | Start it over USB, exempt both apps from battery restrictions, and avoid rebooting |
| Failure after a One UI update | Private API may have changed | Copy Diagnostics and report an issue |

## Stage 14 — Uninstall and clean up

1. Tap **Restore original** first.
2. If **Restore pending** appears, start Shizuku and reopen the app to complete the restore.
3. Confirm that the active-management notification disappears.
4. Uninstall Auto Brightness Offset from Android app info.
5. Stop or uninstall Shizuku only if no other apps need it.
6. Disable wireless and USB debugging if you no longer use Developer options.

## Stage 15 — Privacy, safety, and limitations

- The app has no Internet permission.
- It uses no account, ads, analytics, location, camera, microphone, or contacts.
- It stores only management state, the last offset, and the reapply-after-reboot choice.
- It does not save or transmit ambient-light history or screen content.
- Shizuku's own behavior and networking follow Shizuku's policies.
- Exempting both apps from battery restrictions allows longer background operation and may use more battery than the default policy.
- Outdoor boost, thermal dimming, power saving, HDR, and app-specific limits remain higher priority.
- Samsung-specific commands and private temporary APIs may fail on other manufacturers or after a future One UI change.

See [Privacy and permissions](PRIVACY.md) for the exact permission inventory.

## Official references

- [Official Shizuku download](https://shizuku.rikka.app/download/)
- [Official Shizuku setup guide](https://shizuku.rikka.app/guide/setup/)
- [Official Shizuku GitHub](https://github.com/RikkaApps/Shizuku)
- [Official Shizuku API GitHub](https://github.com/RikkaApps/Shizuku-API)
- [Samsung Developer options guide](https://developer.samsung.com/health/data/guide/phone-developer-options.html)
- [Android hardware-device and wireless-debugging guide](https://developer.android.com/studio/run/device)

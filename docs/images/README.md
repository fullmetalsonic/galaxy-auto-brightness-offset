# Public image inventory

All images in this folder are documentation assets approved for the public repository.

| File | Source and use | Privacy treatment |
|---|---|---|
| `app-home-ko-v1.4.1.png` | Real Galaxy Z Fold8, installed v1.4.1 Release, Korean active screen, captured Aug 27 | Cropped system bars and the partially visible next paragraph |
| `app-home-en-v1.4.1.png` | Real Galaxy Z Fold8, installed v1.4.1 Release, app-only `en-US`, complete Aug 26 source retained | Cropped system bars and the partially visible next paragraph |
| `app-select-offset-ko-v1.4.1.png` | Real Galaxy Z Fold8, installed v1.4.1 Release, draft 0 while +75 remains active, Aug 27 | Exact app-only crop |
| `app-select-offset-en-v1.4.1.png` | Real Galaxy Z Fold8, installed v1.4.1 Release, English draft 0 while +75 remains active, Aug 27 | Exact app-only crop |
| `app-settings-ko-v1.4.1.png` | Real Galaxy Z Fold8, installed v1.4.1 Release, Korean settings with reboot choice OFF, Aug 27 | Cropped to the complete settings section |
| `app-settings-en-v1.4.1.png` | Real Galaxy Z Fold8, installed v1.4.1 Release, English settings with reboot choice OFF, Aug 27 | Cropped to the complete settings section |
| `app-restore-pending-ko-v1.4.1.png` | Real Galaxy Z Fold8, v1.4.1 verification build, Shizuku-disconnected restore queue | Cropped system status and navigation bars |
| `app-home-ko-v1.4.0.png` | Real Galaxy Z Fold8, Korean main screen | Cropped system status and navigation bars |
| `app-select-offset-ko-v1.4.0.png` | Real Galaxy Z Fold8, Korean offset selection | Cropped system status and navigation bars |
| `app-settings-ko-v1.4.0.png` | Real Galaxy Z Fold8, Korean recovery/actions | Cropped to the app-only area |
| `app-home-en-v1.4.0.png` | Real Galaxy Z Fold8, app-only `en-US` locale | Cropped system status and navigation bars |
| `app-settings-en-v1.4.0.png` | Real Galaxy Z Fold8, English recovery/actions | Cropped to the app-only area |
| `shizuku-running-ko.png` | Real official Shizuku running-status screen | Cropped carrier, time, notifications, battery, and navigation |
| `shizuku-wireless-debugging-ko.png` | Real official Shizuku wireless-debugging controls | Cropped to the relevant setup card |

The app screenshots preserve the captured UI. An AI-assisted privacy-crop experiment was rejected because it changed interface pixels; it is not included here. Final public screenshots use deterministic crops of actual captures.

For v1.4.1, [the export manifest](ui-screenshots-v1.4.1.json) records raw-source hashes, crop rectangles, and public-output hashes. The Windows [export/check script](../../scripts/export-ui-screenshots.ps1) compares every RGBA pixel after saving; all seven outputs passed with zero changed pixels on Aug 27. Private raw screenshots are required for a local rerun and remain excluded from Git. A suspicion that the previous selection screenshot had lost text was ruled out by the same pixel comparison (zero differences).

Shizuku is a separate application maintained by RikkaApps. These screenshots are used only to explain the interoperability setup. Refer to the [official Shizuku website](https://shizuku.rikka.app/) and [official repository](https://github.com/RikkaApps/Shizuku) for current information.

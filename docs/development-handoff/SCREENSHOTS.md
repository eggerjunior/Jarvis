# Screenshots

Generated: 2026-07-17T13:38:01-03:00

Screenshots must be added for UI changes and every publication when possible.

## Files

- `screenshots/ios-simulator-auto-activation-2026-07-13.png` — 2026-07-13, iPhone 17 iOS 26.5 Simulator. Confirms Jarvis starts activation automatically on launch, enters the `Ativando` button state, and reaches the iOS speech-recognition permission flow.
- `screenshots/ios-simulator-feedback-adjustments-2026-07-13.png` — 2026-07-13, iPhone 17 iOS 26.5 Simulator. Confirms Jarvis 1.4.0 build 10 launches after the feedback adjustments.
- `screenshots/ios-simulator-voice-picker-2026-07-13.png` — 2026-07-13, iPhone 17 iOS 26.5 Simulator. Confirms Jarvis 1.4.3 build 13 installs and launches after adding installed iOS voice selection.
- `screenshots/ios-simulator-jarvis-installed-2026-07-12.png` — 2026-07-12, iPhone 17 iOS 26.5 Simulator. Confirms Jarvis 1.3.0 build 9 launched locally with the widget extension embedded in the installed app bundle.
- `screenshots/carplay-simulator-window-2026-07-12.png` — 2026-07-12, CarPlay Simulator from Additional Tools for Xcode 26.6. Confirms the CarPlay Simulator session was connected to `iPhone 16 de Ildemar` and showing `Automaker UI`, not the CoreSimulator iPhone 17 used for local widget install.
- `screenshots/carplay-refocus-after-settings-2026-07-13.png` — 2026-07-13, CarPlay Simulator after settings refocus. Confirms the simulator can be opened locally, but full CarPlay app validation still depended on entitlement approval (now approved, see below).


## Missing Screenshots

- CarPlay list template with the "Diga Ei Jarvis" item (added in version 1.5.0 build 15) has not been captured yet — needs a real CarPlay-connected iPhone or the CarPlay Simulator paired to a device running this build.
- Settings sheet with the installed voice picker should be captured during the next manual visual QA pass.
- Second Brain edit modal should be captured during the next manual visual QA pass.
- Live Activity lock screen and Dynamic Island states still need screenshots after a real start trigger is wired.

## CarPlay Simulator Note

CarPlay Simulator from Additional Tools for Xcode 26.6 was installed locally at `~/Applications/Apple Developer Tools/CarPlay Simulator.app`. It connects to a real/remote iPhone/iPad, not to CoreSimulator iOS devices. For CarPlay Simulator widget/scene testing, install the build on a connected iPhone and select/connect that device in CarPlay Simulator.

# Screenshots

Generated: 2026-07-13T09:42:12-03:00

Screenshots must be added for UI changes and every publication when possible.

## Files

- `screenshots/ios-simulator-feedback-adjustments-2026-07-13.png` — 2026-07-13, iPhone 17 iOS 26.5 Simulator. Confirms Jarvis 1.4.0 build 10 launches after the feedback adjustments.
- `screenshots/ios-simulator-jarvis-installed-2026-07-12.png` — 2026-07-12, iPhone 17 iOS 26.5 Simulator. Confirms Jarvis 1.3.0 build 9 launched locally with the widget extension embedded in the installed app bundle.
- `screenshots/carplay-simulator-window-2026-07-12.png` — 2026-07-12, CarPlay Simulator from Additional Tools for Xcode 26.6. Confirms the CarPlay Simulator session was connected to `iPhone 16 de Ildemar` and showing `Automaker UI`, not the CoreSimulator iPhone 17 used for local widget install.

## Missing Screenshots

- Settings sheet with OpenRouter/provider controls should be captured during the next manual visual QA pass.
- Second Brain edit modal should be captured during the next manual visual QA pass.
- Live Activity lock screen and Dynamic Island states still need screenshots after a real start trigger is wired.

## CarPlay Simulator Note

CarPlay Simulator from Additional Tools for Xcode 26.6 was installed locally at `~/Applications/Apple Developer Tools/CarPlay Simulator.app`. It could be opened, but the 2026-07-12 runtime session connected to `iPhone 16 de Ildemar`, while a rebuilt Jarvis app was installed on the booted CoreSimulator device `iPhone 17` (`067DE2A0-9E13-49E6-AFA5-C78D3155EA94`). For CarPlay Simulator widget testing, install the build on the connected iPhone.

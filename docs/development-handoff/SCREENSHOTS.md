# Screenshots

Generated: 2026-07-12T11:12:41-03:00

Screenshots must be added for UI changes and every publication when possible.

## Files

- `screenshots/ios-simulator-jarvis-installed-2026-07-12.png` — 2026-07-12, iPhone 17 iOS 26.5 Simulator. Confirms Jarvis 1.3.0 build 9 launches locally with the widget extension embedded in the installed app bundle.


## Missing Screenshots

No screenshots were captured in this pass because the change created and validated build-level iOS extension infrastructure. Next visual QA should capture:

- the small Jarvis widget in an iOS simulator or device widget gallery/home screen;
- the Live Activity lock screen presentation after a real start trigger is wired;
- the Dynamic Island compact/expanded states on a supported simulator/device.

## CarPlay Simulator Note

CarPlay Simulator from Additional Tools for Xcode 26.6 was installed locally at `~/Applications/Apple Developer Tools/CarPlay Simulator.app`. It could be opened, but its internal help/strings and runtime behavior indicate it connects to an actual iPhone/iPad through USB or wireless/CoreDevice, not to an iOS Simulator booted through CoreSimulator. Local automated validation therefore confirmed iOS Simulator install and WidgetKit registration, but did not render the widget inside CarPlay Simulator.

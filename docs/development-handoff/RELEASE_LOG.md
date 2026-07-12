# Release Log

Generated: 2026-07-12T11:12:41-03:00

Record every deploy, TestFlight/App Store upload, web publish and external processing status here.

## 2026-07-12 - Widget Extension And Live Activity Base

- App: Jarvis iOS
- Bundle id: `br.app.egger.jarvis`
- Widget extension bundle id: `br.app.egger.jarvis.widgets`
- Version/build: kept at `1.2.2` / `8`; no TestFlight upload was performed in this change.
- Commit at start of work: `fab17281`
- Changes:
  - Added `JarvisWidgets` WidgetKit extension target through XcodeGen.
  - Added small static Jarvis status widget.
  - Added shared `JarvisLiveActivityAttributes`.
  - Added ActivityKit `JarvisLiveActivityController` in the app target.
  - Added Live Activity and Dynamic Island rendering in the widget extension.
  - Enabled `NSSupportsLiveActivities` in the app Info.plist.
- Validation:
  - `cd ios && xcodegen generate` succeeded.
  - `cd ios && xcodebuild -project Jarvis.xcodeproj -scheme Jarvis -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build` succeeded.
- External status:
  - CarPlay entitlement request was submitted manually and is pending Apple review.
  - Apple Developer Bundle ID `br.app.egger.jarvis.widgets` was created via App Store Connect API after this setup. Resource id: `Z7SGYGS278`.
  - No App Store Connect/TestFlight processing was started.

## 2026-07-12 - TestFlight 1.3.0 (Build 9)

- App: Jarvis iOS
- Bundle id: `br.app.egger.jarvis`
- Widget extension bundle id: `br.app.egger.jarvis.widgets` (`Z7SGYGS278`)
- Version/build: `1.3.0` / `9`
- Planned changes:
  - Ship Widget Extension and Jarvis status widget.
  - Ship Live Activity/Dynamic Island base.
  - Keep CarPlay full app pending Apple entitlement approval.
- Status: preparing; update this entry after `ios/scripts/testflight.sh` finishes.

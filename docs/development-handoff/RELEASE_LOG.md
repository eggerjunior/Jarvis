# Release Log

Generated: 2026-07-12T11:12:41-03:00

Record every deploy, TestFlight/App Store upload, web publish and external processing status here.

## 2026-07-13 - Speech Pause And Web Search 1.4.2 (Build 12)

- App: Jarvis iOS
- Bundle id: `br.app.egger.jarvis`
- Widget extension bundle id: `br.app.egger.jarvis.widgets`
- Version/build: `1.4.2` / `12`
- Binary commit: `6bc2d325`
- Source feedback: TestFlight feedback `testflight_feedback.zip` for version `1.4.1` build `11`.
- Changes:
  - Increased the in-command speech silence threshold from 1 second to 3 seconds.
  - Added a 5-second continuation wait when the partial transcription ends with words that usually indicate the phrase is not complete.
  - Routed questions about current events, news, prices, recent versions and web searches through OpenRouter web search when an OpenRouter API key is configured.
  - Added a clear spoken fallback when current-information questions require OpenRouter but no OpenRouter key is configured.
- Validation:
  - `cd ios && xcodegen generate` succeeded.
  - `cd ios && xcodebuild -project Jarvis.xcodeproj -scheme Jarvis -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath ../build/DerivedDataFeedback142 build` succeeded.
  - `cd ios && ./scripts/testflight.sh` archived and uploaded successfully.
- Status: uploaded successfully to App Store Connect/TestFlight at 2026-07-13 11:00 America/Sao_Paulo.
- Apple processing: uploaded package is processing.

## 2026-07-13 - Follow-Up And Voice Fix 1.4.1 (Build 11)

- App: Jarvis iOS
- Bundle id: `br.app.egger.jarvis`
- Widget extension bundle id: `br.app.egger.jarvis.widgets`
- Version/build: `1.4.1` / `11`
- Binary commit: `8950b142`
- Changes:
  - Increased the post-speech continuation window from 2 seconds to 5 seconds.
  - Removed remaining active-flow text that mentioned 2 seconds.
  - Reinforced masculine voice selection with known Portuguese male identifiers and a stronger lower-pitch fallback when iOS falls back to the default pt-BR voice.
- Validation:
  - `cd ios && xcodegen generate` succeeded.
  - `cd ios && xcodebuild -project Jarvis.xcodeproj -scheme Jarvis -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath ../build/DerivedDataVoiceFollowup build` succeeded.
  - `cd ios && ./scripts/testflight.sh` archived and uploaded successfully.
- Status: uploaded successfully to App Store Connect/TestFlight at 2026-07-13 10:30 America/Sao_Paulo.
- Apple processing: uploaded package is processing.

## 2026-07-13 - Feedback Adjustments 1.4.0 (Build 10)

- App: Jarvis iOS
- Bundle id: `br.app.egger.jarvis`
- Widget extension bundle id: `br.app.egger.jarvis.widgets`
- Version/build: `1.4.0` / `10`
- Binary commit: `648f0c94`
- Source feedback: TestFlight feedback zips `testflight_feedback-2.zip` through `testflight_feedback-6.zip`.
- Changes:
  - Removed cost/spend lookup from the model test result.
  - Added OpenRouter as an alternate AI provider with separate API key and model list.
  - Added editable Second Brain notes by tapping graph bubbles or cards.
  - Improved masculine/feminine voice selection by preferring named Portuguese voices.
  - Fixed follow-up listening so speech started within 2 seconds after Jarvis finishes speaking stays in direct-command mode.
- Validation:
  - `cd ios && xcodegen generate` succeeded.
  - `cd ios && xcodebuild -project Jarvis.xcodeproj -scheme Jarvis -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath ../build/DerivedDataFeedback build` succeeded.
  - Installed and launched on iPhone 17 iOS 26.5 Simulator and captured `ios-simulator-feedback-adjustments-2026-07-13.png`.
  - `cd ios && ./scripts/testflight.sh` archived and uploaded successfully.
- Status: uploaded successfully to App Store Connect/TestFlight at 2026-07-13 09:45 America/Sao_Paulo.
- Apple processing: uploaded package is processing.

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
- Binary commit: `62bef40b`
- Changes:
  - Ship Widget Extension and Jarvis status widget.
  - Ship Live Activity/Dynamic Island base.
  - Keep CarPlay full app pending Apple entitlement approval.
- Commands:
  - `cd ios && xcodegen generate`
  - `cd ios && xcodebuild -project Jarvis.xcodeproj -scheme Jarvis -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build`
  - `git commit -m "Add Jarvis widget and live activity base"`
  - `git push origin main`
  - `cd ios && ./scripts/testflight.sh`
- Status: uploaded successfully to App Store Connect/TestFlight at 2026-07-12 16:54 America/Sao_Paulo.
- Apple processing: uploaded package is processing.
- Notes:
  - Archive succeeded.
  - Export/upload succeeded.
  - Xcode logged a connected-device passcode warning during archive, but the archive and upload completed successfully.

# Project Context

Generated: 2026-07-12T11:12:41-03:00

## Snapshot

- Project: `Jarvis@apvictorio`
- Root: `/Users/ildemareggerjunior/Projects/Jarvis@apvictorio`
- Branch: `main`
- Commit: `fab17281`
- Git status: dirty
- iOS version: `1.3.0`
- iOS build: `9`
- iOS bundle id: `br.app.egger.jarvis`
- Widget extension bundle id: `br.app.egger.jarvis.widgets`
- Detected stack: swift, node

## Product Purpose

Jarvis is a personal voice assistant for Ildemar, built first as a native iOS app with a complementary web surface. The iOS app provides a voice-first assistant, Anthropic model selection/testing, spoken responses, and a local Second Brain context that can be injected into conversations.

## Current User-Facing Features

- Native iOS SwiftUI app with wake-word-style flow: the user says or types commands and Jarvis answers in Portuguese.
- Anthropic API key is stored locally in Keychain.
- Settings sheet for API key, model selection, model test and voice preference.
- Second Brain notes are shown in a visual graph and injected into the assistant system prompt.
- Version footer exposes app version, build date and git commit.
- Widget Extension target `JarvisWidgets` now provides a small Jarvis status widget.
- Live Activity base is implemented with ActivityKit attributes, controller methods and widget rendering, but is not started automatically from production UI yet.

## Architecture

- `ios/project.yml` is the XcodeGen source of truth for app and extension targets.
- `ios/Sources/App/` contains the SwiftUI app entry, root UI, app session state and the Live Activity controller.
- `ios/Sources/Services/` contains Anthropic and Keychain integrations.
- `ios/Sources/Voice/` contains speech recognition and synthesis wrappers.
- `ios/Sources/Brain/` contains the local Second Brain model.
- `ios/Sources/Version/` contains version display and changelog support.
- `ios/Shared/` contains code compiled into both app and widget extension targets.
- `ios/Widgets/` contains the WidgetKit bundle: a small static widget and the Live Activity/Dynamic Island presentation.

## Important Files

- `ios/project.yml`
- `ios/Sources/App/JarvisLiveActivityController.swift`
- `ios/Shared/JarvisLiveActivityAttributes.swift`
- `ios/Widgets/JarvisWidgets.swift`
- `ios/Sources/Info.plist`
- `ios/Sources/Version/VersionHistory.swift`
- `package.json`
- `scripts/build-web.sh`


## Data Model And Storage

- Anthropic API key: Keychain via `KeychainStore`.
- Selected model and voice preference: `UserDefaults`.
- Second Brain notes: local app state backed by the existing Jarvis storage helpers in `JarvisSession`.
- Widget currently uses static timeline data only; no App Group or shared container has been added.
- Live Activity state is passed through ActivityKit content state when started/updated.

## Integrations And External Services

- Anthropic API for assistant responses and model testing.
- Apple Developer Team ID: `E743636TCJ`.
- iOS app bundle id: `br.app.egger.jarvis`.
- Widget extension bundle id: `br.app.egger.jarvis.widgets` (`Z7SGYGS278` in Apple Developer).
- CarPlay entitlement request was submitted manually on 2026-07-12 and is pending Apple review.
- TestFlight/App Store release scripts live in `ios/scripts/`; never commit `ios/scripts/asc.env` or `.p8` files.

## Versioning And Release Rules

Detected version/build fields:

```json
{
  "ios_marketing_version": "1.3.0",
  "ios_current_project_version": "9",
  "ios_bundle_id": "br.app.egger.jarvis",
  "widget_bundle_id": "br.app.egger.jarvis.widgets",
  "widget_bundle_resource_id": "Z7SGYGS278"
}
```

For distributed iOS builds, use `ios/project.yml` as the source of truth, update `MARKETING_VERSION` and `CURRENT_PROJECT_VERSION`, update `VersionHistory.swift`, regenerate with `xcodegen generate`, commit and push, then run `ios/scripts/testflight.sh`. The script injects the short git commit into `GIT_COMMIT` at archive time.

## Local Development

- Regenerate Xcode project: `cd ios && xcodegen generate`.
- Build simulator app plus widget extension: `cd ios && xcodebuild -project Jarvis.xcodeproj -scheme Jarvis -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build`.
- Open in Xcode: `open ios/Jarvis.xcodeproj`.
- Web assets exist in the root/frontend areas, but this handoff update focused on native iOS.

## Testing And Validation

- 2026-07-12: `xcodegen generate` succeeded.
- 2026-07-12: `xcodebuild -project Jarvis.xcodeproj -scheme Jarvis -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build` succeeded, including `JarvisWidgets.appex` embed.
- 2026-07-12: Installed and launched Jarvis on iPhone 17 iOS 26.5 Simulator (`067DE2A0-9E13-49E6-AFA5-C78D3155EA94`).
- 2026-07-12: Confirmed `br.app.egger.jarvis.widgets` appears in simulator `pluginkit` output.
- 2026-07-12: Installed Additional Tools for Xcode 26.6 CarPlay Simulator locally. CarPlay Simulator opens, but it requires a real/remote iPhone/iPad connection and does not connect to CoreSimulator iOS devices.

## Recent Decisions

- Added the Widget Extension and Live Activity base before CarPlay entitlement approval so the project has a parallel, less blocked path for CarPlay Dashboard-style visibility.
- Kept widget data static for now to avoid adding App Groups or sharing sensitive assistant data with the extension before the expected UX is confirmed.
- Added `NSSupportsLiveActivities` to the app Info.plist and a reusable `JarvisLiveActivityController`, but did not auto-start activities from normal assistant sessions.

## Known Risks And Pending Work

- CarPlay app entitlement is pending Apple review. Full CarPlay app UI cannot be tested as a real CarPlay app until Apple approves the appropriate entitlement and provisioning profile.
- CarPlay Simulator cannot validate this widget using only the iOS Simulator. To test in CarPlay Simulator, use an iPhone with the TestFlight build installed, connect/unlock/trust it, then select/connect that device in CarPlay Simulator. This does not require direct installation from Xcode.
- Before TestFlight with the widget extension, ensure provisioning supports the embedded extension bundle id `br.app.egger.jarvis.widgets`.
- Live Activity still needs product behavior: decide when to start, update and end it from `JarvisSession`.
- Widget currently does not read live Jarvis state. If real data is desired, add an App Group and a privacy-conscious shared state model.
- Screenshots were not captured in this pass because the task was a build-level extension setup, not a visual review on a booted simulator.

## Import Notes For Other Tools

Read `IMPORT_MANIFEST.json` first, then this file, then key files listed above. Never load secret files.

# CarPlay And Widget Context

Last updated: 2026-07-13

This file exists because another AI/tool may inspect the repo and incorrectly say that the CarPlay/widget work "does not exist". The work does exist, but it is split across WidgetKit, Live Activity preparation, Apple entitlement status and CarPlay Simulator limitations.

## What Exists In The Codebase

- `ios/project.yml` defines the embedded WidgetKit extension target `JarvisWidgets`.
- `JarvisWidgets` uses bundle id `br.app.egger.jarvis.widgets`.
- The main app bundle id is `br.app.egger.jarvis`.
- The widget extension is embedded into the iOS app target through the XcodeGen dependency in `ios/project.yml`.
- `ios/Widgets/JarvisWidgets.swift` implements:
  - `JarvisStatusWidget`, a small static Jarvis status widget.
  - `JarvisLiveActivityWidget`, the WidgetKit rendering surface for Live Activity and Dynamic Island.
- `ios/Shared/JarvisLiveActivityAttributes.swift` defines the shared ActivityKit attributes/state compiled into both the app and widget extension.
- `ios/Sources/App/JarvisLiveActivityController.swift` provides app-side methods to start, update and end a Jarvis Live Activity.
- `ios/Sources/Info.plist` enables Live Activities through `NSSupportsLiveActivities`.
- `docs/development-handoff/RELEASE_LOG.md` records the implementation and TestFlight upload history for this widget work.

## What Does Not Exist Yet

- There is not yet a full CarPlay app scene/template UI in the project.
- There is not yet approved Apple CarPlay entitlement/provisioning for a full CarPlay app experience.
- The Live Activity is not yet automatically started from production Jarvis voice-session UI.
- The static widget does not yet read live Jarvis state from an App Group/shared container.
- The repo does not currently contain a CarPlay-specific target or Swift file named "CarPlay"; absence of that name is not proof that the widget/CarPlay discussion is fictional.

## Apple And Simulator Status

- A CarPlay entitlement request was submitted manually on 2026-07-12 and is pending Apple review.
- The Widget Extension Bundle ID `br.app.egger.jarvis.widgets` exists in Apple Developer/App Store Connect records. The recorded resource id is `Z7SGYGS278`.
- CarPlay Simulator from Additional Tools for Xcode 26.6 was installed locally at `~/Applications/Apple Developer Tools/CarPlay Simulator.app`.
- The 2026-07-12 CarPlay Simulator session connected to the real device `iPhone 16 de Ildemar`, while the rebuilt Jarvis app was installed on a CoreSimulator `iPhone 17`.
- Because of that device mismatch, CarPlay Simulator did not validate the locally installed simulator build.
- On 2026-07-13, the physical-device build was installed on `iPhone 16 de Ildemar`, Jarvis was launched once, widgets were enabled from iPhone CarPlay settings, and CarPlay Simulator displayed the Jarvis widget successfully.

## How To Continue The Work

1. Read this file, then `PROJECT_CONTEXT.md`, `RELEASE_LOG.md`, `SCREENSHOTS.md` and `IMPORT_MANIFEST.json`.
2. Inspect `ios/project.yml`, `ios/Widgets/JarvisWidgets.swift`, `ios/Shared/JarvisLiveActivityAttributes.swift` and `ios/Sources/App/JarvisLiveActivityController.swift`.
3. Regenerate Xcode project after project.yml changes:

   ```bash
   cd ios && xcodegen generate
   ```

4. Build simulator app plus widget extension:

   ```bash
   cd ios && xcodebuild -project Jarvis.xcodeproj -scheme Jarvis -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build
   ```

5. To validate in CarPlay Simulator, install the current Jarvis build on the iPhone that CarPlay Simulator is actually connected to, unlock/trust it, then connect that device in CarPlay Simulator.
6. After Apple approves the entitlement, add the required CarPlay capability/provisioning and then implement the CarPlay app UI using Apple's CarPlay templates.

## Summary For Other Agents

Do not conclude "nothing exists" just because there is no CarPlay scene yet. The current implemented surface is WidgetKit plus Live Activity/Dynamic Island groundwork, shipped in TestFlight starting with version `1.3.0` build `9` and still present in version `1.4.4` build `14`. Full CarPlay app UI remains pending Apple entitlement approval.

Latest validation: the Jarvis widget appeared in CarPlay Simulator on 2026-07-13 when connected to physical `iPhone 16 de Ildemar`.

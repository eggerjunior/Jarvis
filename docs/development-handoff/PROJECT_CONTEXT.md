# Project Context

Generated: 2026-07-17T13:38:01-03:00

## Snapshot

- Project: `Jarvis@apvictorio`
- Root: `/Users/ildemareggerjunior/Projects/Jarvis@apvictorio`
- Branch: `main`
- Commit: `dd450094`
- Git status: dirty
- iOS version: `1.5.0`
- iOS build: `15`
- iOS bundle id: `br.app.egger.jarvis`
- Widget extension bundle id: `br.app.egger.jarvis.widgets`
- Detected stack: swift, node

## Product Purpose

Jarvis is a personal voice assistant for Ildemar, built first as a native iOS app with a complementary web surface. The iOS app provides a voice-first assistant, Anthropic/OpenRouter model selection, spoken responses, and a local Second Brain context that can be injected into conversations.

## Current User-Facing Features

- Native iOS SwiftUI app with wake-word-style flow: the user says or types commands and Jarvis answers in Portuguese.
- App automatically requests activation when the main view opens and whenever the scene returns to active.
- Anthropic and OpenRouter API keys are stored locally in Keychain.
- Settings sheet for provider selection, provider-specific API key, model selection/testing, voice preference, installed iOS voice selection and voice preview.
- Model test reports provider, requested model, response model, token usage and returned text; cost/spend lookup was removed because it was not reliable for the normal user key flow.
- Second Brain notes are shown in a visual graph and a card grid; tapping graph bubbles or cards opens an editor for title, area and memory/context text.
- After Jarvis finishes speaking with a follow-up expected, speech that starts within a 5-second continuation window keeps the conversation open even if final transcription arrives later.
- Voice recognition waits longer through natural pauses inside a command and extends the wait when the partial phrase suggests continuation.
- Voice synthesis can use an explicit installed iOS voice selected by the user, with a test button in settings.
- Voice synthesis prepares the audio session before speaking so the first activation after app launch can say the startup line reliably.
- Questions that require current information are routed through OpenRouter with the `openrouter:web_search` server tool when an OpenRouter key is configured.
- Version footer exposes app version, build date and git commit.
- Widget Extension target `JarvisWidgets` provides a small Jarvis status widget.
- Live Activity base is implemented with ActivityKit attributes, controller methods and widget rendering, but is not started automatically from production UI yet.
- CarPlay scene (`CarPlaySceneDelegate`) shows a list template with an item that activates the Jarvis voice session from the car screen via `NotificationCenter` (`.jarvisCarPlayActivate`), reusing the existing `JarvisSession` instance in `RootView`.

## Architecture

- `ios/project.yml` is the XcodeGen source of truth for app and extension targets (settings, Info.plist properties, `CODE_SIGN_ENTITLEMENTS`).
- `ios/Sources/App/` contains the SwiftUI app entry, root UI, app session state, the Live Activity controller and the CarPlay scene delegate.
- `ios/Sources/Services/AnthropicClient.swift` currently contains the multi-provider AI client for Anthropic Messages API and OpenRouter chat completions.
- `ios/Sources/Voice/` contains speech recognition and synthesis wrappers.
- `ios/Sources/Brain/` contains the local Second Brain model.
- `ios/Sources/Version/` contains version display and changelog support.
- `ios/Shared/` contains code compiled into both app and widget extension targets.
- `ios/Widgets/` contains the WidgetKit bundle: a small static widget and the Live Activity/Dynamic Island presentation.

## Important Files

- `ios/project.yml`
- `ios/Sources/App/JarvisSession.swift`
- `ios/Sources/App/RootView.swift`
- `ios/Sources/App/CarPlaySceneDelegate.swift`
- `ios/Sources/Jarvis.entitlements`
- `ios/Sources/Services/AnthropicClient.swift`
- `ios/Sources/Voice/SpeechRecognizer.swift`
- `ios/Sources/Voice/SpeechSynthesizer.swift`
- `ios/Sources/App/JarvisLiveActivityController.swift`
- `ios/Shared/JarvisLiveActivityAttributes.swift`
- `ios/Widgets/JarvisWidgets.swift`
- `ios/Sources/Info.plist`
- `ios/Sources/Version/VersionHistory.swift`
- `package.json`
- `scripts/build-web.sh`


## Data Model And Storage

- Anthropic API key: Keychain key `anthropic_key`.
- OpenRouter API key: Keychain key `openrouter_key`.
- Selected provider: `UserDefaults` key `ai_provider`.
- Selected model per provider: `UserDefaults` keys `anthropic_model` and `openrouter_model`.
- Voice preference: `UserDefaults` key `jarvis_voice_preference`.
- Explicit installed iOS voice identifier: `UserDefaults` key `jarvis_voice_identifier`.
- Second Brain notes: JSON-encoded `[BrainNote]` in `UserDefaults` key `jarvis_notes`.
- Widget currently uses static timeline data only; no App Group or shared container has been added.
- Live Activity state is passed through ActivityKit content state when started/updated.

## Integrations And External Services

- Anthropic Messages API for direct Claude responses.
- OpenRouter chat completions endpoint for aggregator/model-router access, including server-side web search for current-information questions.
- Apple Developer Team ID: `E743636TCJ`.
- iOS app bundle id: `br.app.egger.jarvis`.
- Widget extension bundle id: `br.app.egger.jarvis.widgets` (`Z7SGYGS278` in Apple Developer).
- CarPlay entitlement (`CarPlay Voice Based Conversation`, capability key `com.apple.developer.carplay-voice-based-conversation`) was submitted on 2026-07-12 and was confirmed **approved** by Apple on 2026-07-17: enabled directly on the `br.app.egger.jarvis` App ID in Certificates, Identifiers & Profiles and wired into the app in this same release.
- TestFlight/App Store release scripts live in `ios/scripts/`; never commit `ios/scripts/asc.env` or `.p8` files.

## Versioning And Release Rules

Detected version/build fields:

```json
{
  "ios_marketing_version": "1.5.0",
  "ios_current_project_version": "15",
  "ios_bundle_id": "br.app.egger.jarvis",
  "widget_bundle_id": "br.app.egger.jarvis.widgets",
  "widget_bundle_resource_id": "Z7SGYGS278"
}
```

For distributed iOS builds, use `ios/project.yml` as the source of truth, update `MARKETING_VERSION` and `CURRENT_PROJECT_VERSION`, update `VersionHistory.swift`, regenerate with `xcodegen generate`, commit and push, then run `ios/scripts/testflight.sh`. The script injects the short git commit into `GIT_COMMIT` at archive time.

## Local Development

- Regenerate Xcode project: `cd ios && xcodegen generate`.
- Build simulator app plus widget extension: `cd ios && xcodebuild -project Jarvis.xcodeproj -scheme Jarvis -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build`.
- Build physical device app: `cd ios && xcodebuild -project Jarvis.xcodeproj -scheme Jarvis -destination 'platform=iOS,id=<device-id>' build`.
- Open in Xcode: `open ios/Jarvis.xcodeproj`.
- Web assets exist in the root/frontend areas, but current handoff work focuses on native iOS.

## Testing And Validation

- 2026-07-17: `xcodebuild -project ios/Jarvis.xcodeproj -scheme Jarvis -destination 'generic/platform=iOS' -configuration Debug CODE_SIGNING_ALLOWED=NO build` succeeded after hand-adding `CarPlaySceneDelegate.swift`, `Jarvis.entitlements` and the CarPlay scene manifest to `project.pbxproj`.
- 2026-07-17: `cd ios && xcodegen generate` succeeded for version `1.5.0` build `15` after moving the CarPlay entitlement/Info.plist scene manifest into `project.yml` as the source of truth.
- 2026-07-17: `xcodebuild -project ios/Jarvis.xcodeproj -scheme Jarvis -destination 'generic/platform=iOS' -configuration Debug CODE_SIGNING_ALLOWED=NO build` succeeded again on the XcodeGen-regenerated project.
- 2026-07-13: `xcodebuild -project ios/Jarvis.xcodeproj -scheme Jarvis -destination 'generic/platform=iOS Simulator' -configuration Debug build` succeeded after adding automatic activation on app open/foreground.
- 2026-07-13: `cd ios && ./scripts/testflight.sh` archived and uploaded version `1.4.4` build `14` successfully to App Store Connect/TestFlight; binary commit `3435f245`, package processing started.
- 2026-07-13: Installed and launched Jarvis on iPhone 17 iOS 26.5 Simulator (`067DE2A0-9E13-49E6-AFA5-C78D3155EA94`) and captured `ios-simulator-auto-activation-2026-07-13.png`.
- 2026-07-13: Extracted and reviewed TestFlight feedback zips `testflight_feedback-2.zip` through `testflight_feedback-6.zip`.
- 2026-07-12: Installed Additional Tools for Xcode 26.6 CarPlay Simulator locally. CarPlay Simulator opens, but it connects to a real/remote iPhone/iPad, not to CoreSimulator iOS devices.

## Recent Decisions

- CarPlay entitlement approved by Apple on 2026-07-17; enabled the `CarPlay Voice Based Conversation` capability on the `br.app.egger.jarvis` App ID and added `Jarvis.entitlements` (`com.apple.developer.carplay-voice-based-conversation = true`), a `CPTemplateApplicationSceneSessionRoleApplication` scene entry in Info.plist, `UIBackgroundModes: audio`, and `CarPlaySceneDelegate.swift` implementing the standard `CPTemplateApplicationSceneDelegate` connect/disconnect flow with a `CPListTemplate` that activates the existing `JarvisSession` via `NotificationCenter`. The exact delegate API surface for the newer "voice based conversation" CarPlay interaction (beyond standard template connect) was not fully verified against the latest CarPlay SDK docs/headers and may need refinement once tested against a real CarPlay-connected device or the CarPlay Simulator.
- This is a native-only OS integration (CarPlay has no web equivalent), so the web surface (`jarvis.html`) was intentionally not touched for this change.
- RootView now calls `session.start()` when the main view task first runs and when `scenePhase` returns to `.active`, and also when a CarPlay activation notification is received.
- `JarvisSession.start()` now tracks `isStarting` and a request UUID to avoid duplicate permission/activation flows and to ignore stale async permission results after a stop.
- The activation button shows `Ativando` and is disabled while permissions/activation are in flight.
- Prepared the `AVAudioSession` inside `JarvisSpeechSynthesizer` before each `speak`/`preview` call so the startup utterance is not lost on the first activation after launch or permission setup.
- Added OpenRouter as a second provider instead of replacing Anthropic, preserving the known working direct Claude flow.
- Removed cost/spend lookup from model testing because normal API keys do not reliably have admin-cost access and the feedback asked to remove that noisy result.
- Increased the follow-up window from 2 seconds to 5 seconds; partial speech detected inside that window keeps direct-command mode active until final transcription arrives.
- Routed current-information questions to OpenRouter web search instead of relying on static model knowledge.
- Added an explicit installed-voice picker because iOS system voice availability varies and automatic male selection can fall back to a female/default voice.
- Added Second Brain editing in-place through graph bubbles and grid cards rather than creating a separate memory management screen.

## Known Risks And Pending Work

- CarPlay scene has not yet been validated on a real CarPlay-connected device or in CarPlay Simulator (CarPlay Simulator connects to a real/remote iPhone/iPad, not CoreSimulator devices); do this before relying on the feature in daily use.
- The CarPlay delegate implements the standard `CPTemplateApplicationSceneDelegate` connect/disconnect + `CPListTemplate` flow; if Apple's "Voice Based Conversation" capability expects additional delegate methods or template types beyond this baseline, they still need to be added once verified against current CarPlay framework docs/headers in a recent Xcode SDK.
- First launch after install may show iOS speech/microphone permission prompts before the app can become fully active and speak.
- OpenRouter responses and web search depend on the selected model slug, account credit and OpenRouter server-tool availability.
- Current-information questions need an OpenRouter key; Anthropic-only configuration cannot perform web search from the app.
- AVSpeechSynthesisVoice availability varies by device and installed voices.
- Live Activity still needs product behavior: decide when to start, update and end it from `JarvisSession`.
- Widget currently does not read live Jarvis state. If real data is desired, add an App Group and a privacy-conscious shared state model.

## Import Notes For Other Tools

Read `IMPORT_MANIFEST.json` first, then this file, then key files listed above. Never load secret files.

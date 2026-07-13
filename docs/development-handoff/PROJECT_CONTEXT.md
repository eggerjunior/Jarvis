# Project Context

Generated: 2026-07-13T09:42:12-03:00

## Snapshot

- Project: `Jarvis@apvictorio`
- Root: `/Users/ildemareggerjunior/Projects/Jarvis@apvictorio`
- Branch: `main`
- Commit at start of current pass: `f5acf97`
- Git status: dirty
- iOS version: `1.4.3`
- iOS build: `13`
- iOS bundle id: `br.app.egger.jarvis`
- Widget extension bundle id: `br.app.egger.jarvis.widgets`
- Detected stack: swift, node

## Product Purpose

Jarvis is a personal voice assistant for Ildemar, built first as a native iOS app with a complementary web surface. The iOS app provides a voice-first assistant, Anthropic/OpenRouter model selection, spoken responses, and a local Second Brain context that can be injected into conversations.

## Current User-Facing Features

- Native iOS SwiftUI app with wake-word-style flow: the user says or types commands and Jarvis answers in Portuguese.
- Anthropic and OpenRouter API keys are stored locally in Keychain.
- Settings sheet for provider selection, provider-specific API key, model selection/testing, voice preference, installed iOS voice selection and voice preview.
- Model test reports provider, requested model, response model, token usage and returned text; cost/spend lookup was removed because it was not reliable for the normal user key flow.
- Second Brain notes are shown in a visual graph and a card grid; tapping graph bubbles or cards opens an editor for title, area and memory/context text.
- After Jarvis finishes speaking with a follow-up expected, speech that starts within a 5-second continuation window keeps the conversation open even if final transcription arrives later.
- Voice recognition now waits longer through natural pauses inside a command and extends the wait when the partial phrase suggests continuation.
- Voice synthesis can use an explicit installed iOS voice selected by the user, with a test button in settings.
- Questions that require current information are routed through OpenRouter with the `openrouter:web_search` server tool when an OpenRouter key is configured.
- Version footer exposes app version, build date and git commit.
- Widget Extension target `JarvisWidgets` provides a small Jarvis status widget.
- Live Activity base is implemented with ActivityKit attributes, controller methods and widget rendering, but is not started automatically from production UI yet.

## Architecture

- `ios/project.yml` is the XcodeGen source of truth for app and extension targets.
- `ios/Sources/App/` contains the SwiftUI app entry, root UI, app session state and the Live Activity controller.
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
- CarPlay entitlement request was submitted manually on 2026-07-12 and is pending Apple review.
- TestFlight/App Store release scripts live in `ios/scripts/`; never commit `ios/scripts/asc.env` or `.p8` files.

## Versioning And Release Rules

Detected version/build fields:

```json
{
  "ios_marketing_version": "1.4.3",
  "ios_current_project_version": "13",
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
- Web assets exist in the root/frontend areas, but this handoff update focused on native iOS feedback.

## Testing And Validation

- 2026-07-13: Extracted and reviewed TestFlight feedback zips `testflight_feedback-2.zip` through `testflight_feedback-6.zip`.
- 2026-07-13: `cd ios && xcodegen generate` succeeded.
- 2026-07-13: `cd ios && xcodebuild -project Jarvis.xcodeproj -scheme Jarvis -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath ../build/DerivedDataFeedback build` succeeded for version `1.4.0` build `10`.
- 2026-07-13: `cd ios && xcodegen generate` succeeded for version `1.4.1` build `11`.
- 2026-07-13: `cd ios && xcodebuild -project Jarvis.xcodeproj -scheme Jarvis -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath ../build/DerivedDataVoiceFollowup build` succeeded.
- 2026-07-13: `cd ios && ./scripts/testflight.sh` archived and uploaded version `1.4.1` build `11` successfully to App Store Connect/TestFlight.
- 2026-07-13: Extracted and reviewed TestFlight feedback `testflight_feedback.zip` for version `1.4.1` build `11`: speech recognition finalized after a mid-sentence pause and current-information questions needed web search.
- 2026-07-13: `cd ios && xcodegen generate` succeeded for version `1.4.2` build `12`.
- 2026-07-13: `cd ios && xcodebuild -project Jarvis.xcodeproj -scheme Jarvis -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath ../build/DerivedDataFeedback142 build` succeeded.
- 2026-07-13: `cd ios && ./scripts/testflight.sh` archived and uploaded version `1.4.2` build `12` successfully to App Store Connect/TestFlight.
- 2026-07-13: `cd ios && xcodegen generate` succeeded for version `1.4.3` build `13`.
- 2026-07-13: `cd ios && xcodebuild -project Jarvis.xcodeproj -scheme Jarvis -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath ../build/DerivedDataVoicePicker143 build` succeeded.
- 2026-07-13: Installed and launched Jarvis on iPhone 17 iOS 26.5 Simulator (`067DE2A0-9E13-49E6-AFA5-C78D3155EA94`) and captured `ios-simulator-feedback-adjustments-2026-07-13.png`.
- 2026-07-12: Installed Additional Tools for Xcode 26.6 CarPlay Simulator locally. CarPlay Simulator opens, but it connects to a real/remote iPhone/iPad, not to CoreSimulator iOS devices.

## Recent Decisions

- Added OpenRouter as a second provider instead of replacing Anthropic, preserving the known working direct Claude flow.
- Removed cost/spend lookup from model testing because normal API keys do not reliably have admin-cost access and the feedback asked to remove that noisy result.
- Increased the follow-up window from 2 seconds to 5 seconds; partial speech detected inside that window keeps direct-command mode active until final transcription arrives.
- Increased in-command silence tolerance from 1 second to 3 seconds, with a 5-second wait for partial phrases that likely continue.
- Routed current-information questions to OpenRouter web search instead of relying on static model knowledge.
- Added an explicit installed-voice picker because iOS system voice availability varies and automatic male selection can fall back to a female/default voice.
- Added Second Brain editing in-place through graph bubbles and grid cards rather than creating a separate memory management screen.
- Voice preference now tries known Portuguese male/female voice identifiers and names first; masculine fallback applies lower pitch and slower rate when iOS still falls back to the default pt-BR voice.

## Known Risks And Pending Work

- OpenRouter responses and web search depend on the selected model slug, account credit and OpenRouter server-tool availability; the app stores only the API key and does not yet fetch the live OpenRouter model catalog.
- Current-information questions need an OpenRouter key; Anthropic-only configuration cannot perform web search from the app.
- AVSpeechSynthesisVoice availability varies by device and installed voices; the user can now explicitly select and test installed voices, but a truly native male pt-BR timbre still depends on system voice availability.
- CarPlay app entitlement is pending Apple review. Full CarPlay app UI cannot be tested as a real CarPlay app until Apple approves the appropriate entitlement and provisioning profile.
- CarPlay Simulator cannot validate this widget using only the iOS Simulator. To test in CarPlay Simulator, use an iPhone with the build installed, connect/unlock/trust it, then select/connect that device in CarPlay Simulator.
- Live Activity still needs product behavior: decide when to start, update and end it from `JarvisSession`.
- Widget currently does not read live Jarvis state. If real data is desired, add an App Group and a privacy-conscious shared state model.

## Import Notes For Other Tools

Read `IMPORT_MANIFEST.json` first, then this file, then key files listed above. Never load secret files.

import ActivityKit
import SwiftUI
import WidgetKit

@main
struct JarvisWidgetBundle: WidgetBundle {
    var body: some Widget {
        JarvisStatusWidget()
        JarvisLiveActivityWidget()
    }
}

struct JarvisStatusWidget: Widget {
    let kind = "JarvisStatusWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: JarvisStatusProvider()) { entry in
            JarvisStatusWidgetView(entry: entry)
                .containerBackground(Color.black, for: .widget)
        }
        .configurationDisplayName("Jarvis")
        .description("Status rápido do assistente para consultas por voz.")
        .supportedFamilies([.systemSmall, .accessoryRectangular, .accessoryCircular])
    }
}

struct JarvisStatusEntry: TimelineEntry {
    let date: Date
    let status: String
    let detail: String
}

struct JarvisStatusProvider: TimelineProvider {
    func placeholder(in context: Context) -> JarvisStatusEntry {
        JarvisStatusEntry(date: Date(), status: "Pronto", detail: "Assistente de voz")
    }

    func getSnapshot(in context: Context, completion: @escaping (JarvisStatusEntry) -> Void) {
        completion(placeholder(in: context))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<JarvisStatusEntry>) -> Void) {
        let entry = JarvisStatusEntry(date: Date(), status: "Pronto", detail: "Diga Ei Jarvis")
        completion(Timeline(entries: [entry], policy: .after(Date().addingTimeInterval(30 * 60))))
    }
}

struct JarvisStatusWidgetView: View {
    let entry: JarvisStatusEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        switch family {
        case .accessoryCircular:
            ZStack {
                AccessoryWidgetBackground()
                Image(systemName: "waveform.circle.fill")
                    .font(.title2)
                    .foregroundStyle(.cyan)
            }
        case .accessoryRectangular:
            VStack(alignment: .leading, spacing: 2) {
                Text("Jarvis")
                    .font(.headline)
                Text(entry.status)
                    .font(.caption)
                    .foregroundStyle(.cyan)
            }
        default:
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Image(systemName: "waveform.circle.fill")
                        .font(.title2)
                        .foregroundStyle(.cyan)
                    Spacer()
                    Circle()
                        .fill(.green)
                        .frame(width: 8, height: 8)
                }

                Spacer()

                Text("Jarvis")
                    .font(.headline.weight(.bold))
                Text(entry.detail)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(2)
            }
            .foregroundStyle(.white)
        }
    }
}

struct JarvisLiveActivityWidget: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: JarvisLiveActivityAttributes.self) { context in
            JarvisLiveActivityView(state: context.state)
                .activityBackgroundTint(.black)
                .activitySystemActionForegroundColor(.cyan)
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    Label(context.state.status, systemImage: "waveform")
                        .foregroundStyle(.cyan)
                }

                DynamicIslandExpandedRegion(.trailing) {
                    Text(context.state.updatedAt, style: .time)
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                }

                DynamicIslandExpandedRegion(.bottom) {
                    Text(context.state.response)
                        .font(.caption)
                        .lineLimit(2)
                }
            } compactLeading: {
                Image(systemName: "waveform")
                    .foregroundStyle(.cyan)
            } compactTrailing: {
                Text("J")
                    .font(.caption.weight(.bold))
            } minimal: {
                Image(systemName: "waveform")
                    .foregroundStyle(.cyan)
            }
            .widgetURL(URL(string: "jarvis://activity"))
            .keylineTint(.cyan)
        }
    }
}

struct JarvisLiveActivityView: View {
    let state: JarvisLiveActivityAttributes.ContentState

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Label(state.status, systemImage: "waveform")
                    .font(.headline)
                    .foregroundStyle(.cyan)
                Spacer()
                Text(state.updatedAt, style: .time)
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }

            Text(state.prompt)
                .font(.caption)
                .foregroundStyle(.secondary)
                .lineLimit(1)

            Text(state.response)
                .font(.subheadline.weight(.semibold))
                .lineLimit(2)
        }
        .padding()
        .foregroundStyle(.white)
    }
}

#Preview("Widget", as: .systemSmall) {
    JarvisStatusWidget()
} timeline: {
    JarvisStatusEntry(date: Date(), status: "Pronto", detail: "Diga Ei Jarvis")
}

#Preview("Live Activity", as: .content, using: JarvisLiveActivityAttributes.preview) {
    JarvisLiveActivityWidget()
} contentStates: {
    JarvisLiveActivityAttributes.ContentState.preview
}

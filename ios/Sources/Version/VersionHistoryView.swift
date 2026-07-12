import SwiftUI

struct VersionHistoryView: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            List {
                ForEach(VersionHistory.entries) { entry in
                    Section {
                        VStack(alignment: .leading, spacing: 10) {
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text("v\(entry.version) (Build \(entry.build))")
                                        .font(.headline)
                                    Text(entry.isCurrent ? VersionManager.shared.currentBuildDateString : entry.date)
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                                Spacer()
                                if entry.isCurrent {
                                    Text("Atual")
                                        .font(.caption.weight(.bold))
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 4)
                                        .background(.green.opacity(0.18), in: Capsule())
                                        .foregroundStyle(.green)
                                }
                            }

                            ForEach(entry.changes, id: \.self) { change in
                                HStack(alignment: .top, spacing: 8) {
                                    Text("•")
                                        .foregroundStyle(.cyan)
                                    Text(change)
                                        .font(.subheadline)
                                }
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }
            }
            .navigationTitle("Histórico")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("OK") { dismiss() }
                }
            }
        }
    }
}

import Foundation

final class VersionManager {
    static let shared = VersionManager()

    private init() {}

    var currentVersionString: String {
        let info = Bundle.main.infoDictionary
        let version = info?["CFBundleShortVersionString"] as? String ?? VersionHistory.currentVersionFallback
        let build = info?["CFBundleVersion"] as? String ?? VersionHistory.currentBuildFallback
        return "\(version) (Build \(build))"
    }

    var currentCommit: String {
        let raw = Bundle.main.infoDictionary?["GitCommit"] as? String
        let commit = raw?.trimmingCharacters(in: .whitespacesAndNewlines)
        return commit?.isEmpty == false ? commit! : VersionHistory.currentCommitFallback
    }

    var commitURL: URL? {
        guard currentCommit != "dev" else { return nil }
        return URL(string: "https://github.com/eggerjunior/Jarvis/commit/\(currentCommit)")
    }

    var currentBuildDateString: String {
        guard let executableURL = Bundle.main.executableURL,
              let values = try? executableURL.resourceValues(forKeys: [.contentModificationDateKey]),
              let date = values.contentModificationDate else {
            return "build local"
        }
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "pt_BR")
        formatter.dateStyle = .short
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

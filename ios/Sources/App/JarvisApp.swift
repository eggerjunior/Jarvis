import SwiftUI

@main
struct JarvisApp: App {
    @StateObject private var session = JarvisSession.shared

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(session)
        }
    }
}

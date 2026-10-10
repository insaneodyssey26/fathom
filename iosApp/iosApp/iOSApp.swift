import SwiftUI
import SharedLogic

@main
struct iOSApp: App {
    private let graph = IosAppGraph()

    var body: some Scene {
        WindowGroup {
            MainShellView(graph: graph)
                .preferredColorScheme(.dark)
        }
    }
}
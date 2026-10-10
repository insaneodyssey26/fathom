import SwiftUI
import SharedLogic

@MainActor
public final class JournalViewModel: ObservableObject {
    private let graph: IosAppGraph

    @Published public var streakDays: Int = 0
    @Published public var totalVersesRead: Int = 0
    @Published public var savedBookmarks: [Passage] = []
    @Published public var reflections: [Reflection] = []
    @Published public var selectedTab: Int = 0

    public init(graph: IosAppGraph) {
        self.graph = graph
        loadJournalData()
    }

    public func loadJournalData() {
        Task {
            let streakResult = try? await graph.userRepository.getReadingStreak()
            if let result = streakResult as? FathomResultSuccess<ReadingStreak>, let streak = result.data {
                self.streakDays = Int(streak.currentStreakDays)
            }

            let bookmarksResult = try? await graph.userRepository.getBookmarkedPassages()
            if let result = bookmarksResult as? FathomResultSuccess<NSArray>, let list = result.data as? [Passage] {
                self.savedBookmarks = list
            }

            let reflectionsResult = try? await graph.userRepository.getAllReflections()
            if let result = reflectionsResult as? FathomResultSuccess<NSArray>, let list = result.data as? [Reflection] {
                self.reflections = list
            }
        }
    }
}

public struct JournalView: View {
    @ObservedObject public var viewModel: JournalViewModel
    public let colors: ThemeColors
    public let onOpenSettings: () -> Void

    public var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()

            VStack(spacing: 0) {
                HStack {
                    Text("journal")
                        .font(.system(size: 26, weight: .bold, design: .serif))
                        .foregroundColor(colors.textPrimary)
                    Spacer()

                    Button(action: { onOpenSettings() }) {
                        Image(systemName: "gearshape")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(colors.textPrimary)
                            .padding(10)
                            .liquidGlass(cornerRadius: 50)
                    }
                }
                .padding(.horizontal, 22)
                .padding(.top, 10)
                .padding(.bottom, 16)

                ScrollView {
                    VStack(alignment: .leading, spacing: 24) {
                        HStack(spacing: 16) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("\(viewModel.streakDays)")
                                    .font(.system(size: 38, weight: .bold, design: .serif))
                                    .foregroundColor(TraditionTones.hue(for: nil))
                                Text("Day reading streak")
                                    .font(.system(size: 13, weight: .medium))
                                    .foregroundColor(colors.textSecondary)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(20)
                            .liquidGlass(cornerRadius: 22)

                            VStack(alignment: .leading, spacing: 4) {
                                Text("\(viewModel.savedBookmarks.count)")
                                    .font(.system(size: 38, weight: .bold, design: .serif))
                                    .foregroundColor(colors.textPrimary)
                                Text("Saved passages")
                                    .font(.system(size: 13, weight: .medium))
                                    .foregroundColor(colors.textSecondary)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(20)
                            .liquidGlass(cornerRadius: 22)
                        }

                        VStack(alignment: .leading, spacing: 14) {
                            Text("SAVED PASSAGES")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(colors.textTertiary)
                                .tracking(1.2)

                            if viewModel.savedBookmarks.isEmpty {
                                Text("No saved passages yet. Tap the bookmark button on any verse to keep it here.")
                                    .font(.system(size: 14))
                                    .foregroundColor(colors.textSecondary)
                                    .padding(.vertical, 12)
                            } else {
                                ForEach(viewModel.savedBookmarks, id: \.id) { passage in
                                    VStack(alignment: .leading, spacing: 8) {
                                        HStack {
                                            Text(passage.canonicalReference)
                                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                                .foregroundColor(TraditionTones.hue(for: passage.traditionId))
                                            Spacer()
                                            Text(passage.traditionId.displayName)
                                                .font(.system(size: 11, design: .monospaced))
                                                .foregroundColor(colors.textTertiary)
                                        }

                                        Text(passage.translation)
                                            .font(.system(size: 15, weight: .regular, design: .serif))
                                            .foregroundColor(colors.textPrimary)
                                            .lineSpacing(4)
                                    }
                                    .padding(16)
                                    .liquidGlass(cornerRadius: 18)
                                }
                            }
                        }
                    }
                    .padding(.horizontal, 22)
                    .padding(.bottom, 120)
                }
            }
        }
        .onAppear {
            viewModel.loadJournalData()
        }
    }
}

import SwiftUI
import SharedLogic

@MainActor
public final class JournalViewModel: ObservableObject {
    private let graph: IosAppGraph

    @Published public var streakDays: Int = 0
    @Published public var savedBookmarks: [Passage] = []
    @Published public var savedNotes: [UserNote] = []
    @Published public var selectedSegment: Int = 0

    public init(graph: IosAppGraph) {
        self.graph = graph
        loadJournalData()
    }

    public func loadJournalData() {
        Task {
            if let streak = try? await graph.getReadingStreakDays() {
                self.streakDays = Int(streak)
            }
            if let bookmarks = try? await graph.getBookmarkedPassages() {
                self.savedBookmarks = bookmarks
            }
            if let notes = try? await graph.getAllNotes() {
                self.savedNotes = notes
            }
        }
    }
}

public struct JournalView: View {
    @ObservedObject public var viewModel: JournalViewModel
    public let colors: ThemeColors
    public let onOpenSettings: () -> Void

    @State private var showSanctuaryHub: Bool = false

    public var body: some View {
        let tone = TraditionTones.hue(for: nil)

        ZStack {
            colors.background.ignoresSafeArea()

            VStack(spacing: 0) {
                HStack {
                    Text("journal")
                        .font(.system(size: 26, weight: .bold, design: .serif))
                        .foregroundColor(colors.textPrimary)
                    Spacer()

                    HStack(spacing: 8) {
                        Button(action: { showSanctuaryHub = true }) {
                            Image(systemName: "sparkles")
                                .font(.system(size: 15, weight: .semibold))
                                .foregroundColor(tone)
                                .padding(10)
                                .liquidGlass(cornerRadius: 50)
                        }

                        Button(action: { onOpenSettings() }) {
                            Image(systemName: "gearshape")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(colors.textPrimary)
                                .padding(10)
                                .liquidGlass(cornerRadius: 50)
                        }
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
                                    .foregroundColor(tone)
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

                        HStack(spacing: 10) {
                            segmentTab(title: "Passages (\(viewModel.savedBookmarks.count))", index: 0)
                            segmentTab(title: "Reflections (\(viewModel.savedNotes.count))", index: 1)
                        }

                        if viewModel.selectedSegment == 0 {
                            if viewModel.savedBookmarks.isEmpty {
                                Text("No saved passages yet. Tap the bookmark icon on any verse to keep it in your sanctuary.")
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
                        } else {
                            if viewModel.savedNotes.isEmpty {
                                Text("No written reflections yet. Tap 'Reflect' on any passage card to record your thoughts.")
                                    .font(.system(size: 14))
                                    .foregroundColor(colors.textSecondary)
                                    .padding(.vertical, 12)
                            } else {
                                ForEach(viewModel.savedNotes, id: \.id) { note in
                                    VStack(alignment: .leading, spacing: 8) {
                                        HStack {
                                            Text("CONTEMPLATION NOTE")
                                                .font(.system(size: 10, weight: .bold, design: .monospaced))
                                                .foregroundColor(tone)
                                            Spacer()
                                        }

                                        Text(note.content)
                                            .font(.system(size: 15, weight: .regular))
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
        .sheet(isPresented: $showSanctuaryHub) {
            SanctuaryHubSheet(
                colors: colors,
                streakDays: viewModel.streakDays,
                notesCount: viewModel.savedNotes.count,
                bookmarksCount: viewModel.savedBookmarks.count,
                onOpenSettings: onOpenSettings
            )
        }
    }

    private func segmentTab(title: String, index: Int) -> some View {
        let isSelected = viewModel.selectedSegment == index
        let tone = TraditionTones.hue(for: nil)

        return Button(action: {
            FathomHaptics.light()
            viewModel.selectedSegment = index
        }) {
            Text(title)
                .font(.system(size: 13, weight: isSelected ? .semibold : .medium))
                .foregroundColor(isSelected ? colors.textPrimary : colors.textSecondary)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? tone.opacity(0.15) : colors.surfaceElevated, in: Capsule())
                .overlay(Capsule().strokeBorder(isSelected ? tone.opacity(0.6) : colors.hairline, lineWidth: 1))
        }
    }
}

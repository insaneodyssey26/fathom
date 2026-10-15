import SwiftUI
import SharedLogic

public struct ContemplationTopic: Identifiable {
    public let id: String
    public let title: String
    public let subtitle: String
    public let icon: String
    public let searchQuery: String
    public let accentTone: Color

    public init(id: String, title: String, subtitle: String, icon: String, searchQuery: String, accentTone: Color) {
        self.id = id
        self.title = title
        self.subtitle = subtitle
        self.icon = icon
        self.searchQuery = searchQuery
        self.accentTone = accentTone
    }
}

@MainActor
public final class SearchViewModel: ObservableObject {
    private let graph: IosAppGraph

    @Published public var query: String = ""
    @Published public var searchResults: [Passage] = []
    @Published public var allBooks: [Book] = []
    @Published public var selectedTraditionFilter: TraditionId? = nil
    @Published public var selectedTopic: ContemplationTopic? = nil
    @Published public var isSearching: Bool = false

    public let topics: [ContemplationTopic] = [
        ContemplationTopic(id: "stillness", title: "Stillness", subtitle: "Inner peace & silence", icon: "sparkles", searchQuery: "peace silence stillness calm", accentTone: Color(red: 0.40, green: 0.85, blue: 0.80)),
        ContemplationTopic(id: "courage", title: "Courage", subtitle: "Strength & resilience", icon: "shield.lefthalf.filled", searchQuery: "courage strength fear overcome", accentTone: Color(red: 0.95, green: 0.65, blue: 0.35)),
        ContemplationTopic(id: "grief", title: "Grief & Healing", subtitle: "Solace in suffering", icon: "heart.slash", searchQuery: "grief sorrow sorrow healing comfort", accentTone: Color(red: 0.75, green: 0.60, blue: 0.90)),
        ContemplationTopic(id: "purpose", title: "Duty & Purpose", subtitle: "Sacred action & path", icon: "arrow.up.right.circle", searchQuery: "duty action righteous path purpose", accentTone: Color(red: 0.90, green: 0.75, blue: 0.40)),
        ContemplationTopic(id: "compassion", title: "Compassion", subtitle: "Mercy & benevolence", icon: "hands.sparkles", searchQuery: "compassion mercy kindness love", accentTone: Color(red: 0.90, green: 0.45, blue: 0.55)),
        ContemplationTopic(id: "mortality", title: "Mortality & Time", subtitle: "The transient nature", icon: "hourglass", searchQuery: "impermanent death transient time eternal", accentTone: Color(red: 0.60, green: 0.75, blue: 0.85))
    ]

    public init(graph: IosAppGraph) {
        self.graph = graph
        loadBooks()
    }

    public func loadBooks() {
        Task {
            let result = try? await graph.passageRepository.getAllBooks()
            if let result = result as? FathomResultSuccess<NSArray>, let books = result.data as? [Book] {
                self.allBooks = books
            }
        }
    }

    public func selectTopic(_ topic: ContemplationTopic) {
        FathomHaptics.medium()
        if selectedTopic?.id == topic.id {
            selectedTopic = nil
            query = ""
            searchResults = []
        } else {
            selectedTopic = topic
            query = topic.searchQuery
            performSearch()
        }
    }

    public func performSearch() {
        let trimmed = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            searchResults = []
            isSearching = false
            return
        }

        isSearching = true
        Task {
            let result = try? await graph.passageRepository.searchPassages(
                query: trimmed,
                traditionId: selectedTraditionFilter,
                offset: 0,
                limit: 50
            )

            if let result = result as? FathomResultSuccess<NSArray>, let list = result.data as? [Passage] {
                self.searchResults = list
                self.isSearching = false
            } else {
                self.searchResults = []
                self.isSearching = false
            }
        }
    }

    public func setTraditionFilter(_ tradition: TraditionId?) {
        self.selectedTraditionFilter = tradition
        if !query.isEmpty {
            performSearch()
        }
    }

    public func clearSearch() {
        query = ""
        selectedTopic = nil
        searchResults = []
        isSearching = false
    }
}

public struct SearchView: View {
    @ObservedObject public var viewModel: SearchViewModel
    public let colors: ThemeColors

    private let bentoColumns = [
        GridItem(.flexible(), spacing: 12),
        GridItem(.flexible(), spacing: 12)
    ]

    public var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()

            VStack(spacing: 16) {
                HStack {
                    Text("explore")
                        .font(.system(size: 26, weight: .bold, design: .serif))
                        .foregroundColor(colors.textPrimary)
                    Spacer()
                }
                .padding(.horizontal, 22)
                .padding(.top, 10)

                HStack(spacing: 10) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(colors.textTertiary)

                    TextField("Search all 15,163 verses...", text: $viewModel.query)
                        .font(.system(size: 15))
                        .foregroundColor(colors.textPrimary)
                        .onChange(of: viewModel.query) { _ in
                            viewModel.performSearch()
                        }

                    if !viewModel.query.isEmpty {
                        Button(action: { viewModel.clearSearch() }) {
                            Image(systemName: "xmark.circle.fill")
                                .font(.system(size: 14))
                                .foregroundColor(colors.textTertiary)
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .liquidGlass(cornerRadius: 18)
                .padding(.horizontal, 22)

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        filterPill(title: "All Traditions", isSelected: viewModel.selectedTraditionFilter == nil, tone: TraditionTones.hue(for: nil)) {
                            viewModel.setTraditionFilter(nil)
                        }

                        ForEach(TraditionId.entries, id: \.self) { tradition in
                            let isSelected = viewModel.selectedTraditionFilter == tradition
                            let tone = TraditionTones.hue(for: tradition)
                            filterPill(title: tradition.displayName, isSelected: isSelected, tone: tone) {
                                viewModel.setTraditionFilter(isSelected ? nil : tradition)
                            }
                        }
                    }
                    .padding(.horizontal, 22)
                }

                if viewModel.isSearching {
                    Spacer()
                    ProgressView().tint(colors.textPrimary)
                    Spacer()
                } else if !viewModel.query.isEmpty && viewModel.searchResults.isEmpty {
                    Spacer()
                    VStack(spacing: 8) {
                        Text("No matching verses")
                            .font(.system(size: 18, weight: .semibold, design: .serif))
                            .foregroundColor(colors.textPrimary)
                        Text("Try different search keywords or remove filters.")
                            .font(.system(size: 14))
                            .foregroundColor(colors.textSecondary)
                    }
                    Spacer()
                } else if !viewModel.searchResults.isEmpty {
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: 14) {
                            if let topic = viewModel.selectedTopic {
                                HStack(spacing: 8) {
                                    Image(systemName: topic.icon)
                                        .foregroundColor(topic.accentTone)
                                    Text(topic.title.uppercased())
                                        .font(.system(size: 12, weight: .bold, design: .monospaced))
                                        .foregroundColor(topic.accentTone)
                                    Spacer()
                                    Text("\(viewModel.searchResults.count) passages found")
                                        .font(.system(size: 11, design: .monospaced))
                                        .foregroundColor(colors.textTertiary)
                                }
                                .padding(.horizontal, 4)
                                .padding(.top, 4)
                            }

                            ForEach(viewModel.searchResults, id: \.id) { passage in
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

                                    if let original = passage.originalText, !original.isEmpty {
                                        Text(original)
                                            .font(.system(size: 14, design: .serif))
                                            .foregroundColor(colors.textSecondary)
                                    }

                                    Text(passage.translation)
                                        .font(.system(size: 16, weight: .regular, design: .serif))
                                        .foregroundColor(colors.textPrimary)
                                        .lineSpacing(4)
                                }
                                .padding(16)
                                .liquidGlass(cornerRadius: 18)
                            }
                        }
                        .padding(.horizontal, 22)
                        .padding(.bottom, 120)
                    }
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 28) {
                            VStack(alignment: .leading, spacing: 12) {
                                Text("CONTEMPLATION THEMES")
                                    .font(.system(size: 11, weight: .bold, design: .monospaced))
                                    .foregroundColor(colors.textTertiary)
                                    .tracking(1.2)

                                LazyVGrid(columns: bentoColumns, spacing: 12) {
                                    ForEach(viewModel.topics) { topic in
                                        bentoCard(topic: topic)
                                    }
                                }
                            }

                            VStack(alignment: .leading, spacing: 14) {
                                Text("CANONICAL TEXTS")
                                    .font(.system(size: 11, weight: .bold, design: .monospaced))
                                    .foregroundColor(colors.textTertiary)
                                    .tracking(1.2)

                                ForEach(viewModel.allBooks, id: \.id) { book in
                                    HStack {
                                        VStack(alignment: .leading, spacing: 3) {
                                            Text(book.title)
                                                .font(.system(size: 16, weight: .medium))
                                                .foregroundColor(colors.textPrimary)
                                            Text("\(book.traditionId.displayName) · \(book.totalPassages) verses")
                                                .font(.system(size: 12))
                                                .foregroundColor(colors.textTertiary)
                                        }
                                        Spacer()
                                    }
                                    .padding(14)
                                    .liquidGlass(cornerRadius: 16)
                                }
                            }
                        }
                        .padding(.horizontal, 22)
                        .padding(.bottom, 120)
                    }
                }
            }
        }
    }

    private func bentoCard(topic: ContemplationTopic) -> some View {
        Button(action: {
            viewModel.selectTopic(topic)
        }) {
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Image(systemName: topic.icon)
                        .font(.system(size: 18))
                        .foregroundColor(topic.accentTone)
                    Spacer()
                    Image(systemName: "arrow.up.right")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(colors.textTertiary)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(topic.title)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(colors.textPrimary)

                    Text(topic.subtitle)
                        .font(.system(size: 11))
                        .foregroundColor(colors.textSecondary)
                        .lineLimit(1)
                }
            }
            .padding(14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .liquidGlass(cornerRadius: 18)
        }
    }

    @ViewBuilder
    private func filterPill(title: String, isSelected: Bool, tone: Color, action: @escaping () -> Void) -> some View {
        Button(action: {
            FathomHaptics.light()
            action()
        }) {
            Text(title)
                .font(.system(size: 12, weight: isSelected ? .semibold : .regular))
                .foregroundColor(isSelected ? colors.textPrimary : colors.textSecondary)
                .padding(.horizontal, 14)
                .padding(.vertical, 7)
                .background(isSelected ? tone.opacity(0.2) : colors.surfaceElevated, in: Capsule())
                .overlay(Capsule().strokeBorder(isSelected ? tone.opacity(0.6) : colors.hairline, lineWidth: 1))
        }
    }
}

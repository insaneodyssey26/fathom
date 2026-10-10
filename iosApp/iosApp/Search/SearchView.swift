import SwiftUI
import SharedLogic

@MainActor
public final class SearchViewModel: ObservableObject {
    private let graph: IosAppGraph

    @Published public var query: String = ""
    @Published public var searchResults: [Passage] = []
    @Published public var allBooks: [Book] = []
    @Published public var selectedTraditionFilter: TraditionId? = nil
    @Published public var isSearching: Bool = false

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
                limit: 50,
                offset: 0
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
}

public struct SearchView: View {
    @ObservedObject public var viewModel: SearchViewModel
    public let colors: ThemeColors

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
                        Button(action: { viewModel.query = ""; viewModel.performSearch() }) {
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
                        VStack(alignment: .leading, spacing: 24) {
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
                        .padding(.horizontal, 22)
                        .padding(.bottom, 120)
                    }
                }
            }
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

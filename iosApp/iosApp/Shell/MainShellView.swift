import SwiftUI
import SharedLogic

public enum FathomTab: Int, CaseIterable {
    case read = 0
    case paths = 1
    case explore = 2
    case journal = 3

    public var title: String {
        switch self {
        case .read: return "Read"
        case .paths: return "Paths"
        case .explore: return "Explore"
        case .journal: return "Journal"
        }
    }

    public var icon: String {
        switch self {
        case .read: return "book.pages"
        case .paths: return "point.topleft.down.to.point.bottomright.curvepath"
        case .explore: return "magnifyingglass"
        case .journal: return "heart.text.square"
        }
    }
}

public struct MainShellView: View {
    @StateObject private var feedViewModel: FeedViewModel
    @StateObject private var pathwaysViewModel: PathwaysViewModel
    @StateObject private var searchViewModel: SearchViewModel
    @StateObject private var journalViewModel: JournalViewModel

    @State private var selectedTab: FathomTab = .read
    @State private var showScriptureSheet: Bool = false
    @State private var showSettingsSheet: Bool = false
    @State private var currentTheme: ReadingThemeId = .nocturne

    public init(graph: IosAppGraph) {
        _feedViewModel = StateObject(wrappedValue: FeedViewModel(graph: graph))
        _pathwaysViewModel = StateObject(wrappedValue: PathwaysViewModel(graph: graph))
        _searchViewModel = StateObject(wrappedValue: SearchViewModel(graph: graph))
        _journalViewModel = StateObject(wrappedValue: JournalViewModel(graph: graph))
    }

    private var colors: ThemeColors {
        switch currentTheme {
        case .nocturne: return .nocturne
        case .vellum: return .vellum
        case .abyss: return .abyss
        case .moss: return .moss
        }
    }

    public var body: some View {
        ZStack(alignment: .bottom) {
            TabView(selection: $selectedTab) {
                FeedView(
                    viewModel: feedViewModel,
                    colors: colors,
                    onOpenScriptureSelector: { showScriptureSheet = true }
                )
                .tag(FathomTab.read)

                PathwaysView(
                    viewModel: pathwaysViewModel,
                    colors: colors,
                    onOpenScriptureSelector: { showScriptureSheet = true }
                )
                .tag(FathomTab.paths)

                SearchView(
                    viewModel: searchViewModel,
                    colors: colors
                )
                .tag(FathomTab.explore)

                JournalView(
                    viewModel: journalViewModel,
                    colors: colors,
                    onOpenSettings: { showSettingsSheet = true }
                )
                .tag(FathomTab.journal)
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            .ignoresSafeArea()

            floatingDock
                .padding(.horizontal, 24)
                .padding(.bottom, 12)
        }
        .sheet(isPresented: $showSettingsSheet) {
            SettingsView(currentTheme: $currentTheme, colors: colors)
                .presentationDetents([.fraction(0.75), .large])
                .presentationDragIndicator(.visible)
        }
        .sheet(isPresented: $showScriptureSheet) {
            ScriptureSelectorSheet(
                availableBooks: feedViewModel.availableBooks,
                selectedBookId: selectedTab == .paths ? pathwaysViewModel.selectedBook?.id : feedViewModel.selectedBookId,
                selectedTraditions: feedViewModel.selectedTraditions,
                alignedTraditions: feedViewModel.alignedTraditions,
                allowAllScriptures: selectedTab != .paths,
                showWholeTraditionOption: selectedTab != .paths,
                onSelectBook: { bookId in
                    if selectedTab == .paths {
                        if let bookId = bookId, let book = pathwaysViewModel.availableBooks.first(where: { $0.id == bookId }) {
                            pathwaysViewModel.selectBook(book)
                        }
                    } else {
                        feedViewModel.selectBook(bookId: bookId)
                    }
                },
                onSelectTradition: { tradition in
                    if selectedTab == .paths {
                        if let tradition = tradition, let book = pathwaysViewModel.availableBooks.first(where: { $0.traditionId == tradition }) {
                            pathwaysViewModel.selectBook(book)
                        }
                    } else {
                        feedViewModel.selectTradition(tradition: tradition)
                    }
                },
                onToggleAlignedTradition: { feedViewModel.toggleAlignedTradition(tradition: $0) },
                onClearAlignedTraditions: { feedViewModel.clearAlignedTraditions() },
                onDismiss: { showScriptureSheet = false }
            )
            .presentationDetents([.fraction(0.85), .large])
            .presentationDragIndicator(.visible)
        }
    }

    private var floatingDock: some View {
        HStack(spacing: 0) {
            ForEach(FathomTab.allCases, id: \.self) { tab in
                let isSelected = selectedTab == tab
                let tone = TraditionTones.hue(for: feedViewModel.currentPassage?.traditionId)

                Button(action: {
                    FathomHaptics.light()
                    withAnimation(.spring(response: 0.32, dampingFraction: 0.8)) {
                        selectedTab = tab
                    }
                }) {
                    VStack(spacing: 4) {
                        Image(systemName: tab.icon)
                            .font(.system(size: 18, weight: isSelected ? .semibold : .regular))
                            .foregroundColor(isSelected ? tone : colors.textTertiary)

                        Text(tab.title)
                            .font(.system(size: 10, weight: isSelected ? .semibold : .medium))
                            .foregroundColor(isSelected ? colors.textPrimary : colors.textTertiary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                }
            }
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 4)
        .liquidGlass(cornerRadius: 32)
    }
}

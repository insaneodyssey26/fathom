import SwiftUI
import SharedLogic

@MainActor
public final class FeedViewModel: ObservableObject {
    private let graph: IosAppGraph

    @Published public var passages: [Passage] = []
    @Published public var availableBooks: [Book] = []
    @Published public var selectedBookId: String? = nil
    @Published public var selectedTraditions: Set<TraditionId> = Set(TraditionId.entries)
    @Published public var alignedTraditions: Set<TraditionId> = []
    @Published public var bookmarkedIds: Set<String> = []
    @Published public var currentIndex: Int = 0
    @Published public var isLoading: Bool = false
    @Published public var errorMessage: String? = nil

    public init(graph: IosAppGraph) {
        self.graph = graph
        loadBooks()
        loadInitialFeed()
        loadBookmarks()
    }

    public var currentPassage: Passage? {
        guard currentIndex >= 0 && currentIndex < passages.count else { return nil }
        return passages[currentIndex]
    }

    public func loadBooks() {
        Task {
            let result = try? await graph.passageRepository.getAllBooks()
            if let result = result as? FathomResultSuccess<NSArray>, let books = result.data as? [Book] {
                self.availableBooks = books
            }
        }
    }

    public func loadInitialFeed() {
        isLoading = true
        errorMessage = nil
        Task {
            let config = FeedConfig(
                mode: .explorationDiscovery,
                selectedTraditions: selectedTraditions,
                alignedTraditions: alignedTraditions,
                selectedThemeIds: [],
                selectedBookId: selectedBookId,
                enableSeenFilter: true,
                preferredTranslations: [:]
            )

            let result = try? await graph.feedEngine.getFeedBatch(config: config, batchSize: 20)
            if let result = result as? FathomResultSuccess<NSArray>, let list = result.data as? [Passage] {
                self.passages = list
                self.currentIndex = 0
                self.isLoading = false
                if let first = list.first {
                    await graph.recordReading(passageId: first.id, bookId: first.bookId)
                }
            } else {
                self.isLoading = false
                self.errorMessage = "Unable to load sacred passages."
            }
        }
    }

    public func loadMoreIfNeeded(currentIndex: Int) {
        self.currentIndex = currentIndex
        if currentIndex >= 0 && currentIndex < passages.count {
            let p = passages[currentIndex]
            Task {
                await graph.recordReading(passageId: p.id, bookId: p.bookId)
            }
        }
        guard currentIndex >= passages.count - 4, !isLoading else { return }

        Task {
            let config = FeedConfig(
                mode: .explorationDiscovery,
                selectedTraditions: selectedTraditions,
                alignedTraditions: alignedTraditions,
                selectedThemeIds: [],
                selectedBookId: selectedBookId,
                enableSeenFilter: true,
                preferredTranslations: [:]
            )

            let result = try? await graph.feedEngine.getFeedBatch(config: config, batchSize: 20)
            if let result = result as? FathomResultSuccess<NSArray>, let list = result.data as? [Passage] {
                let newPassages = list.filter { newP in !self.passages.contains(where: { $0.id == newP.id }) }
                self.passages.append(contentsOf: newPassages)
            }
        }
    }

    public func toggleBookmark(passage: Passage) {
        FathomHaptics.medium()
        let id = passage.id
        Task {
            if bookmarkedIds.contains(id) {
                await graph.removeBookmark(passageId: id)
                bookmarkedIds.remove(id)
            } else {
                await graph.saveBookmark(passageId: id)
                bookmarkedIds.insert(id)
            }
        }
    }

    public func saveReflection(passage: Passage, content: String) {
        FathomHaptics.medium()
        Task {
            await graph.saveNote(passageId: passage.id, content: content)
        }
    }

    public func loadBookmarks() {
        Task {
            let list = await graph.getBookmarkedPassages()
            self.bookmarkedIds = Set(list.map { $0.id })
        }
    }

    public func selectBook(bookId: String?) {
        self.selectedBookId = bookId
        if bookId != nil {
            self.selectedTraditions = Set(TraditionId.entries)
            self.alignedTraditions = []
        }
        loadInitialFeed()
    }

    public func selectTradition(tradition: TraditionId?) {
        self.selectedBookId = nil
        if let tradition = tradition {
            self.selectedTraditions = [tradition]
            self.alignedTraditions = []
        } else {
            self.selectedTraditions = Set(TraditionId.entries)
        }
        loadInitialFeed()
    }

    public func toggleAlignedTradition(tradition: TraditionId) {
        if alignedTraditions.contains(tradition) {
            alignedTraditions.remove(tradition)
        } else {
            alignedTraditions.insert(tradition)
        }
        loadInitialFeed()
    }

    public func clearAlignedTraditions() {
        alignedTraditions.removeAll()
        loadInitialFeed()
    }
}

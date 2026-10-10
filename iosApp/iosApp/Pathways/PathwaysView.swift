import SwiftUI
import SharedLogic

@MainActor
public final class PathwaysViewModel: ObservableObject {
    private let graph: IosAppGraph

    @Published public var availableBooks: [Book] = []
    @Published public var selectedBook: Book? = nil
    @Published public var chapters: [Int32] = []
    @Published public var activeChapterPassages: [Passage] = []
    @Published public var activeChapterNumber: Int32? = nil
    @Published public var completedChapters: Set<Int32> = []
    @Published public var isLoading: Bool = false

    public init(graph: IosAppGraph) {
        self.graph = graph
        loadBooks()
    }

    public func loadBooks() {
        Task {
            let result = try? await graph.passageRepository.getAllBooks()
            if let result = result as? FathomResultSuccess<NSArray>, let books = result.data as? [Book] {
                self.availableBooks = books
                if self.selectedBook == nil, let first = books.first {
                    self.selectBook(first)
                }
            }
        }
    }

    public func selectBook(_ book: Book) {
        self.selectedBook = book
        self.activeChapterNumber = nil
        self.activeChapterPassages = []
        Task {
            let result = try? await graph.passageRepository.getChaptersForBook(bookId: book.id)
            if let result = result as? FathomResultSuccess<NSArray>, let list = result.data as? [Int32] {
                self.chapters = list
            }
        }
    }

    public func openChapter(_ chapter: Int32) {
        guard let book = selectedBook else { return }
        self.activeChapterNumber = chapter
        self.isLoading = true
        Task {
            let result = try? await graph.passageRepository.getPassagesForBookChapter(bookId: book.id, chapter: chapter)
            if let result = result as? FathomResultSuccess<NSArray>, let list = result.data as? [Passage] {
                self.activeChapterPassages = list
                self.isLoading = false
                self.completedChapters.insert(chapter)
                if let first = list.first {
                    await graph.recordReading(passageId: first.id, bookId: book.id)
                }
            }
        }
    }

    public func closeChapter() {
        self.activeChapterNumber = nil
        self.activeChapterPassages = []
    }
}

public struct DepthDialView: View {
    public let progress: Double
    public let completedCount: Int
    public let totalCount: Int
    public let tone: Color
    public let colors: ThemeColors

    public var body: some View {
        HStack(spacing: 20) {
            ZStack {
                Circle()
                    .stroke(colors.hairline, lineWidth: 6)
                    .frame(width: 84, height: 84)

                Circle()
                    .trim(from: 0, to: CGFloat(max(0.04, min(1.0, progress))))
                    .stroke(
                        AngularGradient(
                            gradient: Gradient(colors: [tone.opacity(0.6), tone]),
                            center: .center,
                            startAngle: .degrees(-90),
                            endAngle: .degrees(270)
                        ),
                        style: StrokeStyle(lineWidth: 6, lineCap: .round)
                    )
                    .rotationEffect(.degrees(-90))
                    .frame(width: 84, height: 84)
                    .animation(.spring(response: 0.5, dampingFraction: 0.7), value: progress)

                Circle()
                    .strokeBorder(tone.opacity(0.2), lineWidth: 1)
                    .frame(width: 66, height: 66)

                VStack(spacing: 0) {
                    Text("\(Int(progress * 100))%")
                        .font(.system(size: 14, weight: .bold, design: .monospaced))
                        .foregroundColor(colors.textPrimary)
                    Text("READ")
                        .font(.system(size: 8, weight: .bold, design: .monospaced))
                        .foregroundColor(colors.textTertiary)
                }
            }

            VStack(alignment: .leading, spacing: 4) {
                Text("CANONICAL PROGRESS")
                    .font(.system(size: 10, weight: .bold, design: .monospaced))
                    .foregroundColor(tone)
                    .tracking(1.2)

                Text("\(completedCount) of \(totalCount) Chapters")
                    .font(.system(size: 17, weight: .semibold, design: .serif))
                    .foregroundColor(colors.textPrimary)

                Text("Immerse sequentially through the sacred text.")
                    .font(.system(size: 12))
                    .foregroundColor(colors.textSecondary)
                    .lineLimit(2)
            }

            Spacer()
        }
        .padding(18)
        .liquidGlass(cornerRadius: 22)
    }
}

public struct PathwaysView: View {
    @ObservedObject public var viewModel: PathwaysViewModel
    public let colors: ThemeColors
    public let onOpenScriptureSelector: () -> Void

    public var body: some View {
        let tone = TraditionTones.hue(for: viewModel.selectedBook?.traditionId)
        let total = viewModel.chapters.count
        let completed = viewModel.completedChapters.count
        let progress = total > 0 ? Double(completed) / Double(total) : 0.0

        ZStack {
            AtmosphereBackground(
                traditionId: viewModel.selectedBook?.traditionId,
                colors: colors
            )

            if let chapter = viewModel.activeChapterNumber {
                ChapterStudyView(
                    chapterNumber: Int(chapter),
                    bookTitle: viewModel.selectedBook?.title ?? "Scripture",
                    passages: viewModel.activeChapterPassages,
                    isLoading: viewModel.isLoading,
                    colors: colors,
                    onBack: { viewModel.closeChapter() }
                )
            } else {
                VStack(spacing: 0) {
                    HStack {
                        Text("paths")
                            .font(.system(size: 26, weight: .bold, design: .serif))
                            .foregroundColor(colors.textPrimary)

                        Spacer()

                        Button(action: onOpenScriptureSelector) {
                            HStack(spacing: 6) {
                                Text(viewModel.selectedBook?.title ?? "Choose text")
                                    .font(.system(size: 13, weight: .semibold))
                                    .foregroundColor(colors.textPrimary)
                                    .lineLimit(1)

                                Image(systemName: "chevron.down")
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(colors.textSecondary)
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 9)
                            .liquidGlass(cornerRadius: 50)
                        }
                    }
                    .padding(.horizontal, 22)
                    .padding(.top, 10)
                    .padding(.bottom, 16)

                    ScrollView {
                        VStack(alignment: .leading, spacing: 24) {
                            if let book = viewModel.selectedBook {
                                VStack(alignment: .leading, spacing: 8) {
                                    Text(book.traditionId.displayName.uppercased())
                                        .font(.system(size: 11, weight: .bold, design: .monospaced))
                                        .foregroundColor(tone)
                                        .tracking(1.4)

                                    Text(book.title)
                                        .font(.system(size: 32, weight: .bold, design: .serif))
                                        .foregroundColor(colors.textPrimary)

                                    if let author = book.author, !author.isEmpty {
                                        Text(author)
                                            .font(.system(size: 16, weight: .regular, design: .serif))
                                            .foregroundColor(colors.textSecondary)
                                            .italic()
                                    }
                                }
                                .padding(.top, 12)

                                DepthDialView(
                                    progress: progress,
                                    completedCount: completed,
                                    totalCount: total,
                                    tone: tone,
                                    colors: colors
                                )
                            }

                            VStack(alignment: .leading, spacing: 14) {
                                Text("CHAPTERS")
                                    .font(.system(size: 11, weight: .bold, design: .monospaced))
                                    .foregroundColor(colors.textTertiary)
                                    .tracking(1.2)

                                ForEach(viewModel.chapters, id: \.self) { chapter in
                                    let isDone = viewModel.completedChapters.contains(chapter)

                                    Button(action: {
                                        FathomHaptics.light()
                                        viewModel.openChapter(chapter)
                                    }) {
                                        HStack(spacing: 16) {
                                            Circle()
                                                .strokeBorder(isDone ? tone : colors.hairline, lineWidth: 1.5)
                                                .background(isDone ? Circle().fill(tone.opacity(0.15)) : nil)
                                                .frame(width: 32, height: 32)
                                                .overlay(
                                                    Text("\(chapter)")
                                                        .font(.system(size: 12, weight: .bold, design: .monospaced))
                                                        .foregroundColor(isDone ? tone : colors.textPrimary)
                                                )

                                            VStack(alignment: .leading, spacing: 2) {
                                                Text("Chapter \(chapter)")
                                                    .font(.system(size: 16, weight: .medium))
                                                    .foregroundColor(colors.textPrimary)
                                            }

                                            Spacer()

                                            if isDone {
                                                Image(systemName: "checkmark.circle.fill")
                                                    .font(.system(size: 14))
                                                    .foregroundColor(tone)
                                            } else {
                                                Image(systemName: "chevron.right")
                                                    .font(.system(size: 12, weight: .semibold))
                                                    .foregroundColor(colors.textTertiary)
                                            }
                                        }
                                        .padding(.horizontal, 18)
                                        .padding(.vertical, 14)
                                        .liquidGlass(cornerRadius: 18)
                                    }
                                }
                            }
                            .padding(.bottom, 100)
                        }
                        .padding(.horizontal, 22)
                    }
                }
            }
        }
    }
}

public struct ChapterStudyView: View {
    public let chapterNumber: Int
    public let bookTitle: String
    public let passages: [Passage]
    public let isLoading: Bool
    public let colors: ThemeColors
    public let onBack: () -> Void

    public var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBack) {
                    HStack(spacing: 6) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 12, weight: .bold))
                        Text("Chapters")
                            .font(.system(size: 14, weight: .medium))
                    }
                    .foregroundColor(colors.textPrimary)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .liquidGlass(cornerRadius: 50)
                }

                Spacer()

                Text("Chapter \(chapterNumber)")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(colors.textPrimary)

                Spacer()
                Color.clear.frame(width: 80, height: 1)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 10)

            if isLoading {
                Spacer()
                ProgressView().tint(colors.textPrimary)
                Spacer()
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: 28) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(bookTitle)
                                .font(.system(size: 12, weight: .bold, design: .monospaced))
                                .foregroundColor(colors.textTertiary)
                            Text("Chapter \(chapterNumber)")
                                .font(.system(size: 28, weight: .bold, design: .serif))
                                .foregroundColor(colors.textPrimary)
                        }
                        .padding(.top, 12)

                        ForEach(passages, id: \.id) { passage in
                            VStack(alignment: .leading, spacing: 10) {
                                Text(passage.canonicalReference)
                                    .font(.system(size: 11, weight: .bold, design: .monospaced))
                                    .foregroundColor(TraditionTones.hue(for: passage.traditionId))

                                if let original = passage.originalText, !original.isEmpty {
                                    Text(original)
                                        .font(.system(size: 17, design: .serif))
                                        .foregroundColor(colors.textSecondary)
                                }

                                Text(passage.translation)
                                    .font(.system(size: 19, weight: .medium, design: .serif))
                                    .foregroundColor(colors.textPrimary)
                                    .lineSpacing(6)
                            }
                            .padding(.vertical, 8)
                            Divider().background(colors.hairline)
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 120)
                }
            }
        }
    }
}

import SwiftUI
import SharedLogic

public struct FeedView: View {
    @ObservedObject public var viewModel: FeedViewModel
    public let colors: ThemeColors
    public let onOpenScriptureSelector: () -> Void

    public var body: some View {
        ZStack {
            AtmosphereBackground(
                traditionId: viewModel.currentPassage?.traditionId,
                colors: colors
            )

            if viewModel.isLoading && viewModel.passages.isEmpty {
                VStack(spacing: 16) {
                    ProgressView()
                        .tint(TraditionTones.hue(for: nil))
                    Text("Opening the sanctuary...")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(colors.textSecondary)
                }
            } else if let error = viewModel.errorMessage, viewModel.passages.isEmpty {
                VStack(spacing: 16) {
                    Text("Something went quiet")
                        .font(.system(size: 20, weight: .semibold, design: .serif))
                        .foregroundColor(colors.textPrimary)
                    Text(error)
                        .font(.system(size: 14))
                        .foregroundColor(colors.textSecondary)
                    Button("Try again") {
                        viewModel.loadInitialFeed()
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 10)
                    .liquidGlass(cornerRadius: 50)
                }
                .padding()
            } else {
                TabView(selection: Binding(
                    get: { viewModel.currentIndex },
                    set: { newIndex in
                        FathomHaptics.light()
                        viewModel.loadMoreIfNeeded(currentIndex: newIndex)
                    }
                )) {
                    ForEach(Array(viewModel.passages.enumerated()), id: \.element.id) { index, passage in
                        PassageCardView(
                            passage: passage,
                            colors: colors,
                            isBookmarked: viewModel.bookmarkedIds.contains(passage.id),
                            onBookmark: { viewModel.toggleBookmark(passage: passage) },
                            onShare: { sharePassage(passage) },
                            onOpenAudio: {}
                        )
                        .tag(index)
                    }
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                .ignoresSafeArea()
            }

            VStack {
                HStack {
                    Text("read")
                        .font(.system(size: 26, weight: .bold, design: .serif))
                        .foregroundColor(colors.textPrimary)

                    Spacer()

                    Button(action: onOpenScriptureSelector) {
                        HStack(spacing: 6) {
                            Text(headerTitle)
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

                Spacer()
            }
        }
    }

    private var headerTitle: String {
        if let bookId = viewModel.selectedBookId, let book = viewModel.availableBooks.first(where: { $0.id == bookId }) {
            return book.title
        }
        if viewModel.selectedTraditions.count == 1, let single = viewModel.selectedTraditions.first {
            return single.displayName
        }
        if viewModel.alignedTraditions.count == 1, let aligned = viewModel.alignedTraditions.first {
            return "Leaning · \(aligned.displayName)"
        }
        if viewModel.alignedTraditions.count > 1 {
            return "Leaning · \(viewModel.alignedTraditions.count)"
        }
        return "All scriptures"
    }

    private func sharePassage(_ passage: Passage) {
        let text = "“\(passage.translation)”\n— \(passage.canonicalReference)\n\nShared from Fathom"
        let activityVC = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            rootVC.present(activityVC, animated: true, completion: nil)
        }
    }
}

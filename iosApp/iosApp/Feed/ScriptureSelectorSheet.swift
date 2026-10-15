import SwiftUI
import SharedLogic

public struct ScriptureSelectorSheet: View {
    public let availableBooks: [Book]
    public let selectedBookId: String?
    public let selectedTraditions: Set<TraditionId>
    public let alignedTraditions: Set<TraditionId>
    public let allowAllScriptures: Bool
    public let showWholeTraditionOption: Bool
    public let onSelectBook: (String?) -> Void
    public let onSelectTradition: (TraditionId?) -> Void
    public let onToggleAlignedTradition: (TraditionId) -> Void
    public let onClearAlignedTraditions: () -> Void
    public let onDismiss: () -> Void

    @State private var expandedTraditions: Set<String> = []

    public init(
        availableBooks: [Book],
        selectedBookId: String?,
        selectedTraditions: Set<TraditionId> = Set(TraditionId.entries),
        alignedTraditions: Set<TraditionId> = [],
        allowAllScriptures: Bool = true,
        showWholeTraditionOption: Bool = true,
        onSelectBook: @escaping (String?) -> Void,
        onSelectTradition: @escaping (TraditionId?) -> Void = { _ in },
        onToggleAlignedTradition: @escaping (TraditionId) -> Void = { _ in },
        onClearAlignedTraditions: @escaping () -> Void = {},
        onDismiss: @escaping () -> Void
    ) {
        self.availableBooks = availableBooks
        self.selectedBookId = selectedBookId
        self.selectedTraditions = selectedTraditions
        self.alignedTraditions = alignedTraditions
        self.allowAllScriptures = allowAllScriptures
        self.showWholeTraditionOption = showWholeTraditionOption
        self.onSelectBook = onSelectBook
        self.onSelectTradition = onSelectTradition
        self.onToggleAlignedTradition = onToggleAlignedTradition
        self.onClearAlignedTraditions = onClearAlignedTraditions
        self.onDismiss = onDismiss
    }

    private var isAllScriptures: Bool {
        selectedBookId == nil && (selectedTraditions.isEmpty || selectedTraditions.count == TraditionId.entries.count)
    }

    public var body: some View {
        let colors = ThemeColors.nocturne

        NavigationView {
            ZStack {
                colors.surface.ignoresSafeArea()

                ScrollView {
                    VStack(alignment: .leading, spacing: 0) {
                        if allowAllScriptures {
                            allScripturesSection(colors: colors)
                            Divider().background(colors.hairline).padding(.vertical, 8)
                        }

                        ForEach(TraditionId.entries, id: \.self) { tradition in
                            let books = availableBooks.filter { $0.traditionId == tradition }
                            if !books.isEmpty {
                                traditionAccordion(tradition: tradition, books: books, colors: colors)
                                Divider().background(colors.hairline).padding(.vertical, 4)
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 10)
                    .padding(.bottom, 40)
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    VStack(spacing: 2) {
                        Text("LIBRARY")
                            .font(.system(size: 10, weight: .bold, design: .monospaced))
                            .foregroundColor(colors.textTertiary)
                            .tracking(1.2)
                        Text("Choose a scripture")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(colors.textPrimary)
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: onDismiss) {
                        Image(systemName: "xmark")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(colors.textSecondary)
                            .padding(8)
                            .liquidGlass(cornerRadius: 50)
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func allScripturesSection(colors: ThemeColors) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Button(action: {
                FathomHaptics.light()
                onSelectTradition(nil)
                onSelectBook(nil)
                onDismiss()
            }) {
                HStack(spacing: 14) {
                    Circle()
                        .strokeBorder(isAllScriptures ? TraditionTones.hue(for: nil) : colors.textTertiary, lineWidth: 1.5)
                        .frame(width: 22, height: 22)
                        .overlay(
                            Circle()
                                .fill(isAllScriptures ? TraditionTones.hue(for: nil) : Color.clear)
                                .frame(width: 12, height: 12)
                        )

                    VStack(alignment: .leading, spacing: 2) {
                        Text("All scriptures")
                            .font(.system(size: 17, weight: .medium))
                            .foregroundColor(isAllScriptures ? colors.textPrimary : colors.textSecondary)

                        Text(allCaption)
                            .font(.system(size: 12))
                            .foregroundColor(colors.textTertiary)
                    }

                    Spacer()

                    if isAllScriptures {
                        Image(systemName: "checkmark")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(TraditionTones.hue(for: nil))
                    }
                }
                .padding(.vertical, 10)
            }

            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text("LEAN FEED TOWARDS (OPTIONAL)")
                        .font(.system(size: 10, weight: .bold, design: .monospaced))
                        .foregroundColor(colors.textTertiary)
                        .tracking(1.0)

                    Spacer()

                    if !alignedTraditions.isEmpty {
                        Button(action: onClearAlignedTraditions) {
                            Text("Reset")
                                .font(.system(size: 12, weight: .medium))
                                .foregroundColor(colors.textSecondary)
                        }
                    }
                }

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(TraditionId.entries, id: \.self) { tradition in
                            let isAligned = isAllScriptures && alignedTraditions.contains(tradition)
                            let tone = TraditionTones.hue(for: tradition)

                            Button(action: {
                                FathomHaptics.light()
                                onToggleAlignedTradition(tradition)
                            }) {
                                HStack(spacing: 6) {
                                    Text(tradition.displayName)
                                        .font(.system(size: 12, weight: isAligned ? .semibold : .regular))
                                        .foregroundColor(isAligned ? colors.textPrimary : colors.textSecondary)

                                    if isAligned {
                                        Image(systemName: "checkmark")
                                            .font(.system(size: 10, weight: .bold))
                                            .foregroundColor(tone)
                                    }
                                }
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(
                                    isAligned ? tone.opacity(0.18) : colors.surfaceElevated,
                                    in: Capsule()
                                )
                                .overlay(
                                    Capsule()
                                        .strokeBorder(isAligned ? tone.opacity(0.6) : colors.hairline, lineWidth: 1)
                                )
                            }
                        }
                    }
                }
            }
            .padding(.bottom, 6)
        }
    }

    @ViewBuilder
    private func traditionAccordion(tradition: TraditionId, books: [Book], colors: ThemeColors) -> some View {
        let isExpanded = expandedTraditions.contains(tradition.key)
        let tone = TraditionTones.hue(for: tradition)
        let wholeSelected = selectedBookId == nil && selectedTraditions.count == 1 && selectedTraditions.contains(tradition)
        let isActive = wholeSelected || books.contains(where: { $0.id == selectedBookId })

        VStack(spacing: 0) {
            Button(action: {
                FathomHaptics.light()
                withAnimation(.spring(response: 0.34, dampingFraction: 0.82)) {
                    if isExpanded {
                        expandedTraditions.remove(tradition.key)
                    } else {
                        expandedTraditions.insert(tradition.key)
                    }
                }
            }) {
                HStack(spacing: 14) {
                    Circle()
                        .fill(isActive ? tone.opacity(0.2) : Color.clear)
                        .frame(width: 26, height: 26)
                        .overlay(
                            Circle()
                                .strokeBorder(isActive ? tone : colors.textTertiary, lineWidth: 1.2)
                        )

                    VStack(alignment: .leading, spacing: 2) {
                        Text(tradition.displayName)
                            .font(.system(size: 17, weight: .medium))
                            .foregroundColor(isActive ? colors.textPrimary : colors.textSecondary)

                        Text("\(books.count) \(books.count == 1 ? "text" : "texts")")
                            .font(.system(size: 12))
                            .foregroundColor(colors.textTertiary)
                    }

                    Spacer()

                    Image(systemName: "chevron.down")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(colors.textTertiary)
                        .rotationEffect(.degrees(isExpanded ? 180 : 0))
                }
                .padding(.vertical, 12)
            }

            if isExpanded {
                VStack(spacing: 4) {
                    if showWholeTraditionOption {
                        Button(action: {
                            FathomHaptics.light()
                            onSelectTradition(tradition)
                            onDismiss()
                        }) {
                            HStack {
                                Text("Every text in \(tradition.displayName)")
                                    .font(.system(size: 15))
                                    .foregroundColor(wholeSelected ? tone : colors.textSecondary)
                                Spacer()
                                if wholeSelected {
                                    Image(systemName: "checkmark")
                                        .font(.system(size: 12, weight: .bold))
                                        .foregroundColor(tone)
                                }
                            }
                            .padding(.vertical, 8)
                            .padding(.leading, 40)
                        }
                    }

                    ForEach(books, id: \.id) { book in
                        let isBookSelected = book.id == selectedBookId
                        Button(action: {
                            FathomHaptics.light()
                            onSelectBook(book.id)
                            onDismiss()
                        }) {
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(book.title)
                                        .font(.system(size: 15))
                                        .foregroundColor(isBookSelected ? colors.textPrimary : colors.textSecondary)
                                    Text("\(book.language) · \(book.totalPassages) verses")
                                        .font(.system(size: 11))
                                        .foregroundColor(colors.textTertiary)
                                }
                                Spacer()
                                if isBookSelected {
                                    Image(systemName: "checkmark")
                                        .font(.system(size: 12, weight: .bold))
                                        .foregroundColor(tone)
                                }
                            }
                            .padding(.vertical, 8)
                            .padding(.leading, 40)
                        }
                    }
                }
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
    }

    private var allCaption: String {
        if !isAllScriptures {
            return "A blend across every tradition"
        }
        if alignedTraditions.isEmpty {
            return "Balanced blend across every tradition"
        }
        if alignedTraditions.count == 1, let first = alignedTraditions.first {
            return "Prioritizing \(first.displayName) with universal discovery"
        }
        return "Prioritizing \(alignedTraditions.count) traditions with universal discovery"
    }
}

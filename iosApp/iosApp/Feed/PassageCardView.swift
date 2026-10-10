import SwiftUI
import SharedLogic

public struct PassageCardView: View {
    public let passage: Passage
    public let colors: ThemeColors
    public let isBookmarked: Bool
    public let onBookmark: () -> Void
    public let onReflect: () -> Void
    public let onShare: () -> Void

    public var body: some View {
        let tone = TraditionTones.hue(for: passage.traditionId)

        GeometryReader { geometry in
            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 20) {
                    Spacer(minLength: 70)

                    HStack(spacing: 8) {
                        Text(passage.traditionId.displayName.uppercased())
                            .font(.system(size: 11, weight: .bold, design: .monospaced))
                            .foregroundColor(tone)
                            .tracking(1.5)

                        Text("·")
                            .foregroundColor(colors.textTertiary)

                        Text(passage.canonicalReference)
                            .font(.system(size: 12, weight: .medium, design: .monospaced))
                            .foregroundColor(colors.textSecondary)
                    }

                    if let original = passage.originalText, !original.isEmpty {
                        VStack(alignment: .leading, spacing: 6) {
                            Text(original)
                                .font(.system(size: 20, weight: .regular, design: .serif))
                                .foregroundColor(tone.opacity(0.85))
                                .lineSpacing(6)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        .padding(.vertical, 4)
                    }

                    Text(passage.translation)
                        .font(.system(size: 24, weight: .medium, design: .serif))
                        .foregroundColor(colors.textPrimary)
                        .lineSpacing(10)
                        .fixedSize(horizontal: false, vertical: true)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    Spacer(minLength: 40)

                    HStack(spacing: 12) {
                        Button(action: onBookmark) {
                            HStack(spacing: 6) {
                                Image(systemName: isBookmarked ? "bookmark.fill" : "bookmark")
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(isBookmarked ? tone : colors.textPrimary)
                                Text(isBookmarked ? "Saved" : "Save")
                                    .font(.system(size: 13, weight: .medium))
                                    .foregroundColor(colors.textPrimary)
                            }
                            .padding(.horizontal, 14)
                            .padding(.vertical, 9)
                            .liquidGlass(cornerRadius: 50)
                        }

                        Button(action: onReflect) {
                            HStack(spacing: 6) {
                                Image(systemName: "pencil.line")
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(colors.textPrimary)
                                Text("Reflect")
                                    .font(.system(size: 13, weight: .medium))
                                    .foregroundColor(colors.textPrimary)
                            }
                            .padding(.horizontal, 14)
                            .padding(.vertical, 9)
                            .liquidGlass(cornerRadius: 50)
                        }

                        Button(action: onShare) {
                            HStack(spacing: 6) {
                                Image(systemName: "square.and.arrow.up")
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(colors.textPrimary)
                                Text("Share")
                                    .font(.system(size: 13, weight: .medium))
                                    .foregroundColor(colors.textPrimary)
                            }
                            .padding(.horizontal, 14)
                            .padding(.vertical, 9)
                            .liquidGlass(cornerRadius: 50)
                        }

                        Spacer()
                    }
                    .padding(.bottom, 120)
                }
                .padding(.horizontal, 28)
                .frame(minHeight: geometry.size.height)
            }
        }
    }
}

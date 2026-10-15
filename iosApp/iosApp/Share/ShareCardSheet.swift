import SwiftUI
import SharedLogic

public struct ShareCardSheet: View {
    public let passage: Passage
    public let colors: ThemeColors
    public let accentColor: Color
    @Environment(\.dismiss) private var dismiss

    @State private var includeOriginalText: Bool = false
    @State private var fontSizeTier: CGFloat = 20

    public var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()

            VStack(spacing: 20) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("SACRED CARD STUDIO")
                            .font(.system(size: 11, weight: .bold, design: .monospaced))
                            .foregroundColor(colors.textTertiary)
                            .tracking(1.4)

                        Text("Share Timeless Wisdom")
                            .font(.system(size: 20, weight: .bold, design: .serif))
                            .foregroundColor(colors.textPrimary)
                    }

                    Spacer()

                    Button(action: { dismiss() }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 22))
                            .foregroundColor(colors.textTertiary)
                    }
                }
                .padding(.horizontal, 24)
                .padding(.top, 24)

                ScrollView {
                    VStack(spacing: 20) {
                        cardPreview
                            .padding(.horizontal, 24)

                        controlsSection
                            .padding(.horizontal, 24)
                    }
                    .padding(.bottom, 16)
                }

                VStack {
                    Button(action: shareViaActivitySheet) {
                        HStack(spacing: 10) {
                            Image(systemName: "square.and.arrow.up")
                                .font(.system(size: 15, weight: .semibold))

                            Text("Share Passage")
                                .font(.system(size: 16, weight: .semibold))
                        }
                        .foregroundColor(colors.isDark ? .black : .white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(
                            RoundedRectangle(cornerRadius: 14)
                                .fill(accentColor)
                        )
                    }
                    .buttonStyle(PlainButtonStyle())
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 20)
            }
        }
        .presentationDetents([.fraction(0.85), .large])
        .presentationDragIndicator(.visible)
    }

    private var cardPreview: some View {
        VStack(alignment: .leading, spacing: 18) {
            HStack {
                Circle()
                    .fill(accentColor)
                    .frame(width: 8, height: 8)

                Text(passage.traditionId.displayName)
                    .font(.system(size: 12, weight: .semibold, design: .monospaced))
                    .foregroundColor(accentColor)
                    .tracking(1.0)

                Spacer()

                Text("FATHOM")
                    .font(.system(size: 10, weight: .bold, design: .monospaced))
                    .foregroundColor(colors.textTertiary)
                    .tracking(2.0)
            }

            if includeOriginalText, let orig = passage.originalText, !orig.isEmpty {
                Text(orig)
                    .font(.system(size: fontSizeTier * 0.85, design: .serif))
                    .foregroundColor(colors.textSecondary)
                    .lineSpacing(4)
                    .padding(.bottom, 2)
            }

            Text("“\(passage.translation)”")
                .font(.system(size: fontSizeTier, weight: .medium, design: .serif))
                .foregroundColor(colors.textPrimary)
                .lineSpacing(6)

            HStack {
                Rectangle()
                    .fill(accentColor.opacity(0.6))
                    .frame(width: 20, height: 1.5)

                Text(passage.canonicalReference)
                    .font(.system(size: 13, weight: .semibold, design: .serif))
                    .foregroundColor(colors.textSecondary)

                Spacer()
            }
            .padding(.top, 4)
        }
        .padding(24)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(colors.surface)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(colors.hairline, lineWidth: 1)
        )
    }

    private var controlsSection: some View {
        VStack(spacing: 14) {
            if passage.originalText != nil && !passage.originalText!.isEmpty {
                Toggle(isOn: $includeOriginalText) {
                    Text("Include original sacred text")
                        .font(.system(size: 14, design: .serif))
                        .foregroundColor(colors.textPrimary)
                }
                .tint(accentColor)
                .padding(14)
                .background(
                    RoundedRectangle(cornerRadius: 14)
                        .fill(colors.surface)
                )
            }

            HStack {
                Text("Text Scale")
                    .font(.system(size: 13, design: .monospaced))
                    .foregroundColor(colors.textTertiary)

                Spacer()

                HStack(spacing: 12) {
                    Button(action: {
                        if fontSizeTier > 16 { fontSizeTier -= 2 }
                    }) {
                        Image(systemName: "textformat.size.smaller")
                            .font(.system(size: 14))
                            .foregroundColor(colors.textPrimary)
                            .padding(8)
                            .background(colors.surfaceElevated, in: Circle())
                    }

                    Button(action: {
                        if fontSizeTier < 28 { fontSizeTier += 2 }
                    }) {
                        Image(systemName: "textformat.size.larger")
                            .font(.system(size: 14))
                            .foregroundColor(colors.textPrimary)
                            .padding(8)
                            .background(colors.surfaceElevated, in: Circle())
                    }
                }
            }
            .padding(14)
            .background(
                RoundedRectangle(cornerRadius: 14)
                    .fill(colors.surface)
            )
        }
    }

    private func shareViaActivitySheet() {
        FathomHaptics.selection()
        var text = ""
        if includeOriginalText, let orig = passage.originalText, !orig.isEmpty {
            text += "\(orig)\n\n"
        }
        text += "“\(passage.translation)”\n— \(passage.canonicalReference)\n\nShared via Fathom"

        let activityVC = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            rootVC.present(activityVC, animated: true)
        }
    }
}

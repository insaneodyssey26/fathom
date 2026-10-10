import SwiftUI
import SharedLogic

public struct ReflectionSheet: View {
    public let passage: Passage
    public let colors: ThemeColors
    public let onSave: (String) -> Void
    @Environment(\.dismiss) private var dismiss

    @State private var noteContent: String = ""

    public var body: some View {
        let tone = TraditionTones.hue(for: passage.traditionId)

        ZStack {
            colors.background.ignoresSafeArea()

            VStack(spacing: 20) {
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("REFLECT")
                            .font(.system(size: 11, weight: .bold, design: .monospaced))
                            .foregroundColor(tone)
                            .tracking(1.2)
                        Text(passage.canonicalReference)
                            .font(.system(size: 16, weight: .semibold, design: .serif))
                            .foregroundColor(colors.textPrimary)
                    }

                    Spacer()

                    Button(action: {
                        if !noteContent.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                            onSave(noteContent)
                        }
                        dismiss()
                    }) {
                        Text("Save Note")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(colors.background)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(tone, in: Capsule())
                    }
                }
                .padding(.horizontal, 22)
                .padding(.top, 20)

                VStack(alignment: .leading, spacing: 8) {
                    Text(passage.translation)
                        .font(.system(size: 14, weight: .regular, design: .serif))
                        .foregroundColor(colors.textSecondary)
                        .lineSpacing(4)
                        .lineLimit(3)
                }
                .padding(14)
                .liquidGlass(cornerRadius: 16)
                .padding(.horizontal, 22)

                ZStack(alignment: .topLeading) {
                    if noteContent.isEmpty {
                        Text("Write your contemplation, meditation notes, or insights here...")
                            .font(.system(size: 15))
                            .foregroundColor(colors.textTertiary)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 14)
                    }

                    TextEditor(text: $noteContent)
                        .font(.system(size: 15))
                        .foregroundColor(colors.textPrimary)
                        .scrollContentBackground(.hidden)
                        .padding(10)
                }
                .frame(maxHeight: .infinity)
                .liquidGlass(cornerRadius: 18)
                .padding(.horizontal, 22)
                .padding(.bottom, 24)
            }
        }
    }
}

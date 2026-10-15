import SwiftUI
import SharedLogic

public struct OnboardingLineage: Identifiable {
    public let id: String
    public let title: String
    public let canon: String
    public let tradition: TraditionId?

    public init(id: String, title: String, canon: String, tradition: TraditionId?) {
        self.id = id
        self.title = title
        self.canon = canon
        self.tradition = tradition
    }
}

public struct OnboardingView: View {
    public let colors: ThemeColors
    public let onComplete: (_ name: String, _ tradition: TraditionId?, _ personalizeFeed: Bool) -> Void

    @State private var step: Int = 0
    @State private var name: String = ""
    @State private var selectedLineageId: String = "All Traditions"
    @State private var personalize: Bool = false

    private let lineages: [OnboardingLineage] = [
        OnboardingLineage(id: "All Traditions", title: "All traditions", canon: "Wander freely", tradition: nil),
        OnboardingLineage(id: "Sanatana Dharma", title: "Sanatana Dharma", canon: "Bhagavad Gita · Upanishads", tradition: .hinduism),
        OnboardingLineage(id: "Islam", title: "Islam", canon: "The Holy Quran", tradition: .islam),
        OnboardingLineage(id: "Christianity", title: "Christianity", canon: "The Holy Bible", tradition: .christianity),
        OnboardingLineage(id: "Buddhism", title: "Buddhism", canon: "Dhammapada · Zen", tradition: .buddhism),
        OnboardingLineage(id: "Taoism", title: "Taoism", canon: "Tao Te Ching", tradition: .taoism),
        OnboardingLineage(id: "Stoicism", title: "Stoicism", canon: "Meditations · Enchiridion", tradition: .stoicism)
    ]

    private let headlines = [
        "Welcome.\nWhat shall we\ncall you?",
        "Which path\nspeaks to\nyou?",
        "How should\ntoday's verse\nfind you?"
    ]

    private var currentLineage: OnboardingLineage {
        lineages.first(where: { $0.id == selectedLineageId }) ?? lineages[0]
    }

    private var toneHue: Color {
        TraditionTones.hue(for: currentLineage.tradition)
    }

    public init(
        colors: ThemeColors,
        onComplete: @escaping (_ name: String, _ tradition: TraditionId?, _ personalizeFeed: Bool) -> Void
    ) {
        self.colors = colors
        self.onComplete = onComplete
    }

    public var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()

            VStack(spacing: 0) {
                HStack {
                    if step > 0 {
                        Button(action: {
                            withAnimation(.easeInOut(duration: 0.25)) {
                                step -= 1
                            }
                        }) {
                            HStack(spacing: 6) {
                                Image(systemName: "chevron.left")
                                    .font(.system(size: 14, weight: .semibold))
                                Text("Back")
                                    .font(.system(size: 14, weight: .medium, design: .serif))
                            }
                            .foregroundColor(colors.textSecondary)
                        }
                    } else {
                        Text("fathom")
                            .font(.system(size: 13, weight: .bold, design: .monospaced))
                            .foregroundColor(colors.textTertiary)
                            .tracking(2.0)
                    }

                    Spacer()

                    Text("0\(step + 1) / 0\(headlines.count)")
                        .font(.system(size: 12, weight: .semibold, design: .monospaced))
                        .foregroundColor(colors.textTertiary)
                }
                .padding(.horizontal, 28)
                .padding(.top, 18)
                .padding(.bottom, 12)

                HStack(spacing: 6) {
                    ForEach(0..<headlines.count, id: \.self) { index in
                        GeometryReader { proxy in
                            ZStack(alignment: .leading) {
                                Capsule()
                                    .fill(colors.hairline)
                                    .frame(height: 2)

                                Capsule()
                                    .fill(toneHue)
                                    .frame(width: index <= step ? proxy.size.width : 0, height: 2)
                                    .animation(.easeInOut(duration: 0.35), value: step)
                            }
                        }
                        .frame(height: 2)
                    }
                }
                .padding(.horizontal, 28)
                .padding(.bottom, 24)

                ScrollView {
                    VStack(alignment: .leading, spacing: 28) {
                        Text(headlines[step])
                            .font(.system(size: 32, weight: .bold, design: .serif))
                            .foregroundColor(colors.textPrimary)
                            .lineSpacing(4)
                            .transition(.opacity)
                            .id("headline_\(step)")

                        stepContent
                            .transition(.opacity)
                            .id("step_\(step)")
                    }
                    .padding(.horizontal, 28)
                    .padding(.top, 8)
                    .padding(.bottom, 20)
                }

                VStack(spacing: 12) {
                    Button(action: advance) {
                        HStack(spacing: 10) {
                            Text(step < headlines.count - 1 ? "Continue" : "Begin reading")
                                .font(.system(size: 16, weight: .semibold))

                            Image(systemName: "arrow.right")
                                .font(.system(size: 14, weight: .semibold))
                        }
                        .foregroundColor(colors.isDark ? .black : .white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(
                            RoundedRectangle(cornerRadius: 14)
                                .fill(toneHue)
                        )
                    }
                    .buttonStyle(PlainButtonStyle())
                }
                .padding(.horizontal, 28)
                .padding(.bottom, 24)
            }
        }
    }

    @ViewBuilder
    private var stepContent: some View {
        switch step {
        case 0:
            nameInputStep
        case 1:
            traditionSelectStep
        default:
            feedPersonalizationStep
        }
    }

    private var nameInputStep: some View {
        VStack(alignment: .leading, spacing: 18) {
            TextField("", text: $name, prompt: Text("Your name").foregroundColor(colors.textTertiary.opacity(0.6)))
                .font(.system(size: 30, weight: .medium, design: .serif))
                .foregroundColor(colors.textPrimary)
                .padding(.bottom, 8)
                .overlay(
                    Rectangle()
                        .fill(toneHue.opacity(0.8))
                        .frame(height: 1.5)
                        .padding(.top, 38),
                    alignment: .bottom
                )

            HStack(spacing: 8) {
                Image(systemName: "lock.shield")
                    .font(.system(size: 13))
                    .foregroundColor(colors.textTertiary)

                Text("Only used to greet you. Nothing leaves your device.")
                    .font(.system(size: 13, design: .serif))
                    .foregroundColor(colors.textTertiary)
            }
        }
        .padding(.top, 12)
    }

    private var traditionSelectStep: some View {
        VStack(spacing: 4) {
            ForEach(lineages) { lineage in
                Button(action: {
                    FathomHaptics.light()
                    withAnimation(.easeInOut(duration: 0.2)) {
                        selectedLineageId = lineage.id
                        personalize = false
                    }
                }) {
                    HStack(spacing: 16) {
                        Circle()
                            .fill(selectedLineageId == lineage.id ? TraditionTones.hue(for: lineage.tradition) : colors.textTertiary.opacity(0.3))
                            .frame(width: 10, height: 10)

                        VStack(alignment: .leading, spacing: 3) {
                            Text(lineage.title)
                                .font(.system(size: 18, weight: selectedLineageId == lineage.id ? .semibold : .regular, design: .serif))
                                .foregroundColor(selectedLineageId == lineage.id ? colors.textPrimary : colors.textSecondary)

                            Text(lineage.canon)
                                .font(.system(size: 12, design: .monospaced))
                                .foregroundColor(colors.textTertiary)
                        }

                        Spacer()

                        if selectedLineageId == lineage.id {
                            Image(systemName: "checkmark")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(TraditionTones.hue(for: lineage.tradition))
                        }
                    }
                    .padding(.vertical, 14)
                    .contentShape(Rectangle())
                }
                .buttonStyle(PlainButtonStyle())

                if lineage.id != lineages.last?.id {
                    Divider()
                        .background(colors.hairline)
                }
            }
        }
    }

    private var feedPersonalizationStep: some View {
        VStack(alignment: .leading, spacing: 16) {
            choiceCard(
                title: "Every scripture",
                subtitle: "A balanced mix drawn freely from all seven ancient traditions.",
                selected: !personalize || currentLineage.tradition == nil,
                onSelect: {
                    FathomHaptics.light()
                    withAnimation { personalize = false }
                }
            )

            if currentLineage.tradition != nil {
                choiceCard(
                    title: "Mainly \(currentLineage.title)",
                    subtitle: "Most verses from \(currentLineage.title), with the occasional voice from elsewhere.",
                    selected: personalize,
                    onSelect: {
                        FathomHaptics.light()
                        withAnimation { personalize = true }
                    }
                )
            }

            HStack(alignment: .top, spacing: 10) {
                Image(systemName: "lock.shield.fill")
                    .font(.system(size: 15))
                    .foregroundColor(colors.textTertiary)

                Text("Everything stays on this phone. There is no server, so your name and preferences are never sent anywhere.")
                    .font(.system(size: 13, design: .serif))
                    .foregroundColor(colors.textTertiary)
                    .lineSpacing(3)
            }
            .padding(.top, 14)
        }
    }

    private func choiceCard(
        title: String,
        subtitle: String,
        selected: Bool,
        onSelect: @escaping () -> Void
    ) -> some View {
        Button(action: onSelect) {
            HStack(spacing: 16) {
                Circle()
                    .fill(selected ? toneHue : colors.hairline)
                    .frame(width: 12, height: 12)

                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.system(size: 17, weight: selected ? .semibold : .medium, design: .serif))
                        .foregroundColor(selected ? colors.textPrimary : colors.textSecondary)

                    Text(subtitle)
                        .font(.system(size: 13))
                        .foregroundColor(colors.textTertiary)
                        .lineSpacing(2)
                }

                Spacer()
            }
            .padding(16)
            .background(
                RoundedRectangle(cornerRadius: 14)
                    .fill(selected ? toneHue.opacity(0.08) : colors.surface)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(selected ? toneHue.opacity(0.4) : colors.hairline, lineWidth: 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }

    private func advance() {
        FathomHaptics.selection()
        if step < headlines.count - 1 {
            withAnimation(.easeInOut(duration: 0.3)) {
                step += 1
            }
        } else {
            let finalName = name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Seeker" : name.trimmingCharacters(in: .whitespacesAndNewlines)
            let finalTradition = currentLineage.tradition
            onComplete(finalName, finalTradition, personalize && finalTradition != nil)
        }
    }
}

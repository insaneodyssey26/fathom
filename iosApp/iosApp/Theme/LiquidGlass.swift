import SwiftUI
import SharedLogic

public struct LiquidGlassModifier: ViewModifier {
    public var cornerRadius: CGFloat
    public var isInteractive: Bool

    public func body(content: Content) -> some View {
        content
            .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .strokeBorder(
                        LinearGradient(
                            colors: [
                                Color.white.opacity(0.18),
                                Color.white.opacity(0.04)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: 0.65
                    )
            )
            .shadow(color: Color.black.opacity(0.12), radius: 10, x: 0, y: 4)
    }
}

public extension View {
    func liquidGlass(cornerRadius: CGFloat = 20, isInteractive: Bool = false) -> some View {
        self.modifier(LiquidGlassModifier(cornerRadius: cornerRadius, isInteractive: isInteractive))
    }
}

public struct AtmosphereBackground: View {
    public var traditionId: TraditionId?
    public var colors: ThemeColors

    public var body: some View {
        let hue = TraditionTones.hue(for: traditionId)
        ZStack {
            colors.background.ignoresSafeArea()

            RadialGradient(
                colors: [
                    hue.opacity(colors.isDark ? 0.22 : 0.12),
                    hue.opacity(0.04),
                    Color.clear
                ],
                center: .top,
                startRadius: 20,
                endRadius: 520
            )
            .ignoresSafeArea()

            RadialGradient(
                colors: [
                    hue.opacity(colors.isDark ? 0.14 : 0.06),
                    Color.clear
                ],
                center: .bottomTrailing,
                startRadius: 40,
                endRadius: 480
            )
            .ignoresSafeArea()
        }
        .animation(.easeInOut(duration: 0.6), value: traditionId?.name)
    }
}

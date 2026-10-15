import SwiftUI
import SharedLogic

public enum ReadingThemeId: String, CaseIterable, Identifiable {
    case nocturne = "NOCTURNE"
    case vellum = "VELLUM"
    case abyss = "ABYSS"
    case moss = "MOSS"

    public var id: String { rawValue }

    public var displayName: String {
        switch self {
        case .nocturne: return "Nocturne"
        case .vellum: return "Vellum"
        case .abyss: return "Abyss"
        case .moss: return "Moss"
        }
    }
}

public struct ThemeColors: Sendable {
    public let background: Color
    public let surface: Color
    public let surfaceElevated: Color
    public let hairline: Color
    public let textPrimary: Color
    public let textSecondary: Color
    public let textTertiary: Color
    public let isDark: Bool

    public static let nocturne = ThemeColors(
        background: Color(red: 0.00, green: 0.00, blue: 0.00),
        surface: Color(red: 0.05, green: 0.05, blue: 0.06),
        surfaceElevated: Color(red: 0.09, green: 0.09, blue: 0.10),
        hairline: Color.white.opacity(0.08),
        textPrimary: Color(red: 0.96, green: 0.96, blue: 0.96),
        textSecondary: Color(red: 0.65, green: 0.65, blue: 0.68),
        textTertiary: Color(red: 0.42, green: 0.42, blue: 0.45),
        isDark: true
    )

    public static let vellum = ThemeColors(
        background: Color(red: 0.96, green: 0.94, blue: 0.90),
        surface: Color(red: 0.92, green: 0.89, blue: 0.84),
        surfaceElevated: Color(red: 0.87, green: 0.84, blue: 0.77),
        hairline: Color.black.opacity(0.08),
        textPrimary: Color(red: 0.14, green: 0.13, blue: 0.12),
        textSecondary: Color(red: 0.38, green: 0.36, blue: 0.33),
        textTertiary: Color(red: 0.58, green: 0.55, blue: 0.50),
        isDark: false
    )

    public static let abyss = ThemeColors(
        background: Color(red: 0.02, green: 0.04, blue: 0.07),
        surface: Color(red: 0.04, green: 0.07, blue: 0.11),
        surfaceElevated: Color(red: 0.07, green: 0.11, blue: 0.17),
        hairline: Color(red: 0.20, green: 0.35, blue: 0.50).opacity(0.16),
        textPrimary: Color(red: 0.94, green: 0.96, blue: 0.98),
        textSecondary: Color(red: 0.60, green: 0.68, blue: 0.76),
        textTertiary: Color(red: 0.38, green: 0.45, blue: 0.52),
        isDark: true
    )

    public static let moss = ThemeColors(
        background: Color(red: 0.03, green: 0.05, blue: 0.04),
        surface: Color(red: 0.05, green: 0.09, blue: 0.06),
        surfaceElevated: Color(red: 0.09, green: 0.13, blue: 0.10),
        hairline: Color(red: 0.25, green: 0.45, blue: 0.30).opacity(0.16),
        textPrimary: Color(red: 0.94, green: 0.97, blue: 0.94),
        textSecondary: Color(red: 0.62, green: 0.72, blue: 0.64),
        textTertiary: Color(red: 0.40, green: 0.48, blue: 0.42),
        isDark: true
    )
}

public struct TraditionTones {
    public static func hue(for tradition: TraditionId?) -> Color {
        guard let tradition = tradition else {
            return Color(red: 0.88, green: 0.75, blue: 0.45)
        }
        switch tradition {
        case .hinduism:
            return Color(red: 0.96, green: 0.68, blue: 0.22)
        case .buddhism:
            return Color(red: 0.98, green: 0.52, blue: 0.18)
        case .stoicism:
            return Color(red: 0.65, green: 0.72, blue: 0.80)
        case .taoism:
            return Color(red: 0.18, green: 0.78, blue: 0.56)
        case .islam:
            return Color(red: 0.22, green: 0.78, blue: 0.42)
        case .christianity:
            return Color(red: 0.96, green: 0.32, blue: 0.42)
        case .judaism:
            return Color(red: 0.30, green: 0.72, blue: 0.96)
        default:
            return Color(red: 0.88, green: 0.75, blue: 0.45)
        }
    }
}

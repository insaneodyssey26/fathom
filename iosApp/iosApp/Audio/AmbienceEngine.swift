import SwiftUI
import AVFoundation

public enum AmbienceTrack: String, CaseIterable, Identifiable {
    case templeRain = "temple_rain"
    case distantStorm = "distant_storm"
    case sanctuaryNight = "sanctuary_night"
    case sacredRiver = "sacred_river"
    case singingBowl = "singing_bowl"
    case deepBrownNoise = "deep_brown_noise"

    public var id: String { rawValue }

    public var title: String {
        switch self {
        case .templeRain: return "Temple Rain"
        case .distantStorm: return "Distant Storm"
        case .sanctuaryNight: return "Sanctuary Night"
        case .sacredRiver: return "Sacred River"
        case .singingBowl: return "Singing Bowl"
        case .deepBrownNoise: return "Deep Brown Noise"
        }
    }

    public var icon: String {
        switch self {
        case .templeRain: return "cloud.rain"
        case .distantStorm: return "cloud.bolt"
        case .sanctuaryNight: return "moon.stars"
        case .sacredRiver: return "water.waves"
        case .singingBowl: return "bell"
        case .deepBrownNoise: return "waveform"
        }
    }
}

public final class AmbienceEngine: ObservableObject {
    public static let shared = AmbienceEngine()

    @Published public var currentTrack: AmbienceTrack? = nil
    @Published public var isPlaying: Bool = false
    @Published public var volume: Float = 0.65 {
        didSet {
            player?.volume = volume
        }
    }

    private var player: AVAudioPlayer?

    private init() {
        configureAudioSession()
    }

    private func configureAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: [.mixWithOthers])
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
        }
    }

    public func toggle(track: AmbienceTrack) {
        if currentTrack == track && isPlaying {
            pause()
        } else {
            play(track: track)
        }
    }

    public func play(track: AmbienceTrack) {
        if currentTrack == track && player != nil {
            player?.play()
            isPlaying = true
            return
        }

        guard let url = Bundle.main.url(forResource: track.rawValue, withExtension: "ogg") ??
                        Bundle.main.url(forResource: track.rawValue, withExtension: "mp3") else {
            currentTrack = track
            isPlaying = true
            return
        }

        do {
            player = try AVAudioPlayer(contentsOf: url)
            player?.numberOfLoops = -1
            player?.volume = volume
            player?.prepareToPlay()
            player?.play()
            currentTrack = track
            isPlaying = true
        } catch {
            currentTrack = track
            isPlaying = false
        }
    }

    public func pause() {
        player?.pause()
        isPlaying = false
    }

    public func stop() {
        player?.stop()
        player = nil
        currentTrack = nil
        isPlaying = false
    }
}

public struct AmbiencePlayerSheet: View {
    @ObservedObject var engine = AmbienceEngine.shared
    public let colors: ThemeColors
    public let accentColor: Color
    @Environment(\.dismiss) private var dismiss

    public var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()

            VStack(spacing: 24) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("SACRED AMBIENCE")
                            .font(.system(size: 11, weight: .bold, design: .monospaced))
                            .foregroundColor(colors.textTertiary)
                            .tracking(1.4)

                        Text("Soundscapes for Contemplation")
                            .font(.system(size: 18, weight: .semibold, design: .serif))
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

                VStack(spacing: 12) {
                    ForEach(AmbienceTrack.allCases) { track in
                        let isSelected = engine.currentTrack == track
                        let isThisPlaying = isSelected && engine.isPlaying

                        Button(action: {
                            FathomHaptics.light()
                            engine.toggle(track: track)
                        }) {
                            HStack(spacing: 16) {
                                Image(systemName: track.icon)
                                    .font(.system(size: 20, weight: .light))
                                    .foregroundColor(isThisPlaying ? accentColor : colors.textSecondary)
                                    .frame(width: 32)

                                Text(track.title)
                                    .font(.system(size: 16, weight: isSelected ? .semibold : .regular, design: .serif))
                                    .foregroundColor(isSelected ? colors.textPrimary : colors.textSecondary)

                                Spacer()

                                if isThisPlaying {
                                    Image(systemName: "speaker.wave.2.fill")
                                        .font(.system(size: 14))
                                        .foregroundColor(accentColor)
                                } else if isSelected {
                                    Image(systemName: "pause.fill")
                                        .font(.system(size: 14))
                                        .foregroundColor(colors.textTertiary)
                                }
                            }
                            .padding(16)
                            .background(
                                RoundedRectangle(cornerRadius: 14)
                                    .fill(isSelected ? accentColor.opacity(0.10) : colors.surface)
                            )
                            .overlay(
                                RoundedRectangle(cornerRadius: 14)
                                    .stroke(isSelected ? accentColor.opacity(0.35) : colors.hairline, lineWidth: 1)
                            )
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                }
                .padding(.horizontal, 24)

                if engine.currentTrack != nil {
                    VStack(spacing: 10) {
                        HStack {
                            Image(systemName: "speaker.fill")
                                .font(.system(size: 12))
                                .foregroundColor(colors.textTertiary)

                            Slider(value: Binding(
                                get: { Double(engine.volume) },
                                set: { engine.volume = Float($0) }
                            ), in: 0...1)
                            .accentColor(accentColor)

                            Image(systemName: "speaker.wave.3.fill")
                                .font(.system(size: 12))
                                .foregroundColor(colors.textTertiary)
                        }
                    }
                    .padding(.horizontal, 28)
                    .padding(.bottom, 16)
                }

                Spacer()
            }
        }
        .presentationDetents([.fraction(0.70), .large])
        .presentationDragIndicator(.visible)
    }
}

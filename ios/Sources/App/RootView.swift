import SwiftUI

struct RootView: View {
    @StateObject private var session = JarvisSession()
    @State private var typedCommand = ""
    @State private var showKey = false
    @State private var showingVersionHistory = false

    var body: some View {
        ZStack {
            LinearGradient(colors: [Color.black, Color(red: 0.02, green: 0.09, blue: 0.13), Color.black], startPoint: .topLeading, endPoint: .bottomTrailing)
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 22) {
                    topPanel
                    orb
                    dialogue
                    secondBrain
                    versionFooter
                }
                .padding(18)
            }
        }
        .preferredColorScheme(.dark)
        .sheet(isPresented: $showingVersionHistory) {
            VersionHistoryView()
        }
    }

    private var topPanel: some View {
        VStack(spacing: 12) {
            HStack {
                statusPill
                Spacer()
                Button(session.isActivated ? "Parar" : "Ativar") {
                    session.isActivated ? session.stop() : session.start()
                }
                .buttonStyle(.borderedProminent)
                .tint(session.isActivated ? .red : .cyan)
            }

            HStack(spacing: 10) {
                Text("API KEY")
                    .font(.system(.caption, design: .monospaced).weight(.bold))
                    .foregroundStyle(.cyan)
                Group {
                    if showKey {
                        TextField("Anthropic API key", text: $session.apiKey)
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                    } else {
                        SecureField("Anthropic API key", text: $session.apiKey)
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                    }
                }
                .font(.system(.body, design: .monospaced))
                Button(showKey ? "Ocultar" : "Mostrar") {
                    showKey.toggle()
                }
                .buttonStyle(.bordered)
            }
            .padding(12)
            .background(.cyan.opacity(0.08), in: RoundedRectangle(cornerRadius: 8))
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(.cyan.opacity(0.35)))

            HStack(spacing: 10) {
                Text("MODELO")
                    .font(.system(.caption, design: .monospaced).weight(.bold))
                    .foregroundStyle(.cyan)
                Picker("Modelo", selection: $session.selectedModel) {
                    ForEach(JarvisSession.availableModels) { model in
                        Text(model.label).tag(model.id)
                    }
                }
                .pickerStyle(.menu)
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(12)
            .background(.cyan.opacity(0.08), in: RoundedRectangle(cornerRadius: 8))
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(.cyan.opacity(0.35)))

            HStack(spacing: 10) {
                Text("VOZ")
                    .font(.system(.caption, design: .monospaced).weight(.bold))
                    .foregroundStyle(.cyan)
                Picker("Voz", selection: $session.selectedVoicePreference) {
                    ForEach(JarvisVoicePreference.allCases) { voice in
                        Text(voice.label).tag(voice)
                    }
                }
                .pickerStyle(.segmented)
            }
            .padding(12)
            .background(.cyan.opacity(0.08), in: RoundedRectangle(cornerRadius: 8))
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(.cyan.opacity(0.35)))
        }
    }

    private var statusPill: some View {
        HStack(spacing: 8) {
            Circle()
                .fill(statusColor)
                .frame(width: 9, height: 9)
                .shadow(color: statusColor, radius: 8)
            Text(session.state.rawValue)
                .font(.system(.caption, design: .monospaced).weight(.semibold))
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(.cyan.opacity(0.08), in: Capsule())
        .overlay(Capsule().stroke(.cyan.opacity(0.25)))
    }

    private var statusColor: Color {
        switch session.state {
        case .idle: return .gray
        case .listening: return .cyan
        case .thinking: return .orange
        case .speaking: return .green
        case .error: return .red
        }
    }

    private var orb: some View {
        ZStack {
            Circle()
                .stroke(.cyan.opacity(0.18), lineWidth: 2)
                .frame(width: 260, height: 260)
                .scaleEffect(session.state == .listening ? 1.08 : 1.0)
                .animation(.easeInOut(duration: 1.2).repeatForever(autoreverses: true), value: session.state == .listening)
            Circle()
                .fill(RadialGradient(colors: [.white, .cyan, .blue.opacity(0.25), .clear], center: .center, startRadius: 8, endRadius: 132))
                .frame(width: 230, height: 230)
                .shadow(color: .cyan.opacity(0.55), radius: 28)
            Circle()
                .fill(.white)
                .frame(width: 34, height: 34)
                .shadow(color: .white, radius: 12)
        }
        .padding(.vertical, 12)
        .onTapGesture {
            session.sendTypedCommand("status")
        }
    }

    private var dialogue: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("JARVIS")
                .font(.system(size: 48, weight: .black, design: .monospaced))
                .foregroundStyle(LinearGradient(colors: [.cyan, .blue], startPoint: .leading, endPoint: .trailing))

            line(title: "SENHOR", text: session.userLine)
            line(title: "JARVIS", text: session.assistantLine)

            HStack {
                TextField("ou digite e toque Enviar", text: $typedCommand)
                    .textFieldStyle(.plain)
                    .padding(12)
                    .background(.white.opacity(0.06), in: RoundedRectangle(cornerRadius: 8))
                Button("Enviar") {
                    let command = typedCommand
                    typedCommand = ""
                    session.sendTypedCommand(command)
                }
                .buttonStyle(.borderedProminent)
                .tint(.cyan)
            }
        }
        .padding(16)
        .background(.white.opacity(0.04), in: RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(.cyan.opacity(0.2)))
    }

    private func line(title: String, text: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.system(.caption, design: .monospaced).weight(.bold))
                .foregroundStyle(.secondary)
            Text(text)
                .font(.body)
                .foregroundStyle(.primary)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(12)
        .background(.white.opacity(0.055), in: RoundedRectangle(cornerRadius: 8))
    }

    private var secondBrain: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("SECOND BRAIN")
                        .font(.system(.headline, design: .monospaced).weight(.black))
                        .foregroundStyle(.cyan)
                    Text("Contexto injetado em todos os comandos.")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                Spacer()
                Text("\(session.notes.count) notas")
                    .font(.system(.caption, design: .monospaced))
                    .foregroundStyle(.secondary)
            }

            BrainGraphView(notes: session.notes)
                .frame(height: 320)

            LazyVGrid(columns: [GridItem(.adaptive(minimum: 145), spacing: 10)], spacing: 10) {
                ForEach(session.notes) { note in
                    VStack(alignment: .leading, spacing: 5) {
                        Text(note.title)
                            .font(.subheadline.weight(.bold))
                        Text(SecondBrain.areas[note.area]?.label ?? note.area)
                            .font(.caption2)
                            .foregroundStyle(SecondBrain.areas[note.area]?.color ?? .gray)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(10)
                    .background(.white.opacity(0.045), in: RoundedRectangle(cornerRadius: 8))
                }
            }
        }
        .padding(16)
        .background(.white.opacity(0.035), in: RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(.cyan.opacity(0.18)))
    }

    private var versionFooter: some View {
        VStack(spacing: 6) {
            Button {
                showingVersionHistory = true
            } label: {
                Text("v\(VersionManager.shared.currentVersionString) — \(VersionManager.shared.currentBuildDateString)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            if let commitURL = VersionManager.shared.commitURL {
                Link(destination: commitURL) {
                    HStack(spacing: 4) {
                        Image(systemName: "arrow.up.right.square")
                        Text("commit \(VersionManager.shared.currentCommit)")
                    }
                    .font(.caption2)
                    .foregroundStyle(.cyan)
                }
            } else {
                Text("commit \(VersionManager.shared.currentCommit)")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.bottom, 8)
    }
}

struct BrainGraphView: View {
    let notes: [BrainNote]

    var body: some View {
        GeometryReader { proxy in
            let size = proxy.size
            let center = CGPoint(x: size.width / 2, y: size.height / 2)
            let radiusX = max(90, size.width * 0.38)
            let radiusY = max(80, size.height * 0.32)
            let points = positionedNotes(center: center, radiusX: radiusX, radiusY: radiusY)

            ZStack {
                ForEach(Array(SecondBrain.relations.enumerated()), id: \.offset) { _, rel in
                    if let a = points[rel.0], let b = points[rel.1] {
                        edgePath(from: a.point, to: b.point, center: center)
                        .stroke(.cyan.opacity(0.22), lineWidth: 1)
                    }
                }

                Circle()
                    .fill(.purple)
                    .frame(width: 66, height: 66)
                    .shadow(color: .purple, radius: 18)
                    .position(center)
                Text("🧠")
                    .font(.title)
                    .position(center)

                ForEach(notes) { note in
                    if let placed = points[note.id] {
                        let color = SecondBrain.areas[note.area]?.color ?? .gray
                        VStack(spacing: 5) {
                            Circle()
                                .fill(color)
                                .frame(width: 30, height: 30)
                                .shadow(color: color, radius: 10)
                            Text(note.title)
                                .font(.caption2.weight(.semibold))
                                .lineLimit(1)
                                .foregroundStyle(.white)
                        }
                        .position(placed.point)
                    }
                }
            }
        }
    }

    private func positionedNotes(center: CGPoint, radiusX: CGFloat, radiusY: CGFloat) -> [String: (note: BrainNote, point: CGPoint)] {
        var result: [String: (BrainNote, CGPoint)] = [:]
        for (index, note) in notes.enumerated() {
            let angle = -.pi / 2 + (2 * .pi * CGFloat(index) / CGFloat(max(notes.count, 1)))
            result[note.id] = (note, CGPoint(x: center.x + cos(angle) * radiusX, y: center.y + sin(angle) * radiusY))
        }
        return result
    }

    private func edgePath(from start: CGPoint, to end: CGPoint, center: CGPoint) -> Path {
        let mid = CGPoint(x: (start.x + end.x) / 2, y: (start.y + end.y) / 2)
        let control = CGPoint(
            x: mid.x + (center.x - mid.x) * 0.35,
            y: mid.y + (center.y - mid.y) * 0.35
        )
        var path = Path()
        path.move(to: start)
        path.addQuadCurve(to: end, control: control)
        return path
    }
}

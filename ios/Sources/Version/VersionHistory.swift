import Foundation

struct VersionEntry: Identifiable, Hashable {
    let id = UUID()
    let version: String
    let build: String
    let date: String
    let changes: [String]
    let isCurrent: Bool
}

enum VersionHistory {
    static let currentVersionFallback = "1.1.1"
    static let currentBuildFallback = "5"
    static let currentCommitFallback = "dev"

    static let entries: [VersionEntry] = [
        VersionEntry(
            version: "1.1.1",
            build: "5",
            date: "12/07/2026",
            changes: [
                "Adicionado ajuste de voz com opções Masculina, Automática e Feminina.",
                "Voz padrão alterada para Masculina, com fallback seguro para português quando indisponível."
            ],
            isCurrent: true
        ),
        VersionEntry(
            version: "1.1.0",
            build: "4",
            date: "11/07/2026",
            changes: [
                "Corrigido versionamento semver para releases distribuídos.",
                "Adicionada validação de release para impedir TestFlight com versão e changelog desalinhados.",
                "Mantida rastreabilidade por build e commit git."
            ],
            isCurrent: false
        ),
        VersionEntry(
            version: "1.0.0",
            build: "3",
            date: "11/07/2026",
            changes: [
                "Projeto colocado sob versionamento git privado.",
                "Aplicado padrão app-versioning com rastreabilidade por commit antes do build distribuído."
            ],
            isCurrent: false
        ),
        VersionEntry(
            version: "1.0.0",
            build: "2",
            date: "11/07/2026",
            changes: [
                "Adicionado seletor de modelo Claude no app iOS.",
                "Modelo escolhido passa a ser persistido localmente.",
                "Cliente Anthropic passa a usar o modelo selecionado."
            ],
            isCurrent: false
        ),
        VersionEntry(
            version: "1.0.0",
            build: "1",
            date: "11/07/2026",
            changes: [
                "Primeira versão nativa iOS do Jarvis.",
                "API key Anthropic salva no Keychain.",
                "Reconhecimento de fala, síntese de voz e Second Brain embutido.",
                "Fluxo de ativação por wake word e janela de continuidade de 2 segundos."
            ],
            isCurrent: false
        )
    ]
}

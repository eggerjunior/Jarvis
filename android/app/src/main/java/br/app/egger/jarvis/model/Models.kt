package br.app.egger.jarvis.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class AIProvider(
    val id: String,
    val label: String,
    val keychainKey: String,
    val modelDefaultsKey: String,
    val defaultModel: String,
    val apiKeyPlaceholder: String
) {
    ANTHROPIC(
        id = "anthropic",
        label = "Anthropic",
        keychainKey = "anthropic_key",
        modelDefaultsKey = "anthropic_model",
        defaultModel = "claude-sonnet-5",
        apiKeyPlaceholder = "Anthropic API key"
    ),
    OPEN_ROUTER(
        id = "openRouter",
        label = "OpenRouter",
        keychainKey = "openrouter_key",
        modelDefaultsKey = "openrouter_model",
        defaultModel = "openrouter/auto",
        apiKeyPlaceholder = "OpenRouter API key"
    ),
    OMNI_ROUTE(
        id = "omniRoute",
        label = "OmniRoute",
        keychainKey = "omniroute_key",
        modelDefaultsKey = "omniroute_model",
        defaultModel = "openai/gpt-4o-mini",
        apiKeyPlaceholder = "OmniRoute API key"
    );

    companion object {
        fun fromId(id: String): AIProvider {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ANTHROPIC
        }
    }
}

data class AIModel(
    val id: String,
    val provider: AIProvider,
    val label: String,
    val price: String,
    val note: String
)

enum class JarvisVoicePreference(val id: String, val label: String) {
    MASCULINE("masculine", "Masculina"),
    AUTOMATIC("automatic", "Automática"),
    FEMININE("feminine", "Feminina");

    companion object {
        fun fromId(id: String): JarvisVoicePreference {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MASCULINE
        }
    }
}

data class JarvisVoiceOption(
    val id: String,
    val name: String,
    val language: String,
    val gender: String,
    val quality: String
) {
    val label: String
        get() = "$name · $language · $gender · $quality"
}

data class BrainArea(
    val id: String,
    val label: String,
    val color: Color
)

data class BrainNote(
    val id: String,
    var area: String,
    var title: String,
    var body: String
)

object SecondBrain {
    val areas: Map<String, BrainArea> = mapOf(
        "metas" to BrainArea("metas", "Metas", Color(0xFFFFD600)),
        "trabalho" to BrainArea("trabalho", "Carreira", Color(0xFFFF5252)),
        "projetos" to BrainArea("projetos", "Projetos", Color(0xE040FB00)),
        "financas" to BrainArea("financas", "Finanças", Color(0xFFFF9100)),
        "aprendizado" to BrainArea("aprendizado", "Aprendizado", Color(0xFF00E5FF)),
        "saude" to BrainArea("saude", "Saúde", Color(0xFF00E676)),
        "relacoes" to BrainArea("relacoes", "Relações", Color(0xFFFF4081)),
        "meta" to BrainArea("meta", "Você", Color(0xFF808080))
    )

    val initialNotes: List<BrainNote> = listOf(
        BrainNote("voce-ildemar", "meta", "Ildemar", "Ildemar, 50 anos, nascido em 20/02/1976, advogado, entusiasta de Tecnologia e Inteligência Artificial, mora em Brasília/DF, Brasil."),
        BrainNote("metas-ia", "metas", "IA", "Meta de curto prazo: especializar-se em Inteligência Artificial."),
        BrainNote("metas-renda", "metas", "Renda", "Meta de longo prazo: aposentar-se e viver de renda."),
        BrainNote("trabalho-caixa", "trabalho", "CAIXA", "Empregado da CAIXA Econômica Federal, no cargo de Advogado, atuando com questões de Tecnologia e Jurídico."),
        BrainNote("trabalho-juridico-tech", "trabalho", "Jurídico Tech", "Foco atual em implementar soluções tecnológicas para aprimorar a atuação jurídica da CAIXA."),
        BrainNote("projetos-pessoal-tech", "projetos", "Pessoal Tech", "Aprender e implementar soluções tecnológicas pessoais."),
        BrainNote("projetos-juridico-tech", "projetos", "Jurídico Tech", "Implementar soluções tecnológicas voltadas à área jurídica."),
        BrainNote("financas-liberdade", "financas", "Liberdade", "Objetivo financeiro principal: alcançar liberdade financeira."),
        BrainNote("aprendizado-ia-aplicada", "aprendizado", "IA Aplicada", "Estuda e quer aprender Tecnologia e Inteligência Artificial aplicadas a finanças e direito."),
        BrainNote("saude-saude", "saude", "Saúde", "Não treina atualmente, toma remédio para dormir e está em boas condições de saúde."),
        BrainNote("relacoes-camila", "relacoes", "Camila", "Casado com Camila desde 2000."),
        BrainNote("relacoes-matheus", "relacoes", "Matheus", "Pai de Matheus, nascido em 20/02/1997, engenheiro eletricista, casado com Amanda."),
        BrainNote("relacoes-pedro", "relacoes", "Pedro", "Pai de Pedro, nascido em 06/11/2002, médico, fazendo residência em cirurgia geral na USP Ribeirão Preto, namora Maria Fernanda."),
        BrainNote("relacoes-netos", "relacoes", "Netos", "Matheus e Amanda são pais de Miguel e Sofia.")
    )

    val relations: List<Pair<String, String>> = listOf(
        Pair("voce-ildemar", "metas-ia"), Pair("voce-ildemar", "trabalho-caixa"), Pair("voce-ildemar", "relacoes-camila"),
        Pair("metas-ia", "trabalho-juridico-tech"), Pair("metas-ia", "projetos-pessoal-tech"), Pair("metas-ia", "projetos-juridico-tech"), Pair("metas-ia", "aprendizado-ia-aplicada"),
        Pair("metas-renda", "financas-liberdade"), Pair("metas-renda", "saude-saude"), Pair("metas-renda", "relacoes-netos"),
        Pair("trabalho-caixa", "trabalho-juridico-tech"), Pair("trabalho-juridico-tech", "projetos-juridico-tech"), Pair("trabalho-juridico-tech", "aprendizado-ia-aplicada"),
        Pair("projetos-pessoal-tech", "aprendizado-ia-aplicada"), Pair("projetos-juridico-tech", "aprendizado-ia-aplicada"),
        Pair("financas-liberdade", "aprendizado-ia-aplicada"), Pair("relacoes-camila", "relacoes-matheus"), Pair("relacoes-camila", "relacoes-pedro"),
        Pair("relacoes-matheus", "relacoes-netos"), Pair("relacoes-matheus", "relacoes-pedro"), Pair("relacoes-pedro", "relacoes-netos")
    )
}

data class VersionEntry(
    val id: String = UUID.randomUUID().toString(),
    val version: String,
    val build: String,
    val date: String,
    val changes: List<String>,
    val isCurrent: Boolean
)

object VersionHistory {
    const val CURRENT_VERSION_FALLBACK = "1.6.1"
    const val CURRENT_BUILD_FALLBACK = "17"
    const val CURRENT_COMMIT_FALLBACK = "dev"

    val entries: List<VersionEntry> = listOf(
        VersionEntry(
            version = "1.6.1",
            build = "17",
            date = "17/07/2026",
            changes = listOf(
                "Versão Android Nativa criada em Jetpack Compose com paridade 1:1 ao app iOS.",
                "Suporte completo a reconhecimento de fala em português, síntese de voz configurável e Second Brain com gráfico Canvas.",
                "Integração aos provedores Anthropic e OpenRouter com ferramenta de busca web."
            ),
            isCurrent = true
        ),
        VersionEntry(
            version = "1.6.0",
            build = "16",
            date = "17/07/2026",
            changes = listOf(
                "CarPlay agora é um app funcional de verdade: a sessão do Jarvis passou a ser compartilhada entre iPhone e CarPlay.",
                "Tocar em “Ativar” no CarPlay leva a uma tela de controle por voz nativa (CPVoiceControlTemplate) que mostra ao vivo o estado do Jarvis.",
                "Aviso quando microfone/reconhecimento de fala ainda não foram autorizados."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.5.0",
            build = "15",
            date = "17/07/2026",
            changes = listOf(
                "Adicionado suporte a CarPlay (entitlement Voice Based Conversation aprovado pela Apple).",
                "Nova cena de CarPlay com ativação do Jarvis por toque na tela do carro.",
                "Modo de fundo de áudio habilitado para uso contínuo conectado ao CarPlay."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.4.4",
            build = "14",
            date = "13/07/2026",
            changes = listOf(
                "Jarvis agora tenta ativar automaticamente ao abrir o app.",
                "Jarvis tenta reativar ao voltar para primeiro plano.",
                "Adicionado estado Ativando para evitar múltiplas ativações simultâneas.",
                "Preparada a sessão de áudio antes da fala inicial para evitar silêncio no primeiro acionamento."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.4.3",
            build = "13",
            date = "13/07/2026",
            changes = listOf(
                "Adicionado seletor de vozes instaladas do iOS nas Configurações.",
                "Adicionado botão para testar imediatamente a voz selecionada.",
                "A voz escolhida pelo usuário agora tem prioridade sobre os fallbacks Masculina, Automática e Feminina."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.4.2",
            build = "12",
            date = "13/07/2026",
            changes = listOf(
                "Aumentada a tolerância a pausas naturais durante comandos de voz para evitar envio de frases incompletas.",
                "Adicionado tempo extra quando a transcrição termina em palavra que sugere continuação.",
                "Adicionada busca web via OpenRouter para perguntas sobre atualidades, notícias, preços, versões e fatos recentes."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.4.1",
            build = "11",
            date = "13/07/2026",
            changes = listOf(
                "Aumentada a janela de continuidade após a fala do Jarvis para 5 segundos.",
                "Removidos textos que ainda mencionavam 2 segundos no fluxo de ativação.",
                "Reforçada a seleção de voz masculina com identificadores conhecidos e prosódia mais grave."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.4.0",
            build = "10",
            date = "13/07/2026",
            changes = listOf(
                "Adicionado suporte ao OpenRouter como provedor alternativo de IA, com chave e modelos próprios.",
                "Removida a consulta de gastos do teste de modelo para manter o teste simples e confiável.",
                "Corrigida a janela de continuidade para aceitar fala iniciada após a resposta do Jarvis.",
                "Melhorada a seleção de voz masculina/feminina.",
                "Adicionada edição das memórias do Second Brain tocando nos balões ou nos cards."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.3.0",
            build = "9",
            date = "12/07/2026",
            changes = listOf(
                "Adicionado Widget Extension do Jarvis com widget de status rápido.",
                "Preparada base de Live Activity e Dynamic Island para sessões do assistente."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.2.2",
            build = "8",
            date = "12/07/2026",
            changes = listOf(
                "Adicionado gasto da organização ao resultado do Testar modelo."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.2.1",
            build = "7",
            date = "12/07/2026",
            changes = listOf(
                "Movidos API KEY, MODELO, TESTE e VOZ para uma tela de Configurações separada.",
                "Tela inicial simplificada, mostrando apenas status e o botão Ativar."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.2.0",
            build = "6",
            date = "12/07/2026",
            changes = listOf(
                "Adicionado botão Testar modelo para confirmar o modelo efetivamente usado pela API Anthropic.",
                "Exibido o modelo solicitado, o modelo retornado pela API, consumo de tokens e retorno do teste."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.1.1",
            build = "5",
            date = "12/07/2026",
            changes = listOf(
                "Adicionado ajuste de voz com opções Masculina, Automática e Feminina.",
                "Voz padrão alterada para Masculina, com fallback seguro para português quando indisponível."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.1.0",
            build = "4",
            date = "11/07/2026",
            changes = listOf(
                "Corrigido versionamento semver para releases distribuídos."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.0.0",
            build = "3",
            date = "11/07/2026",
            changes = listOf(
                "Projeto colocado sob versionamento git privado."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.0.0",
            build = "2",
            date = "11/07/2026",
            changes = listOf(
                "Adicionado seletor de modelo Claude no app.",
                "Modelo escolhido passa a ser persistido localmente."
            ),
            isCurrent = false
        ),
        VersionEntry(
            version = "1.0.0",
            build = "1",
            date = "11/07/2026",
            changes = listOf(
                "Primeira versão nativa do Jarvis.",
                "API key salva em armazenamento seguro.",
                "Reconhecimento de fala, síntese de voz e Second Brain embutido."
            ),
            isCurrent = false
        )
    )
}

import Foundation
import SwiftUI

struct BrainArea: Identifiable, Hashable {
    let id: String
    let label: String
    let color: Color
}

struct BrainNote: Identifiable, Codable, Hashable {
    var id: String
    var area: String
    var title: String
    var body: String
}

enum SecondBrain {
    static let areas: [String: BrainArea] = [
        "metas": .init(id: "metas", label: "Metas", color: .yellow),
        "trabalho": .init(id: "trabalho", label: "Carreira", color: .red),
        "projetos": .init(id: "projetos", label: "Projetos", color: .purple),
        "financas": .init(id: "financas", label: "Finanças", color: .orange),
        "aprendizado": .init(id: "aprendizado", label: "Aprendizado", color: .cyan),
        "saude": .init(id: "saude", label: "Saúde", color: .green),
        "relacoes": .init(id: "relacoes", label: "Relações", color: .pink),
        "meta": .init(id: "meta", label: "Você", color: .gray)
    ]

    static let initialNotes: [BrainNote] = [
        .init(id: "voce-ildemar", area: "meta", title: "Ildemar", body: "Ildemar, 50 anos, nascido em 20/02/1976, advogado, entusiasta de Tecnologia e Inteligência Artificial, mora em Brasília/DF, Brasil."),
        .init(id: "metas-ia", area: "metas", title: "IA", body: "Meta de curto prazo: especializar-se em Inteligência Artificial."),
        .init(id: "metas-renda", area: "metas", title: "Renda", body: "Meta de longo prazo: aposentar-se e viver de renda."),
        .init(id: "trabalho-caixa", area: "trabalho", title: "CAIXA", body: "Empregado da CAIXA Econômica Federal, no cargo de Advogado, atuando com questões de Tecnologia e Jurídico."),
        .init(id: "trabalho-juridico-tech", area: "trabalho", title: "Jurídico Tech", body: "Foco atual em implementar soluções tecnológicas para aprimorar a atuação jurídica da CAIXA."),
        .init(id: "projetos-pessoal-tech", area: "projetos", title: "Pessoal Tech", body: "Aprender e implementar soluções tecnológicas pessoais."),
        .init(id: "projetos-juridico-tech", area: "projetos", title: "Jurídico Tech", body: "Implementar soluções tecnológicas voltadas à área jurídica."),
        .init(id: "financas-liberdade", area: "financas", title: "Liberdade", body: "Objetivo financeiro principal: alcançar liberdade financeira."),
        .init(id: "aprendizado-ia-aplicada", area: "aprendizado", title: "IA Aplicada", body: "Estuda e quer aprender Tecnologia e Inteligência Artificial aplicadas a finanças e direito."),
        .init(id: "saude-saude", area: "saude", title: "Saúde", body: "Não treina atualmente, toma remédio para dormir e está em boas condições de saúde."),
        .init(id: "relacoes-camila", area: "relacoes", title: "Camila", body: "Casado com Camila desde 2000."),
        .init(id: "relacoes-matheus", area: "relacoes", title: "Matheus", body: "Pai de Matheus, nascido em 20/02/1997, engenheiro eletricista, casado com Amanda."),
        .init(id: "relacoes-pedro", area: "relacoes", title: "Pedro", body: "Pai de Pedro, nascido em 06/11/2002, médico, fazendo residência em cirurgia geral na USP Ribeirão Preto, namora Maria Fernanda."),
        .init(id: "relacoes-netos", area: "relacoes", title: "Netos", body: "Matheus e Amanda são pais de Miguel e Sofia.")
    ]

    static let relations: [(String, String)] = [
        ("voce-ildemar", "metas-ia"), ("voce-ildemar", "trabalho-caixa"), ("voce-ildemar", "relacoes-camila"),
        ("metas-ia", "trabalho-juridico-tech"), ("metas-ia", "projetos-pessoal-tech"), ("metas-ia", "projetos-juridico-tech"), ("metas-ia", "aprendizado-ia-aplicada"),
        ("metas-renda", "financas-liberdade"), ("metas-renda", "saude-saude"), ("metas-renda", "relacoes-netos"),
        ("trabalho-caixa", "trabalho-juridico-tech"), ("trabalho-juridico-tech", "projetos-juridico-tech"), ("trabalho-juridico-tech", "aprendizado-ia-aplicada"),
        ("projetos-pessoal-tech", "aprendizado-ia-aplicada"), ("projetos-juridico-tech", "aprendizado-ia-aplicada"),
        ("financas-liberdade", "aprendizado-ia-aplicada"), ("relacoes-camila", "relacoes-matheus"), ("relacoes-camila", "relacoes-pedro"),
        ("relacoes-matheus", "relacoes-netos"), ("relacoes-matheus", "relacoes-pedro"), ("relacoes-pedro", "relacoes-netos")
    ]
}

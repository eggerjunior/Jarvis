# Jarvis — instruções para agentes

Branch padrão: `main`. Stack principal: Swift/SwiftUI, Python e JavaScript/React.
Preserve a modificação pré-existente em `.gitignore`.

Não há workflows GitHub Actions atuais. Antes de criar CI, leia
`ildemar-github-actions-self-hosted`; qualquer build iOS/Xcode deve seguir
`ildemar_ios-native-testflight` em macOS, nunca Linux. A skill antiga
`.claude/skills/ios-native-testflight` deve ser tratada como legado até revisão,
sem ser apagada automaticamente. Não faça deploy, release, bump, push ou alteração
de secrets sem solicitação explícita.


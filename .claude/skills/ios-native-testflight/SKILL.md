---
name: ios-native-testflight
description: OBSOLETA. Cópia histórica do fluxo iOS/TestFlight do Jarvis; não usar para trabalho novo. Use a skill canônica global ildemar_ios-native-testflight e, para CI Linux/runners, ildemar-github-actions-self-hosted.
---

# iOS Native TestFlight

> **Obsoleta:** preservada apenas como histórico. Não execute este fluxo, pois ele
> contém caminhos locais antigos e antecede o padrão `macos-26`, assinatura Manual
> em Release e autorização explícita para publicação. Leia
> `/config/.claude/skills/ildemar_ios-native-testflight/SKILL.md` e
> `/config/.claude/skills/ildemar-github-actions-self-hosted/SKILL.md`.

## Regra Principal

Aplicar esta skill junto com `app-versioning` sempre que houver build distribuído. Antes de editar, leia também:

- `/Users/ildemareggerjunior/.codex/skills/app-versioning/SKILL.md`
- `/Users/ildemareggerjunior/.codex/skills/app-versioning/references/xcode-swift.md`
- `references/ildemar-ios-release.md`

Nunca gravar nem exibir segredos: conteúdo de `.p8`, tokens JWT, senhas, API keys, `asc.env` real, certificados privados ou session tokens. Usar placeholders e caminhos locais. Arquivos com segredos devem ficar no filesystem local e no `.gitignore`.

## Fluxo Obrigatório

1. Definir nome do app, slug, bundle id `br.app.egger.<slug>`, SKU igual ao bundle id e repo GitHub privado em `eggerjunior/<RepoName>`.
2. Criar ou atualizar projeto iOS nativo em `ios/` usando XcodeGen (`project.yml`) e SwiftUI, salvo se o repo já tiver outro padrão claro.
3. Implementar versionamento completo conforme `app-versioning`: `MARKETING_VERSION`, `CURRENT_PROJECT_VERSION`, `GIT_COMMIT`, `VersionManager`, `VersionHistory`, UI de versão/changelog e commit link.
4. Criar `.gitignore` antes de qualquer commit, incluindo `*.p8`, `**/asc.env`, `.env*`, `DerivedData/`, `*.xcarchive`, `*.ipa`, `*.xcdistributionlogs`.
5. Criar repo GitHub privado com `gh repo create <owner>/<repo> --private --source . --remote origin --push` ou usar `scripts/ensure_private_github_repo.sh`.
6. Criar/configurar App Store Connect:
   - preferir API com `scripts/create_app.py` quando aplicável;
   - se a API recusar por permissão/termo pendente, informar exatamente o bloqueio e pedir criação manual mínima do app record.
7. Gerar projeto com `xcodegen generate`, validar build settings, fazer commit e push antes do release.
8. Rodar `ios/scripts/testflight.sh`, que injeta `GIT_COMMIT="$(git rev-parse --short=8 HEAD)"`, arquiva, exporta e envia ao App Store Connect.
9. Reportar versão, build, commit, repo, bundle id, status do upload e qualquer pendência do processamento Apple.

## Utilitários

- `scripts/ensure_private_github_repo.sh`: inicializa git, configura remote e garante repo privado no GitHub.
- `scripts/write_ios_release_files.sh`: escreve `asc.env.example`, `ExportOptions.plist` e `testflight.sh` padrão no diretório `ios/`.
- `references/ildemar-ios-release.md`: inventário do ambiente, IDs não secretos, estrutura esperada e comandos completos.

Leia os scripts antes de executá-los se precisar adaptar nome do app, bundle id, team id ou owner.

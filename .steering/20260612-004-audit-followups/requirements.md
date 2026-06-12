# 要求内容: 監査残アクション対応 (audit-followups)

## 背景

`.steering/20260612-003-security-dependency-audit/report.md` の「検討推奨」3 件への対応。互換性は事前 Web 調査済み (sonnet、2026-06-12):

- Expo SDK 56 (npm 56.0.11) は **react-native 0.85 系を同梱** — 現在の `REACT_NATIVE ^0.85.2` はそのままで整合する。RN 0.86.0 (2026-06-08 リリース) は Expo 未対応の独立版
- pitest 1.25.4 は gradle-pitest-plugin 1.19.0 (現行最新) + pitest-junit5-plugin 1.2.3 (要求 1.19.4+) と互換

## 要求

1. `libs.versions.toml` の pitest を 1.20.4 → 1.25.4 に上げ、`./gradlew pitest` の実行で互換を検証する
2. `TemplateVersions.kt` の `EXPO` を `^55.0.17` → `^56.0.11` に上げる (RN 系定数は据え置き)。react-native テンプレートの golden 3 件を再生成する
3. dependabot の死角である TemplateVersions.kt の npm 定数を **monthly-verify.yml で自動 CVE 照合**する: 定数から package.json を生成し `npm audit` を実行、high/critical で fail。CLAUDE.md の CI 表と release.md の前提条件に反映する

## 受け入れ条件

- [ ] `./gradlew pitest` green (1.25.4)
- [ ] golden 再生成の差分が react-native の 3 件のみ
- [ ] audit スクリプトのローカル実行で TemplateVersions の全 npm 定数が package.json 化され npm audit が走る
- [ ] `./gradlew ktlintCheck test --rerun` green
- [ ] CLAUDE.md / release.md 同期。sphinx 更新なし (ユーザー向け機能不変)

## スコープ外 / 見送り

- **CLI テンプレートの RN 0.86 化**: Expo (0.85 同梱) と CLI で `REACT_NATIVE` 定数を分離する必要があり、急いで追従する理由 (リリース 4 日・CVE なし) もないため見送り。次回 RN バンプ時に定数分離と合わせて実施
- Docker イメージタグ (postgres/mysql) の自動照合 (npm audit の対象外)

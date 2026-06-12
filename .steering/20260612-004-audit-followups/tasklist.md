# タスクリスト: 監査残アクション対応

## セクション 1: pitest バンプ

- [ ] `libs.versions.toml` pitest 1.20.4 → 1.25.4
- [ ] `./gradlew pitest` 実行で互換検証 (targetClasses は RescriptPaths/RescriptRegexPatterns の 2 クラスのみ)
- [ ] コミット: `⬆ Bump pitest to 1.25.4`

## セクション 2: Expo SDK 56

- [ ] `TemplateVersions.kt` EXPO → `^56.0.11` (REACT_NATIVE 等は据え置き = SDK 56 同梱の RN 0.85 と整合)
- [ ] `compileTestKotlin --rerun` (const val stale インライン対策) → golden 再生成 → 差分が REACT_NATIVE テンプレート 3 件のみ確認
- [ ] wizard テスト green
- [ ] コミット: `⬆ Move the Expo template to SDK 56`

## セクション 3: TemplateVersions の CVE 照合自動化

- [ ] `.github/scripts/audit-template-versions.mjs` 新規 (TemplateVersions.kt の `const val X = "^1.2.3"` を正規表現抽出 → 定数名→npm パッケージ名のマッピングで package.json 生成 → 呼び出し側で npm audit)
- [ ] ローカル実行で全 npm 定数の抽出と npm audit 動作を確認
- [ ] `monthly-verify.yml` に template-versions-audit ジョブ追加 (npm i --package-lock-only → npm audit --audit-level=high)
- [ ] CLAUDE.md の CI 表 (Monthly Verify 行) と `.claude/rules/release.md` の前提条件に反映
- [ ] コミット: `🔧 Audit template npm versions monthly via generated package.json`

## マージ前検証

- [ ] `./gradlew ktlintCheck test --rerun` green
- [ ] tasklist 全項目 `[x]` 更新をマージ前最終コミットに含める
- [ ] `AskUserQuestion` でマージ可否確認 → main マージ → push

## テスト免除の記載

- audit スクリプト (.mjs): Kotlin テスト規約の対象外 (CI 補助スクリプト)。ローカル実行 + CI での実走で担保

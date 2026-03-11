# 設計: リリース品質向上

## 方針

### Tier 1: 既存設定の強化
- YAML/Gradle 設定の変更のみ。新規コード不要
- Qodana バイパス除去、リリース整合性チェック、カバレッジラチェット

### Tier 2: Gradle カスタムタスク
- `build.gradle.kts` にファイルシステムベースの検証タスクを追加
- CI ワークフローにステップとして組み込み
- KDoc チェック: 正規表現で class/object/interface 定義を検出し、直前に `/** */` が存在するか確認
- テストファイルチェック: プロダクションクラスに対応する `*Test.kt` の存在を確認（免除リスト付き）
- EP 登録チェック: plugin.xml の implementation 属性と src/main/kotlin の照合

### Tier 3: テストインフラ拡充
- テストフィクスチャ: レクサーエッジケース、全宣言型、JSX パターン等を追加
- パフォーマンスベンチマーク: 大規模フィクスチャに対するレクサー/パーサーの所要時間計測
- ヘッドレス IDE テスト: 既存テストインフラで十分なため、サンドボックス起動テストとして実装
- 脆弱性スキャン: Trivy の GitHub Action を CI に追加

## 影響範囲

| ファイル | 変更内容 |
|---------|---------|
| `.github/workflows/qodana_code_quality.yml` | 1.1 Dependabot 条件除去 |
| `.github/workflows/release.yml` | 1.2 整合性チェック, 2.4 承認ゲート |
| `.github/workflows/ci.yml` | 3.1 Trivy ステップ追加 |
| `build.gradle.kts` | 1.3 カバレッジ閾値, 2.1/2.2/2.3 カスタムタスク |
| `qodana.yaml` | 1.4 failThreshold |
| `.claude/rules/release.md` | 1.3 ラチェットポリシー追記 |
| `src/test/testData/` | 3.2 フィクスチャ追加 |
| `src/test/kotlin/.../` | 3.3 ベンチマーク, 3.4 スモークテスト |

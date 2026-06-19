# 要求内容

## 背景

CI 品質監査の結果、`main` と Release パイプラインが RED になっていることが判明した。

- 根本原因: `SmartElementDescriptor.getPsiElement()` が 2026.2 EAP (IU-262.8117.x) で `@ApiStatus.Internal` に再分類された。`build.gradle.kts` の `pluginVerification.ides { recommended() }` が EAP に追従するため、`verifyPlugin` が `[INTERNAL_API_USAGES]` で失敗する。
- コミット `fe0fe758` が `plugin-verifier-ignored-problems.txt` にエントリを追加して抑制を試みたが、`-ignored-problems` は compatibility problem 専用で `INTERNAL_API_USAGES` の failureLevel ゲートを抑制できず、無効だった(2 つの独立した CI 実行で実証済み)。
- 副次的に、ドキュメントがワークフロー数を誤記(4 と記載、実際は 7)。
- Release の `publish` ジョブが `validate-and-build` のみに依存し、main の CI workflow が green であることを保証していない。

## 受け入れ条件

### 1. 本質的な内部 API 依存の解消

- [ ] `hierarchy/` パッケージの 8 箇所の `SmartElementDescriptor.getPsiElement()`(Kotlin の `.psiElement` / `psiElement`)使用を、内部 API に依存しない公開アクセサに置き換える
- [ ] 置き換えは `SmartPsiElementPointer` ベースで、PSI 再パース後の無効化セマンティクス(無効時 null)を保持する
- [ ] 抑制が不要になるため、`plugin-verifier-ignored-problems.txt` の `SmartElementDescriptor.getPsiElement` エントリを削除する
- [ ] テストコード内の `.psiElement` 参照も新アクセサに更新する
- [ ] CI の `verifyPlugin` が green になる(EAP を含む 3 IDE で `INTERNAL_API_USAGES` ゼロ)

### 2. ドキュメント乖離の修正

- [ ] `CLAUDE.md` の CI/CD セクションに全 7 ワークフロー(CI / Release / Docs / Monthly Verify / CodeQL / Integration Tests / OS Matrix)を記載する
- [ ] `docs/repository-structure.md` の `.github/workflows/` 一覧に全 7 ワークフローを記載する

### 3. publish に CI グリーン必須化

- [ ] Release workflow の `publish` ジョブが、タグ対象コミットの CI workflow が `success` であることを必須とする
- [ ] CI がまだ実行中の場合は完了まで待機し、`success` 以外なら publish を中止する
- [ ] 最小権限(`actions: read`)で実装し、外部アクション依存を増やさない

## 制約

- ローカルに Java が無いため `./gradlew` を実行できない。検証は feature ブランチ push 後の CI で行う。
- ktlint / KDoc 規約を満たすこと(既存コードスタイルに合わせる)。

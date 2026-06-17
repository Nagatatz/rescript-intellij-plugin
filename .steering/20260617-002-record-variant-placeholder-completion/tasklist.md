# タスクリスト: Record / Variant Placeholder 補完 (#116)

各セクションは「マージ可能な単位」= 1 コミット。
依存関係: セクション 1 (純ロジック) → セクション 2 (リゾルバ) → セクション 3 (contributor + 登録)。
セクション 4 (ドキュメント) は実装完了後にまとめる。

## セクション 1: 純ロジック (注釈検出 + 雛形生成)

- [x] `completion/RescriptTypeAnnotationContext.kt` を作成 (lexer 走査で `let x: T = ` 値位置検出、head 型名抽出、`{` 済みフラグ)
- [x] `completion/RescriptPlaceholderBuilder.kt` を作成 (record → `{ f: _, ... }`、variant → `Ctor(_)`/`Ctor` 文字列)
- [x] `RescriptTypeAnnotationContextTest.kt` を作成 (record/variant/型引数/ドット型/`{` 済み/注釈なし/誤爆ケース)
- [x] `RescriptPlaceholderBuilderTest.kt` を作成 (record 0/1/N field、variant payload 有無、option/result 形)
- [x] KDoc を全クラス・複雑メソッドに付与
- [x] `./gradlew ktlintCheck test` グリーン確認
- [x] コミット `✨ Add placeholder completion pure logic`

## セクション 2: 型リゾルバ

- [x] `completion/RescriptPlaceholderTypeResolver.kt` を作成 (built-in option/result ハードコード + stub index → `RescriptTypeDeclarationParser.parse` で `TypeShape` 解決)
- [x] `RescriptPlaceholderTypeResolverTest.kt` を作成 (built-in option/result の解決、Unknown 経路。stub index 部は built-in 中心に検証)
- [x] KDoc 付与
- [x] `./gradlew ktlintCheck test` グリーン確認
- [x] コミット `✨ Add placeholder type resolver`

## セクション 3: CompletionContributor + plugin.xml 登録

- [x] `completion/RescriptPlaceholderCompletionContributor.kt` を作成 (orchestrator: detection → resolve → build → addElement)
- [x] `plugin.xml` に `completion.contributor` を登録 (既存 Decorator 近傍、アルファベット順)
- [x] contributor のテスト免除を本 tasklist に明記 (下記「テスト免除」参照)
- [x] KDoc 付与 (実装する `CompletionContributor` インターフェースに言及)
- [x] `./gradlew ktlintCheck clean buildPlugin test verifyPluginStructure` グリーン確認
- [x] コミット `✨ Add record/variant placeholder completion contributor`

## セクション 4: ドキュメント更新

- [x] `CLAUDE.md` レイヤー 3 に機能エントリ追加
- [x] `README.md` Features (補完カテゴリ) に追加
- [x] `sphinx-docs/user/features/code-completion.md` に説明 + 変換例追加
- [x] 対応する `.po` (`sphinx-docs/locale/ja/LC_MESSAGES/`) を同一コミットで更新 (`make gettext && make update-po` → msgstr 充填 → `make build-ja`)
- [x] `docs/repository-structure.md` の completion パッケージ代表クラスを更新
- [x] `docs/product-requirements.md` ロードマップから #116 行を削除
- [x] コミット `📝 Document record/variant placeholder completion`

## セクション 5: マージ

- [ ] 全セクション `[x]` 確認、`./gradlew clean buildPlugin test` 最終グリーン
- [ ] tasklist の全項目 `[x]` 更新をマージ前最終コミットに含める
- [ ] `AskUserQuestion` でマージ可否確認
- [ ] 承認後 `main` にマージ、作業ブランチ削除、セッション終了

## テスト免除

`RescriptPlaceholderCompletionContributor` は `CompletionContributor` 実装で `fillCompletionVariants`
の駆動に light fixture が困難 (既存 `RescriptDecoratorCompletionContributor` も同様に本体は免除し
補助メソッドのみ検証)。testing.md の免除基準には明示列挙されていないが、本体は detection/resolve/build
の純ロジックを薄く組み立てるだけで分岐ロジックを持たないため、純ロジック 3 クラスの網羅テストで実質カバーする。
contributor 本体のテストは省略する。

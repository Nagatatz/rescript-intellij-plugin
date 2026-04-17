# 要求内容: テストコンパイラ警告クリーンアップ

## 背景

`./gradlew test` 実行時に Kotlin コンパイラが **53 件の警告**を出力している（`.kt` テストソースのみ）。すべて既存のテストコードに由来するもので、プロダクションコードは警告ゼロ。警告は次の 6 種類に分類できる:

| 種類 | 件数 | 代表例 |
|---|---|---|
| `Check for instance is always 'true'` | 45 | `assertTrue(inspection is LocalInspectionTool)` で `inspection` の静的型がすでに `LocalInspectionTool` を満たす |
| `No cast needed` | 2 | `null as? PsiElement`、`x as Y` で実質キャストが不要 |
| Deprecated API 呼び出し | 3 | `AnActionEvent.createFromDataContext(...)` |
| Deprecated member override 注釈欠落 | 1 | `RescriptCallHierarchyProviderTest.kt:49` |
| `Unchecked cast` | 1 | `RescriptDependencyAnalyzerTest.kt:262` の `Array<*> as Array<PsiElement>` |
| Windows で問題になる文字を含むテスト名 | 1 | `RescriptLspSignatureParserTest.kt:44` の `... =?` |

影響ファイル数: **30 ファイル**（すべて `src/test/kotlin/` 配下）。プロダクションコードは変更しない。

## 目的

1. Kotlin コンパイラ警告をテスト側でゼロにする
2. 無意味な「常に true」アサーションを削除 or 意味のある検証に置き換え、テスト品質を向上させる
3. 非推奨 API 利用を最新 API に置き換え、将来の Platform バージョン更新時の破損を予防する

## スコープ

### In scope

- `src/test/kotlin/` 配下の 30 ファイルのみの編集
- 既存テストケースの書き換え（削除を含む）
- 必要に応じて `@Suppress(...)` 注釈の追加（正当な理由がある場合のみ）
- 変更後、`./gradlew ktlintCheck buildPlugin test` が警告ゼロで成功する

### Out of scope

- プロダクションコードの変更
- 新規テストケースの追加（純粋なクリーンアップに限定）
- 動作不変のテストロジック自体の変更（パッチ対象は警告が出ている行のみ）
- `plugin.xml` の変更（別途処理する）
- `RescriptListSplitJoinContext.kt` の KDoc バグ修正（別途処理する）

## 受け入れ条件

- [ ] `./gradlew test` 実行時、テストコードから Kotlin コンパイラ警告 `w:` が 0 件になる（注: プラグイン更新バナー等のプラットフォーム警告は対象外）
- [ ] `./gradlew ktlintCheck` が成功する
- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] すべてのテストが成功する（現在のテスト通過数を下回らない）
- [ ] 削除したテスト or 書き換えたアサーションは、意味のある検証を維持している（タウトロジー削除 or より具体的な主張への置き換え）

## 非受け入れ条件（やらないこと）

- `@Suppress("KotlinConstantConditions")` を全箇所に機械的に付与する（警告を隠蔽するのみで、無意味な検証が残り続ける）
- テストを大量に削除してカバレッジを低下させる（Kover の `minBound=86` を下回らないこと）

## リスク

- **既存テストの意図を誤って変更**: 特に「常に true」アサーションは「クラスが X を実装している」という型レベルの契約を意図していた可能性があるため、削除ではなく型を汎化して意味のあるチェックに変換するケースを個別に判断する
- **Deprecated API 置換時の動作差**: `AnActionEvent.createFromDataContext` → 新 API で引数の順序やセマンティクスが変わる可能性がある（テスト実行で確認）
- **カバレッジ低下**: テスト削除によりカバレッジラチェット（`minBound=86`）を下回る可能性。削除より書き換えを優先する

# Design: Deprecated API Migration

調査結果と実装で確認したこと:

| API | 2026.1.1 での状況 | 対応 |
|---|---|---|
| `CompletionConfidence.shouldSkipAutopopup(PsiElement, PsiFile, int)` | 4-arg overload (`Editor` first) が追加され、3-arg 版は `@Deprecated` 付き | **置換** |
| `FloatingToolbarProvider.isApplicable(DataContext)` | 2026.1.1 ではまだ deprecated 注釈なし。`isApplicableAsync` も未存在 | **suppression 維持** |
| `FileIncludeProvider.acceptFile(VirtualFile)` | 2026.1.1 では abstract、`acceptFile(IndexedFile)` は未存在 | **suppression 維持** |
| `MarkedString` (LSP4J) | `Hover.contents` の左ブランチ内で `Either.right.value` 経由で参照 | **左ブランチの右経路を撤去** |

Marketplace 側の Plugin Verifier は EAP IDE に対しても走るため、新しい IDE で deprecated 化された 2 つの API も警告対象になっている。compile target が 2026.1.1 である間は replacement が存在しないため、suppression を維持する。

## 1. CompletionConfidence.shouldSkipAutopopup → 4-arg

**変更**:
```kotlin
- @Suppress("OVERRIDE_DEPRECATION")
  override fun shouldSkipAutopopup(
+     editor: Editor,
      contextElement: PsiElement,
      psiFile: PsiFile,
      offset: Int,
  ): ThreeState
```

`Editor` 引数は本判定 (トークン種別判定) では使わないが、新 API のシグネチャに従う。既存テストは `shouldSkipAutopopup` を直接呼ばないため変更不要。

## 2. FloatingToolbarProvider.isApplicable: suppression 維持

2026.1.1 の `FloatingToolbarProvider` は `isApplicable(DataContext)` を default として持ち、`isApplicableAsync` は未追加。Marketplace 側で deprecated 警告が出るのは Marketplace verifier が EAP/master IDE に対しても走るため。

`@Suppress("DEPRECATION")` と `plugin-verifier-ignored-problems.txt` の双方を維持。コメントを更新して「2026.1.1 にまだ存在しない」と明示。

## 3. FileIncludeProvider.acceptFile: suppression 維持

2026.1.1 では `acceptFile(VirtualFile)` が abstract。override 必須で `acceptFile(IndexedFile)` は未存在。Marketplace 側の deprecated 警告に対応するため `plugin-verifier-ignored-problems.txt` に新エントリを追加。本体コードは変更なし。

## 4. MarkedString 参照の除去

**変更**:
```kotlin
content.isLeft -> {
    content.left
        .firstOrNull()
-       ?.let { if (it.isLeft) it.left else it.right.value }
+       ?.takeIf { it.isLeft }
+       ?.left
}
```

`it.right` (= `Either.getRight()` returns `MarkedString`) と `.value` (= `MarkedString.getValue()`) の両方を撤去。これで bytecode から `MarkedString` への参照が完全に消える。

rescript-language-server は `MarkupContent` を返すため、`MarkedString` 経路は dead code。

## 5. plugin-verifier-ignored-problems.txt の再編

- `CompletionConfidence.*shouldSkipAutopopup` を削除（コード側で解消）
- `MarkedString.*` を削除（コード側で解消）
- `FloatingToolbarProvider.*isApplicable` は維持（コメント更新）
- `FileIncludeProvider.*acceptFile` を新規追加（Marketplace 警告抑制用）
- 見出しコメントを「on the compile target」に書き換え、`Reviewed` を 2026-05-14、`Expires` を 2027-05-14 に更新

## 6. テスト戦略

| クラス | 既存テスト |
|---|---|
| `RescriptCompletionConfidenceTest` | `shouldSkipAutopopup` を直接呼ばないため変更なし。クラス構築テストのみ |
| `RescriptFloatingToolbarProviderTest` | 本体無変更のため変更なし |
| `RescriptFileIncludeProviderTest` | 本体無変更のため変更なし |
| `RescriptLspUtilsTest` 等 | `getHoverType` の左ブランチ挙動は元から `it.isLeft` 経路のみテスト可能。実害なし |

## 7. リスク

- `MarkedString` 経路の撤去により、もし将来 LSP サーバーが `MarkedString[]` を返した場合 hover が空になる。rescript-language-server は `MarkupContent` 固定のため実害なし
- `FloatingToolbarProvider` / `FileIncludeProvider` の修正は将来 platformVersion を `isApplicableAsync` / `acceptFile(IndexedFile)` 同梱版にバンプしたタイミングで実施

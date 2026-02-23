# Design: #51 Stub Index

## アーキテクチャ

### 変更前
- `RescriptElementTypes` の宣言型は全て `RescriptElementType`（plain `IElementType`）
- `RescriptParserDefinition.createElement()` は全ノードに `ASTWrapperPsiElement` を返す
- `RescriptParserDefinition.FILE` は `IFileElementType`（非スタブ）
- シンボル検索は `FileTypeIndex.getFiles()` → 各ファイルの PSI ツリーを走査

### 変更後
- 5つの宣言型を `RescriptDeclarationElementType`（`IStubElementType` サブクラス）に置換
- 宣言用 PSI クラス `RescriptDeclarationPsiElement` を導入
- `IStubFileElementType` に移行し、ファイルスタブを有効化
- `RescriptNameIndex` + `RescriptModuleIndex` でシンボルの高速ルックアップ
- `RescriptSymbolContributor` を StubIndex ベースに書き換え

### 非スタブ要素（変更なし）
OPEN_STATEMENT, INCLUDE_STATEMENT, ANNOTATION, JSX_* — 引き続き `RescriptElementType` + `ASTWrapperPsiElement`

## 新規ファイル

| ファイル | 目的 |
|---------|------|
| `lang/psi/RescriptDeclarationStub.kt` | スタブデータ（name, declarationType） |
| `lang/psi/RescriptDeclarationElementType.kt` | `IStubElementType` 実装 |
| `lang/psi/RescriptDeclarationPsiElement.kt` | `StubBasedPsiElementBase` 継承 PSI |
| `lang/psi/RescriptFileStub.kt` | `PsiFileStubImpl` 実装 |
| `indexing/RescriptNameIndex.kt` | 全宣言名インデックス |
| `indexing/RescriptModuleIndex.kt` | モジュール専用インデックス |

## 変更ファイル

| ファイル | 変更内容 |
|---------|---------|
| `lang/psi/RescriptPsi.kt` | 5つの宣言型を `RescriptDeclarationElementType` に置換 |
| `lang/RescriptParserDefinition.kt` | `IStubFileElementType` 化 + `createElement` 分岐 |
| `navigation/RescriptSymbolContributor.kt` | StubIndex ベースに書き換え |
| `plugin.xml` | `stubElementTypeHolder` + `stubIndex` ×2 登録 |

## 互換性

`IStubElementType` は `IElementType` のサブクラスなので、`elementType` チェック（`==`, `in Set<IElementType>`）は透過的に動作する。25+ ファイルの既存コードに影響なし。

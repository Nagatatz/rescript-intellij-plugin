# Design: Call Hierarchy (#44)

## アーキテクチャ

既存の Module Hierarchy (`hierarchy/`) と同じパターンに従い、`hierarchy/call/` サブパッケージに配置。

### ファイル構成

```
hierarchy/call/
├── RescriptCallHierarchyProvider.kt     — EP エントリポイント
├── RescriptCallHierarchyBrowser.kt      — UI ブラウザ
├── RescriptCallHierarchyNodeDescriptor.kt — ノード描画
├── RescriptCallerTreeStructure.kt       — 呼び出し元ツリー
├── RescriptCalleeTreeStructure.kt       — 呼び出し先ツリー
└── RescriptCallAnalyzer.kt              — 呼び出し関係分析（テスタブル）
```

## 実装詳細

### RescriptCallAnalyzer (核心ロジック)

`object` として実装。テスト可能な純粋ユーティリティ。

**Callers 検索:**
1. `RescriptPsiUtils.extractName(target)` で関数名を取得
2. `PsiSearchHelper.processElementsWithWord()` でプロジェクト全体から関数名を検索
3. 各一致箇所で `findEnclosingDeclaration()` を使い親の LET_DECLARATION を取得
4. 自分自身（target）を除外

**Callees 検索:**
1. target（LET_DECLARATION）の本体部分（`=` 以後のノード）を取得
2. 本体内の LIDENT トークンを収集
3. 各トークンについて同ファイル内の他の LET_DECLARATION と名前照合
4. 一致するものを CallReference として返す

### RescriptCallHierarchyProvider

`HierarchyProvider` 実装。カーソル位置の LET_DECLARATION を target として取得。

### RescriptCallHierarchyBrowser

`HierarchyBrowserBaseEx` 継承。Callers/Callees の2ビューを持つ。

### plugin.xml 登録

```xml
<callHierarchyProvider language="ReScript"
                       implementationClass="com.rescript.plugin.hierarchy.call.RescriptCallHierarchyProvider"/>
```

## 再利用する既存コード

| ユーティリティ | ファイル | 用途 |
|---------------|---------|------|
| `RescriptPsiUtils.extractName()` | `lang/psi/RescriptPsiUtils.kt` | 関数名取得 |
| `RescriptPsiUtils.getIcon()` | 同上 | アイコン取得 |
| `RescriptPsiUtils.findEnclosingDeclaration()` | 同上 | 囲む宣言の検索 |
| `RescriptPsiUtils.BINDING_TYPES` | 同上 | let/external 判定 |
| `RescriptModuleHierarchyNodeDescriptor` | `hierarchy/` | ノード描画パターン |
| `RescriptModuleHierarchyBrowser` | `hierarchy/` | ブラウザ UI パターン |

## テスト計画

- `RescriptCallAnalyzerTest` — CallReference データクラス、トークン抽出ロジック
- `RescriptCallHierarchyProviderTest` — インスタンス化、基本動作
- PsiSearchHelper を用いるプロジェクト横断検索は IDE 結合テストとして免除

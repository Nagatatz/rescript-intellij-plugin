# ReScript IntelliJ Plugin

> **Note (2026-02):** This document was created during the initial planning phase and reflects the original concept. The plugin has since grown significantly beyond the scope described here (8,500+ Kotlin lines, 60+ source files, full LSP integration with semantic tokens, code generation, debugger support, etc.). For the current state, refer to `CLAUDE.md` and `docs/product-requirements.md`.

[ReScript](https://rescript-lang.org) の言語サポートを JetBrains IDE に提供するプラグインです。

既存の [reasonml-idea-plugin](https://github.com/reasonml-editor/reasonml-idea-plugin) は 2025-09 を最後にコードリリースが停滞し低頻度メンテに移行しているため、ReScript 専用のクリーンな設計をフルスクラッチで新規に開発しています。ReScript/Reason/OCaml の3言語共有アーキテクチャを捨て、ReScript 専用のクリーンな設計を採用しました。

## 特徴

**ネイティブ機能（プラグイン内蔵）**
- シンタックスハイライト — JFlex レクサーによる高速なトークンベースのカラーリング
- コード折りたたみ — モジュール、let/type 宣言、ブロックコメントの折りたたみ
- ブレースマッチング — `{}`、`[]`、`()` の自動対応
- コメント — 行コメント (`//`) とブロックコメント (`/* */`) のトグル

**LSP 連携（rescript-language-server 経由）**
- コード補完
- 定義ジャンプ
- ホバードキュメント
- 参照検索
- リアルタイム診断（エラー・警告表示）
- インレイヒント（型注釈）

## アーキテクチャ

```
┌─────────────────────────────────────────────┐
│              IntelliJ Plugin (Kotlin)        │
├──────────────────┬──────────────────────────┤
│  レイヤー 1       │  レイヤー 2              │
│  言語基盤         │  LSP 統合                │
│                  │                          │
│  ・JFlex Lexer   │  ・IntelliJ LSP API      │
│  ・軽量 Parser   │  ・stdio 通信             │
│  ・PSI 構造      │  ・自動サーバー検出        │
│  ・ハイライト     │                          │
├──────────────────┴──────────────────────────┤
│          rescript-language-server             │
│          (@rescript/language-server)          │
└─────────────────────────────────────────────┘
```

このプラグインは **ハイブリッドアプローチ** を採用しています。

1. **JFlex レクサー** — ReScript のソースコードをトークンに分解し、高速なシンタックスハイライトを提供します。外部ツールに依存せず、エディタ入力に対して即座に反応します。

2. **軽量パーサー** — トップレベル宣言（`let`、`type`、`module`、`external`、`open`、`include`、`exception`）のみを認識する最小限の PSI ツリーを構築します。コード折りたたみやストラクチャービューのために使用され、JSX を含む複雑な式のパースは行いません。

3. **LSP 統合** — 意味解析を伴うすべての機能（補完、診断、ナビゲーション、ホバーなど）は [rescript-language-server](https://github.com/rescript-lang/rescript-vscode/tree/master/server) に委譲します。IntelliJ Platform の LSP API（2023.2+）を使用し、VSCode 拡張と同等の機能をそのまま利用できます。

### なぜフルスクラッチなのか

既存の reasonml-idea-plugin は Java 47,200行、561ファイルの巨大なコードベースで、ReScript/Reason/OCaml の3言語がコア層188ファイルを共有する複雑な構造でした。特に手書きの再帰下降パーサー（1,499行）における JSX 処理が65箇所に分散しており、改修よりも新規設計の方が結果的に低コストと判断しました。

LSP に意味解析を委譲することで、JSX パースの複雑さを根本的に回避しつつ、rescript-language-server のアップデートに自動追従できます。

## 動作要件

- IntelliJ IDEA Ultimate 2024.2+（または LSP API をサポートする JetBrains IDE）
- Node.js（PATH で利用可能なこと）
- `@rescript/language-server`

```bash
# プロジェクトローカルにインストール（推奨）
npm install @rescript/language-server

# またはグローバルインストール
npm install -g @rescript/language-server
```

> **Note:** LSP API は IntelliJ 2025.3 から Community Edition でも利用可能になる予定です。それまでは Ultimate（または商用 IDE）が必要です。

## プロジェクト構成

```
src/
├── main/
│   ├── kotlin/com/rescript/plugin/
│   │   ├── RescriptLanguage.kt          # 言語定義
│   │   ├── RescriptFileTypes.kt         # .res / .resi ファイルタイプ
│   │   ├── RescriptIcons.kt             # アイコン
│   │   ├── lang/
│   │   │   ├── RescriptTokenTypes.kt    # トークン型定義
│   │   │   ├── RescriptLexer.kt         # JFlex ラッパー
│   │   │   ├── RescriptParser.kt        # 軽量パーサー
│   │   │   ├── RescriptParserDefinition.kt
│   │   │   └── psi/RescriptPsi.kt       # PSI 要素
│   │   ├── highlight/
│   │   │   ├── RescriptSyntaxHighlighter.kt
│   │   │   ├── RescriptSyntaxHighlighterFactory.kt
│   │   │   └── RescriptBraceMatcher.kt
│   │   ├── lsp/
│   │   │   ├── RescriptLspServerSupportProvider.kt
│   │   │   └── RescriptLspServerDescriptor.kt
│   │   ├── folding/RescriptFoldingBuilder.kt
│   │   └── commenter/RescriptCommenter.kt
│   ├── java/com/rescript/plugin/lang/
│   │   └── Rescript.flex                 # JFlex レクサー定義
│   └── resources/
│       ├── META-INF/plugin.xml
│       └── icons/
└── test/
```

Kotlin 15ファイル（約760行）+ JFlex 定義1ファイル（284行）で構成されています。

## 開発

### 前提条件

- JDK 21+
- IntelliJ IDEA

### ビルド

```bash
./gradlew buildPlugin
```

### 実行（開発用 IDE インスタンス起動）

```bash
./gradlew runIde
```

### JFlex レクサーの生成

`Rescript.flex` から Java レクサーを生成する必要があります。

1. [Grammar-Kit](https://plugins.jetbrains.com/plugin/6606-grammar-kit) プラグインをインストール
2. `src/main/java/com/rescript/plugin/lang/Rescript.flex` を開く
3. 「Generate JFlex Lexer」を実行（Ctrl+Shift+G）

生成された `RescriptFlexLexer.java` は `.gitignore` に含まれています。

## 技術スタック

- **Kotlin** 2.0 — プラグイン本体
- **IntelliJ Platform SDK** 2024.2 — IDE 統合基盤
- **JFlex** — レクサー生成（Java コード生成）
- **LSP** — rescript-language-server との通信

## ロードマップ

- [ ] カラースキーム設定 UI
- [ ] rescript.json の自動検出とプロジェクト設定
- [ ] ReScript コンパイラ実行とビルドツール統合
- [ ] ストラクチャービュー（ファイル内シンボル一覧）
- [ ] IntelliJ 2025.3 Community Edition 対応
- [ ] JetBrains Marketplace への公開

## ライセンス

MIT
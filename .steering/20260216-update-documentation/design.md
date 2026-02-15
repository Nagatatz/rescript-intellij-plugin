# Design: CLAUDE.md / README.md の更新

## 変更方針

既存のドキュメント構造・文体を維持しつつ、不足している情報を追加・修正する。

## CLAUDE.md の変更内容

### 1. プロジェクト構成図

現在の構成図を実際のファイル構成に合わせて更新する。追加するエントリ：

```
src/main/
├── kotlin/com/rescript/plugin/
│   ├── ...（既存ファイル）
│   ├── codestyle/
│   │   ├── RescriptCodeStyleSettingsProvider.kt
│   │   └── RescriptLineIndentProvider.kt
│   ├── config/
│   │   └── RescriptJsonIconProvider.kt
│   ├── highlight/
│   │   ├── ...（既存ファイル）
│   │   └── RescriptColorSettingsPage.kt          # 追加
│   ├── lsp/
│   │   ├── ...（既存ファイル）
│   │   └── RescriptSemanticTokensSupport.kt      # 追加
│   ├── run/
│   │   ├── RescriptCliDetector.kt
│   │   ├── RescriptCommand.kt
│   │   ├── RescriptConfigurationFactory.kt
│   │   ├── RescriptRunConfiguration.kt
│   │   ├── RescriptRunConfigurationOptions.kt
│   │   ├── RescriptRunConfigurationType.kt
│   │   └── RescriptSettingsEditor.kt
│   └── structure/
│       ├── RescriptStructureViewElement.kt
│       ├── RescriptStructureViewFactory.kt
│       └── RescriptStructureViewModel.kt
└── resources/
    ├── META-INF/plugin.xml
    ├── colorSchemes/                              # 追加
    │   ├── RescriptDarcula.xml
    │   └── RescriptDefault.xml
    └── icons/
```

### 2. アーキテクチャ説明

「レイヤー 1: 言語基盤」に以下を追記：
- ストラクチャービュー（`structure/`）

「レイヤー 2: LSP 統合」に以下を追記：
- セマンティックトークンハイライト

新規セクション「レイヤー 3: IDE 統合機能」を追加：
- 実行構成（`run/`）— rescript.json 経由の ReScript ビルド実行
- コードスタイル（`codestyle/`）— インデント設定
- カラースキーム — Darcula / Default テーマ用配色
- rescript.json アイコン（`config/`）

## README.md の変更内容

### 1. Requirements セクション

```diff
- IntelliJ IDEA Ultimate 2024.2+ (or other JetBrains IDE with LSP support)
+ IntelliJ IDEA 2025.3+ (or other JetBrains IDE with LSP support)
```

### 2. Features セクション

以下を追記：
- **Structure view** — Navigate module, function, and type declarations
- **Run configuration** — Build ReScript projects from IDE
- **Semantic highlighting** — Enhanced token coloring via LSP semantic tokens
- **Code style** — Indentation settings for ReScript files
- **rescript.json icon** — Custom icon for ReScript configuration files

### 3. Generate Lexer セクション

Grammar-Kit 手動手順を Gradle タスクによる自動生成に書き換え：

```markdown
### Generate Lexer

The JFlex lexer is automatically generated from `Rescript.flex` during the build process
via the `generateRescriptLexer` Gradle task (dependency of `compileJava` / `compileKotlin`).
Manual generation is not required.
```

## 影響範囲

- CLAUDE.md と README.md のみ（コード変更なし）
- 他のドキュメント（docs/ 配下）への影響なし

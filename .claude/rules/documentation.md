---
paths:
  - "**/*.md"
  - "docs/**"
  - ".steering/**"
---

# ドキュメント管理規約

## ドキュメントの分類

### 1. 永続的ドキュメント（`docs/`）

アプリケーション全体の「**何を作るか**」「**どう作るか**」を定義する恒久的なドキュメント。
アプリケーションの基本設計や方針が変わらない限り更新されません。

- **product-requirements.md** - プロダクト要求定義書
　- プロダクトビジョンと目的
　- ターゲットユーザーと課題・ニーズ
　- 主要な機能一覧
　- 成功の定義
　- ビジネス要件
　- ユーザーストーリー
　- 受け入れ条件
　- 機能要件
　- 非機能要件

- **functional-design.md** - 機能設計書
　- 機能ごとのアーキテクチャ
　- システム構成図
　- データモデル定義（ER図含む）
　- コンポーネント設計
　- ユースケース図、画面遷移図、ワイヤフレーム
　- API設計（将来的にバックエンドと連携する場合）

- **architecture.md** - 技術仕様書
　- テクノロジースタック
　- 開発ツールと手法
　- 技術的制約と要件
　- パフォーマンス要件

- **repository-structure.md** - リポジトリ構造定義書
　- フォルダ・ファイル構成
　- ディレクトリの役割
　- ファイル配置ルール

- **development-guidelines.md** - 開発ガイドライン
　- コーディング規約
　- 命名規則
　- スタイリング規約
　- テスト規約
　- Git規約

- **glossary.md** - ユビキタス言語定義
　- ドメイン用語の定義
　- ビジネス用語の定義
　- UI/UX用語の定義
　- 英語・日本語対応表
　- コード上の命名規則

### 2. 作業単位のドキュメント（`.steering/[YYYYMMDD]-[NNN]-[開発タイトル]/`）

特定の開発作業における「**今回何をするか**」を定義する一時的なステアリングファイル。
作業完了後は参照用として保持されますが、新しい作業では新しいディレクトリを作成します。

- **requirements.md** - 今回の作業の要求内容
　- 変更・追加する機能の説明
　- ユーザーストーリー
　- 受け入れ条件
　- 制約事項

- **design.md** - 変更内容の設計
　- 実装アプローチ
　- 変更するコンポーネント
　- データ構造の変更
　- 影響範囲の分析

- **tasklist.md** - タスクリスト
　- 具体的な実装タスク
　- タスクの進捗状況
　- 完了条件

## 開発プロセス

### 初回セットアップ時の手順

#### 1. フォルダ作成
```bash
mkdir -p docs
mkdir -p .steering
```

#### 2. 永続的ドキュメント作成（`docs/`）

アプリケーション全体の設計を定義します。
各ドキュメントを作成後、必ず確認・承認を得てから次に進みます。

1. `docs/product-requirements.md` - プロダクト要求定義書
2. `docs/functional-design.md` - 機能設計書
3. `docs/architecture.md` - 技術仕様書
4. `docs/repository-structure.md` - リポジトリ構造定義書
5. `docs/development-guidelines.md` - 開発ガイドライン
6. `docs/glossary.md` - ユビキタス言語定義

**重要：** 1ファイルごとに作成後、必ず確認・承認を得てから次のファイル作成を行う

#### 3. 初回実装用のステアリングファイル作成

初回実装用のディレクトリを作成し、実装に必要なドキュメントを配置します。

```bash
mkdir -p .steering/[YYYYMMDD]-001-initial-implementation
```

作成するドキュメント：
1. `.steering/[YYYYMMDD]-001-initial-implementation/requirements.md` - 初回実装の要求
2. `.steering/[YYYYMMDD]-001-initial-implementation/design.md` - 実装設計
3. `.steering/[YYYYMMDD]-001-initial-implementation/tasklist.md` - 実装タスク

#### 4. 環境セットアップ

#### 5. 実装開始

`.steering/[YYYYMMDD]-001-initial-implementation/tasklist.md` に基づいて実装を進めます。

#### 6. 品質チェック

### 機能追加・修正時の手順

#### 1. 影響分析

- 永続的ドキュメント（`docs/`）への影響を確認
- 変更が基本設計に影響する場合は `docs/` を更新

#### 2. ステアリングディレクトリ作成

新しい作業用のディレクトリを作成します。

```bash
mkdir -p .steering/[YYYYMMDD]-[NNN]-[開発タイトル]
```

**例：**
```bash
mkdir -p .steering/20250115-001-add-tag-feature
```

#### 3. 作業ドキュメント作成

作業単位のドキュメントを作成します。
各ドキュメント作成後、必ず確認・承認を得てから次に進みます。

1. `.steering/[YYYYMMDD]-[NNN]-[開発タイトル]/requirements.md` - 要求内容
2. `.steering/[YYYYMMDD]-[NNN]-[開発タイトル]/design.md` - 設計
3. `.steering/[YYYYMMDD]-[NNN]-[開発タイトル]/tasklist.md` - タスクリスト

**重要：** 1ファイルごとに作成後、必ず確認・承認を得てから次のファイル作成を行う

#### 4. 永続的ドキュメント更新（必要な場合のみ）

変更が基本設計に影響する場合、該当する `docs/` 内のドキュメントを更新します。

#### 5. 実装開始

`.steering/[YYYYMMDD]-[NNN]-[開発タイトル]/tasklist.md` に基づいて実装を進めます。

#### 6. 品質チェック

## ドキュメント管理の原則

### 永続的ドキュメント（`docs/`）
- アプリケーションの基本設計を記述
- 頻繁に更新されない
- 大きな設計変更時のみ更新
- プロジェクト全体の「北極星」として機能

### 作業単位のドキュメント（`.steering/`）
- 特定の作業・変更に特化
- 作業ごとに新しいディレクトリを作成
- 作業完了後は履歴として保持
- 変更の意図と経緯を記録

## 図表・ダイアグラムの記載ルール

### 記載場所
設計図やダイアグラムは、関連する永続的ドキュメント内に直接記載します。
独立したdiagramsフォルダは作成せず、手間を最小限に抑えます。

**配置例：**
- ER図、データモデル図 → `functional-design.md` 内に記載
- ユースケース図 → `functional-design.md` または `product-requirements.md` 内に記載
- 画面遷移図、ワイヤフレーム → `functional-design.md` 内に記載
- システム構成図 → `functional-design.md` または `architecture.md` 内に記載

### 記述形式

**以下は強制的な行動指示であり、例外なく従うこと。**

図表・ダイアグラムを作成・更新する場合は、**必ず draw.io MCP ツールを使用すること**。ASCII アートや Mermaid 記法での図表作成は禁止する。

1. **draw.io MCP（必須）**
   - `mcp__drawio__open_drawio_mermaid` — Mermaid 記法で図を定義し、draw.io で描画する。フローチャート、シーケンス図、状態遷移図、クラス図等に使用
   - `mcp__drawio__open_drawio_xml` — draw.io XML で直接図を定義する。レイヤー図、カスタムレイアウトなど Mermaid では表現が難しい図に使用
   - `mcp__drawio__open_drawio_csv` — CSV データから図を生成する。組織図やテーブル構造の可視化に使用
   - 図はブラウザ上で draw.io エディタとして開かれ、ユーザーが確認・編集できる

2. **図の種類と推奨ツール:**

   | 図の種類 | 推奨ツール | 入力形式 |
   |---------|-----------|---------|
   | フローチャート | `open_drawio_mermaid` | `graph TD` / `flowchart TD` |
   | シーケンス図 | `open_drawio_mermaid` | `sequenceDiagram` |
   | 状態遷移図 | `open_drawio_mermaid` | `stateDiagram-v2` |
   | クラス図 | `open_drawio_mermaid` | `classDiagram` |
   | レイヤー図・積み重ね図 | `open_drawio_xml` | draw.io XML |
   | カスタムレイアウト | `open_drawio_xml` | draw.io XML |
   | テーブル構造・組織図 | `open_drawio_csv` | CSV |

3. **禁止事項:**
   - ASCII アート（罫線文字 `┌─┐│└─┘` 等）での図表作成
   - Markdown コードブロック内の Mermaid 記法（` ```mermaid ` ブロック）での図表埋め込み
   - 手書きテキストベースのツリー図（`├──`, `└──` 等）

4. **既存の ASCII / Mermaid 図を発見した場合:**
   - 新規作成・更新の機会があれば draw.io MCP で描き直すこと

### 図表の更新
- 設計変更時は対応する図表も draw.io MCP で同時に更新
- 図表とコードの乖離を防ぐ

## 注意事項

- ドキュメントの作成・更新は段階的に行い、各段階で承認を得る
- `.steering/` のディレクトリ名は日付とシリアル番号と開発タイトルで明確に識別できるようにする
- 永続的ドキュメントと作業単位のドキュメントを混同しない
- コード変更後は必ずリント・型チェックを実施する
- 図表は必要最小限に留め、メンテナンスコストを抑える

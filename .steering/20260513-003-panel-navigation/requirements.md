# 要求: 結果一覧 Panel にエディタジャンプを追加

## 背景

サンドボックステストで「利用例が出る機能はジャンプできた方がよさそう」と要望があった。現状のジャンプ実装状況:

| Panel | コンポーネント | ジャンプ |
|------|----------|---------|
| Type Impact | JBList | ✅ ダブルクリック → OpenFileDescriptor |
| Interop Risk | JBList | ✅ ダブルクリック → OpenFileDescriptor |
| Type Coverage | JBTable | ✅ ダブルクリック → OpenFileDescriptor |
| **Migration Pilot** | JBList | ❌ チェックボックス toggle のみ |
| **Variant Flow** | JTextArea | ❌ Mermaid テキスト表示のみ |
| **Dependencies** | JTree | ❌ ラベル文字列のみ |

## ユーザーストーリー

- **Migration Pilot ユーザーとして**、候補一覧の `.re` ファイルをダブルクリックして、変換前に中身をエディタで確認したい
- **Variant Flow ユーザーとして**、ToolWindow に表示された diagram から元の `switch` 式に戻ってカーソルジャンプしたい
- **Dependencies ユーザーとして**、ツリー内の依存パッケージをダブルクリックして `node_modules/<pkg>/package.json` を開きたい

## 受け入れ条件

- [ ] Migration Pilot の候補リストをダブルクリックすると当該 `.re` / `.rei` ファイルが開く（チェックボックス toggle はシングルクリックで継続）
- [ ] Variant Flow ツールバーに `Jump to switch` アクションが追加され、現在表示中の diagram の元 switch にカーソルを移動する
- [ ] Dependencies ツリーの依存パッケージノードをダブルクリックすると `node_modules/<pkg>/package.json` が開く（package.json が存在しないノードはなにも起こさない）
- [ ] すべての操作で `OpenFileDescriptor(project, file [, offset]).navigate(true)` を使用する
- [ ] pure な package.json 解決ヘルパーを切り出してユニットテストする

## 制約

- Panel 本体は Swing UI 例外で test 免除。挙動の純粋ロジックは抽出してテスト
- 既存の単一クリックハンドラ（Migration Pilot のチェックボックス toggle）は維持する
- Variant Flow の jump 用 source location は Panel 内 state として保持し、`FlowDiagram` data class は変更しない
- Dependencies のツリーノードは `userObject` を使った隠れデータ保持に切り替え、表示ラベル文字列はそのまま見える

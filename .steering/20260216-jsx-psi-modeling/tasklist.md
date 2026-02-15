# Tasklist: JSX PSI モデリング

## タスク一覧

### Phase 1: PSI 要素型の定義
- [x] T1: `RescriptElementTypes` に `JSX_ELEMENT`, `JSX_SELF_CLOSING_ELEMENT`, `JSX_FRAGMENT` を追加

### Phase 2: パーサーの拡張
- [x] T2: `RescriptParser` に JSX パースメソッドを追加 (`tryParseJsx`, `parseJsxChildren`, `skipJsxAttributes`, `expectClosingTag`)
- [x] T3: `skipToEndOfDeclaration` に `TAG_LT` 検出時の JSX パース呼び出しを統合
- [x] T4: `parseTopLevel` の `else` ブランチに `TAG_LT` の JSX パース呼び出しを追加

### Phase 3: コード折りたたみの拡張
- [x] T5: `RescriptFoldingBuilder` に `JSX_ELEMENT`, `JSX_FRAGMENT` の折りたたみ対応を追加

### Phase 4: テスト
- [x] T6: JSX パースのユニットテストを追加 (自己閉じタグ、開閉タグ、ネスト、フラグメント、コンポーネント、式コンテナ)
- [x] T7: 既存テストが全てパスすることを確認

### Phase 5: ビルド検証・コミット
- [x] T8: `./gradlew clean buildPlugin` でビルド成功を確認
- [x] T9: 変更をコミット (`✨ Add JSX PSI modeling to lightweight parser`)
- [x] T10: CLAUDE.md・README.md の更新が必要か確認し、必要なら更新してコミット

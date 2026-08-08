# 監査是正実装 — requirements

## 目的

`20260702-001-fable5-verification-audit` で確定した 23 所見を、8 チェックポイント（CP1〜CP8）として実装する。各 CP は独立ブランチで実装し、ktlint/build/test 緑を確認してから `main` にマージする（緑を刻む方針）。

## 参照

- 所見一覧・根拠: `.steering/20260702-001-fable5-verification-audit/requirements.md`
- 優先度・CP 分割: `.steering/20260702-001-fable5-verification-audit/design.md`

## 受け入れ条件

- 各 CP について:
  - [ ] 対象コードを修正し、対応するユニットテスト（回帰/エッジケース）を追加（testing.md）
  - [ ] 新規/変更クラスに英語 KDoc（code-comments.md）
  - [ ] `./gradlew ktlintCheck clean buildPlugin test --rerun` が緑
  - [ ] deprecated API 不使用（deprecated-api.md）
  - [ ] docs 同期が必要な CP（CP7）は EN/JA を同一コミット（documentation.md）
- CP6 はセキュリティ関連としてマージ確認時に明示（DoD Phase 4）。
- 全 CP マージ後、tasklist の全項目 `[x]`。

## 非機能

- コミット粒度は機能単位（git-conventions.md）。CP 内で refactor と fix が分かれる場合は複数コミット可。
- 本体コードを変更してテストを通すのは可（バグ修正のため）。ただし挙動変更は回帰テストで裏取りする。

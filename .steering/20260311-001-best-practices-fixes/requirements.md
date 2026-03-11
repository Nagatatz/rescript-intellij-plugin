# 要求定義: JetBrains プラグインベストプラクティス改善

## 背景

IntelliJ プラグインとして推奨プラクティスから外れている3つの問題を修正する。

## 修正内容

1. **AnAction サブクラスに `getActionUpdateThread()` を追加** — EDT ブロック回避
2. **リスナーリストをスレッドセーフに** — `CopyOnWriteArrayList` 使用
3. **`invokeLater` に `ModalityState` を明示** — 予期せぬ実行タイミング回避

## 受け入れ条件

- [ ] 9つの AnAction サブクラスに `getActionUpdateThread()` が追加されている
- [ ] `RescriptCompilationStatusService` のリスナーリストが `CopyOnWriteArrayList` になっている
- [ ] `invokeLater` 呼び出しに `ModalityState.any()` が指定されている
- [ ] 既存テストがすべてパスする
- [ ] generate アクションのテストに `getActionUpdateThread()` 検証が追加されている
- [ ] `./gradlew clean buildPlugin` が成功する

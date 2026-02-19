# Tasklist: Project Wizard + Code Generation

## Feature 1: Project Wizard

- [x] RescriptProjectGenerator.kt を実装
- [x] RescriptProjectGeneratorTest.kt を作成
- [x] RescriptModuleBuilder.kt を実装
- [x] RescriptModuleBuilderTest.kt を作成
- [x] RescriptProjectWizardStep.kt を実装（テスト省略: Swing UI）
- [x] plugin.xml に moduleBuilder を登録
- [x] ビルド確認
- [x] コミット: ✨ Add Project Wizard for new ReScript projects

## Feature 2: Code Generation

- [x] RescriptTypeDeclarationParser.kt を実装
- [x] RescriptTypeDeclarationParserTest.kt を作成
- [x] RescriptGenerateSwitchAction.kt を実装
- [x] RescriptGenerateSwitchActionTest.kt を作成
- [x] RescriptGenerateModuleTypeAction.kt を実装
- [x] RescriptGenerateModuleTypeActionTest.kt を作成
- [x] RescriptGenerateGroup.kt を実装（テスト省略: ActionGroup UI）
- [x] plugin.xml に GenerateGroup を登録
- [x] ビルド確認
- [x] コミット: ✨ Add Code Generation for switch arms and module types

## ドキュメント更新

- [x] CLAUDE.md プロジェクト構成図を更新
- [x] docs/product-requirements.md を更新
- [x] docs/functional-design.md を更新
- [x] コミット: 📝 Update docs for Project Wizard and Code Generation

## 最終確認

- [x] ./gradlew buildPlugin 成功
- [x] ./gradlew test 全テスト PASS

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

- [ ] RescriptTypeDeclarationParser.kt を実装
- [ ] RescriptTypeDeclarationParserTest.kt を作成
- [ ] RescriptGenerateSwitchAction.kt を実装
- [ ] RescriptGenerateSwitchActionTest.kt を作成
- [ ] RescriptGenerateModuleTypeAction.kt を実装
- [ ] RescriptGenerateModuleTypeActionTest.kt を作成
- [ ] RescriptGenerateGroup.kt を実装（テスト省略: ActionGroup UI）
- [ ] plugin.xml に GenerateGroup を登録
- [ ] ビルド確認
- [ ] コミット: ✨ Add Code Generation for switch arms and module types

## ドキュメント更新

- [ ] CLAUDE.md プロジェクト構成図を更新
- [ ] docs/product-requirements.md を更新
- [ ] docs/functional-design.md を更新
- [ ] コミット: 📝 Update docs for Project Wizard and Code Generation

## 最終確認

- [ ] ./gradlew buildPlugin 成功
- [ ] ./gradlew test 全テスト PASS

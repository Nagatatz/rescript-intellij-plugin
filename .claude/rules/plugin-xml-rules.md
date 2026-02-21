---
globs: ["**/plugin.xml", "**/rescript-*.xml"]
---

# Extension Point 登録ルール

- 新しい extension point の実装クラスは `plugin.xml` に登録すること
- オプション依存（JavaScript, Markdown, NodeJS 等）の extension point は `META-INF/rescript-*.xml` に分離し、`plugin.xml` の `<depends optional="true" config-file="...">` で参照すること
- 登録時は既存のエントリの並び順（アルファベット順またはカテゴリ順）に従うこと
- `<extensions defaultExtensionNs="com.intellij">` 内に配置すること

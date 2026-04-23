# Design — .gitignore ギャップ充実

## 実装

各テンプレートで `CommonFiles.gitignore(extra = listOf(...))` の引数に以下のパターンを追加する。

### Next.js

```kotlin
".gitignore" to CommonFiles.gitignore(extra = listOf(".next/", "out/", ".env*.local"))
```

### Cloudflare Workers

```kotlin
".gitignore" to CommonFiles.gitignore(extra = listOf(".wrangler/", "dist/", ".dev.vars"))
```

### AWS Lambda

```kotlin
".gitignore" to CommonFiles.gitignore(extra = listOf("dist/", "*.zip", ".aws-sam/"))
```

### React Native (CLI)

既存の `extra` がすでに充実しているが、ビルド成果物を追加:

```kotlin
".gitignore" to CommonFiles.gitignore(extra = listOf(
    "android/build/",
    "android/app/build/",
    "android/.gradle/",
    "android/local.properties",
    "ios/Pods/",
    "ios/build/",
    "ios/.xcode.env.local",
    "*.hbc",
    "*.keystore",
    "*.tsbuildinfo",
    "*.apk",
    "*.aab",
    "*.ipa",
))
```

## テスト戦略

各テンプレートテストに assertion を追加:

```kotlin
@Test
fun `gitignore includes NAME-specific patterns`() {
    val gi = XxxTemplateFiles.generate(ctx)[".gitignore"]!!
    assertTrue(gi.contains("<新パターン>"))
}
```

既存テストがすでに `.gitignore` の特定パターンを検証している箇所には追加アサーションを並べる。

## リスク

リスクほぼゼロ。追加的変更（subtractive ではない）であり、パターンは全て既存プロジェクトで標準的なもの。

# Design: Code Quality Quick Wins

## 1. 空の catch ブロックへのコメント追加

4 箇所とも同じパターン: 外部プロセスの stdin/stderr を別スレッドで処理しており、プロセスが先に終了した場合に `IOException` (Broken pipe / Stream closed) が発生する。これは正常動作であり、無視して問題ない。

```kotlin
// Before
} catch (_: IOException) {
}

// After
} catch (_: IOException) {
    // Expected when the process exits before stream I/O completes (broken pipe)
}
```

## 2. Regex パターンの集約

### 追加パターン

`RescriptRegexPatterns.kt` に以下を追加:

```kotlin
// ── variant constructor patterns ──────────────────────────
/** Matches a variant constructor with optional payload: `Name` or `Name(payload)`. */
@JvmField
val CONSTRUCTOR_WITH_PAYLOAD = Regex("""^([A-Z]\w*)(?:\((.+)\))?\s*$""")

// ── labeled parameter patterns ────────────────────────────
/** Extracts the name from a labeled parameter: `~name`. */
@JvmField
val LABELED_PARAM_NAME = Regex("""~(\w+)""")

// ── include statement patterns ────────────────────────────
/** Captures the module name from `include ModuleName` statements. */
@JvmField
val INCLUDE_MODULE_CAPTURE = Regex("""^include\s+([A-Z][\w.]*)""")
```

### 使用箇所の変更

| ファイル | 変更前 | 変更後 |
|---------|--------|--------|
| `RescriptTypeDeclarationParser.kt` | `private val CONSTRUCTOR_PATTERN` | `RescriptRegexPatterns.CONSTRUCTOR_WITH_PAYLOAD` |
| `RescriptLspSignatureParser.kt` | `private val CONSTRUCTOR_PATTERN` | `RescriptRegexPatterns.CONSTRUCTOR_WITH_PAYLOAD` |
| `RescriptGenerateDocCommentIntention.kt` | `private val LABELED_PARAM_REGEX` | `RescriptRegexPatterns.LABELED_PARAM_NAME` |
| `RescriptDependencyDiagramProvider.kt` | `private val INCLUDE_PATTERN` | `RescriptRegexPatterns.INCLUDE_MODULE_CAPTURE` |

**注意:** `RescriptLspSignatureParser.kt` の CONSTRUCTOR_PATTERN は `$` で終わるが、`RescriptTypeDeclarationParser.kt` は `\s*$` で終わる。統一パターンは `\s*$` を採用（末尾空白の許容は安全側）。

## テスト戦略

- ロジック変更なし（パターン参照先の変更のみ）
- `RescriptRegexPatterns` の既存テストに新パターンの基本テストを追加
- 既存テストの通過で動作保証

# 設計: コード品質改善

## 変更対象ファイル

### テスト追加（ブランチカバレッジ改善）

| ファイル | 変更内容 |
|---------|---------|
| `src/test/.../util/RescriptProcessUtilsTest.kt` | タイムアウト・InterruptedException テスト追加 |
| `src/test/.../util/RescriptSecurityUtilsTest.kt` | `isWithinProject()` null ケーステスト追加 |
| `src/test/.../util/RescriptFileUtilTest.kt` | 未カバー分岐のテスト追加 |

### エラーハンドリング改善

| ファイル | 変更内容 |
|---------|---------|
| `src/main/java/.../codevision/RescriptCodeVisionProvider.java` | catch ブロックにログ追加 |
| `src/main/kotlin/.../binding/DtsParserProcess.kt` | `extractScript()` に `synchronized` 追加 |
| `src/main/kotlin/.../dependencies/RescriptDependenciesPanel.kt` | 例外型の具体化 |
| `src/main/kotlin/.../lsp/RescriptLspUtils.kt` | トレースログ追加 |

## 設計詳細

### ブランチカバレッジ

#### RescriptProcessUtils テスト
- タイムアウトテスト: `timeoutSeconds = 1` で長時間コマンド実行 → タイムアウト結果を検証
- readLine null テスト: 空出力コマンドで null ハンドリングを検証

#### RescriptSecurityUtils テスト
- `isWithinProject()` に project.guessProjectDir() が null を返すケースをテスト

#### RescriptFileUtil テスト
- 未カバー分岐をカバレッジレポートから特定し、テスト追加

### エラーハンドリング

#### RescriptCodeVisionProvider.java
```java
// Before
catch (Exception e) { return Collections.emptyList(); }

// After
catch (Exception e) {
    LOG.debug("Failed to retrieve code lens", e);
    return Collections.emptyList();
}
```

#### DtsParserProcess.kt
```kotlin
// Before: @Volatile + no sync
fun extractScript(): Path {
    cachedScriptPath?.let { if (Files.isRegularFile(it)) return it }
    val tempFile = ...
    cachedScriptPath = tempFile
    return tempFile
}

// After: synchronized block
fun extractScript(): Path = synchronized(this) {
    cachedScriptPath?.let { if (Files.isRegularFile(it)) return it }
    val tempFile = ...
    cachedScriptPath = tempFile
    tempFile
}
```

#### RescriptDependenciesPanel.kt
```kotlin
// Before
catch (_: Exception) { ... }

// After
catch (e: IOException) { LOG.debug("Failed to read rescript.json", e); ... }
catch (e: com.google.gson.JsonParseException) { LOG.debug("Invalid rescript.json", e); ... }
```

#### RescriptLspUtils.kt
```kotlin
// Before
catch (_: Exception) { ... }

// After
catch (e: Exception) { LOG.trace("URI parsing failed", e); ... }
```

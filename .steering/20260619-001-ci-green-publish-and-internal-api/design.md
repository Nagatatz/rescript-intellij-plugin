# 設計

## 1. 内部 API 依存の解消

### 問題

`HierarchyNodeDescriptor` は `SmartElementDescriptor` を継承し、`getPsiElement()` を継承する。この
メソッドが 2026.2 EAP で `@ApiStatus.Internal` になった。Kotlin では `.psiElement` / `psiElement`
としてアクセスされており、`hierarchy/` 内の以下 8 箇所で使用:

| ファイル | 箇所 |
|---------|------|
| `RescriptModuleHierarchyNodeDescriptor.kt` | `update()` 内 `psiElement` |
| `RescriptCallHierarchyNodeDescriptor.kt` | `update()` 内 `psiElement` |
| `RescriptModuleHierarchyTreeStructure.kt` | `RescriptModuleHierarchyTreeStructure.buildChildren` / `RescriptModuleDependencyTreeStructure.buildChildren` (2 箇所) |
| `RescriptCalleeTreeStructure.kt` | `buildChildren` |
| `RescriptCallerTreeStructure.kt` | `buildChildren` |
| `RescriptModuleHierarchyBrowser.kt` | `getElementFromDescriptor` |
| `RescriptCallHierarchyBrowser.kt` | `getElementFromDescriptor` |

### 方針

これらの descriptor はすべて自前のサブクラス(`RescriptModuleHierarchyNodeDescriptor` /
`RescriptCallHierarchyNodeDescriptor`)であり、コンストラクタで `element: PsiElement` を受け取る。
そこで各サブクラスに `SmartPsiElementPointer` を保持し、公開アクセサ `rescriptElement: PsiElement?`
を追加する。内部メソッド `getPsiElement()` を一切呼ばずに同等(無効時 null)の挙動を得る。

```kotlin
private val elementPointer: SmartPsiElementPointer<PsiElement> =
    SmartPointerManager.getInstance(project).createSmartPsiElementPointer(element)

/** Non-internal replacement for SmartElementDescriptor.getPsiElement(). */
val rescriptElement: PsiElement?
    get() = elementPointer.element
```

- `update()` 内: `psiElement` → `rescriptElement`
- tree structure / browser 内: `descriptor.psiElement` → `(descriptor as? RescriptXxxNodeDescriptor)?.rescriptElement`
  - tree structure / browser は自前の descriptor のみを生成・受領するためキャストは安全。非該当型なら null/emptyArray にフォールバック。

### ignored-problems の整理

使用が消えるため `plugin-verifier-ignored-problems.txt` の `SmartElementDescriptor.getPsiElement`
エントリ(コメント含む)を削除。他のエントリ(CodeVisionPlaceholderCollector / FloatingToolbarProvider /
FileIncludeProvider)は変更しない。

## 2. ドキュメント乖離

- `CLAUDE.md`: CI/CD のワークフロー表に CodeQL / Integration Tests / OS Matrix を追加(計 7)。
- `docs/repository-structure.md`: `.github/workflows/` のツリーに 3 ファイルを追加。

## 3. publish に CI グリーン必須化

Release workflow に新ジョブ `require-ci-green` を追加し、`publish` の `needs` に加える。

```yaml
require-ci-green:
  runs-on: ubuntu-latest
  timeout-minutes: 30
  permissions:
    contents: read
    actions: read
  steps:
    - name: Wait for CI workflow to succeed for this commit
      env:
        GH_TOKEN: ${{ github.token }}
        SHA: ${{ github.sha }}
        REPO: ${{ github.repository }}
      run: |  # gh api で ci.yml の head_sha=SHA な push 実行を polling、completed まで待ち success 以外で fail
```

- タグとコミットは同時 push されるため、CI は Release と並走する。完了までポーリング(最大 ~25 分)。
- `publish: needs: [validate-and-build, require-ci-green]`
- GitHub Release 作成は `validate-and-build` のままで、Marketplace publish のみをゲートする(要求どおり)。

## テスト方針

- `RescriptModuleHierarchyNodeDescriptorTest` の `.psiElement` 参照を `rescriptElement` に更新し、新アクセサが
  非 null を返すことを検証(既存の heavy/light fixture テストを流用)。
- 既存の tree structure / browser テストは挙動不変のためグリーンであることを確認(必要なら `.psiElement` 参照を更新)。
- workflow YAML は actionlint(CI の actionlint ジョブ)で検証。
- ローカル Java 不在のため、最終検証は push 後の CI(`verifyPlugin` 含む)。

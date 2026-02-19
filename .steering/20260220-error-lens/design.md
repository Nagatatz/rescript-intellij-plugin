# Design: Error Lens

## Architecture

### Approach: MarkupModelListener + InlayModel

Listen for `RangeHighlighter` additions/removals on the editor's `MarkupModel`. When a diagnostic highlighter is added, create an `afterLineEndElement` inlay to display the message.

### Components

1. **RescriptErrorLensSeverity** - Maps HighlightSeverity to display colors
2. **RescriptErrorLensHighlighterInfo** - Extracts diagnostic info from RangeHighlighter
3. **RescriptErrorLensRenderer** - EditorCustomElementRenderer for inlay rendering
4. **RescriptErrorLensManager** - Per-editor manager implementing MarkupModelListener
5. **RescriptErrorLensEditorListener** - EditorFactoryListener for editor lifecycle

### Data Flow

```
LSP diagnostics -> MarkupModel (RangeHighlighters) -> MarkupModelListener
  -> Extract HighlightInfo -> Create Inlay with Renderer
```

### Settings Integration

- `RescriptProjectSettings.State` gains `errorLensEnabled` and `errorLensMinSeverity`
- `RescriptConfigurable` gains Error Lens section with checkbox + combo box

### Registration

- `EditorFactoryListener` registered via `<editorFactoryListener>` extension point in plugin.xml

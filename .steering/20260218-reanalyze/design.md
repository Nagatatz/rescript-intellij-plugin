# Design: reanalyze Integration

## New Files
- `src/main/kotlin/com/rescript/plugin/analysis/RescriptReanalyzeAnnotator.kt`

## Changed Files
- `src/main/resources/META-INF/plugin.xml`

## Architecture
- `ExternalAnnotator<CollectedInfo, AnnotationResult>` with 3-phase model
- Tool detection following `RescriptCliDetector` pattern
- JSON parsing with `com.google.gson.JsonParser`

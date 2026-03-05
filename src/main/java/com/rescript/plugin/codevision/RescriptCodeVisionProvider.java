package com.rescript.plugin.codevision;

import com.intellij.codeInsight.codeVision.CodeVisionAnchorKind;
import com.intellij.codeInsight.codeVision.CodeVisionEntry;
import com.intellij.codeInsight.codeVision.CodeVisionRelativeOrdering;
import com.intellij.codeInsight.codeVision.ui.model.TextCodeVisionEntry;
import com.intellij.codeInsight.hints.codeVision.DaemonBoundCodeVisionProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.platform.lsp.api.LspServer;
import com.intellij.platform.lsp.api.LspServerState;
import com.intellij.psi.PsiFile;
import com.rescript.plugin.lsp.RescriptLspUtils;
import com.rescript.plugin.util.RescriptFileUtil;
import kotlin.Pair;
import kotlin.jvm.functions.Function1;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageServer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Displays inferred type signatures above function definitions using
 * the IntelliJ CodeVision API, backed by the LSP {@code textDocument/codeLens} request.
 *
 * <p>The rescript-language-server returns type annotations as Code Lens entries
 * with {@code command.title} containing the type string and {@code command.command} empty
 * (display-only, no click action).</p>
 *
 * <p>Code Lenses are not shown for {@code .resi} files since interface files contain
 * explicit type declarations.</p>
 *
 * <p>This class is intentionally written in Java rather than Kotlin to avoid the
 * Kotlin compiler generating bridge methods for {@code getPlaceholderCollector()},
 * which references the internal API type {@code CodeVisionPlaceholderCollector}.
 * In Java, unoverridden default interface methods do not produce bytecode in the
 * implementing class, so the plugin verifier does not flag the internal API usage.</p>
 *
 * @see DaemonBoundCodeVisionProvider
 */
public class RescriptCodeVisionProvider implements DaemonBoundCodeVisionProvider {
    public static final String ID = "rescript.codeLens";

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @NotNull
    @Override
    public String getName() {
        return "ReScript Type Annotations";
    }

    @NotNull
    @Override
    public CodeVisionAnchorKind getDefaultAnchor() {
        return CodeVisionAnchorKind.Top;
    }

    @NotNull
    @Override
    public List<CodeVisionRelativeOrdering> getRelativeOrderings() {
        return Collections.emptyList();
    }

    // getPlaceholderCollector() is intentionally NOT overridden.
    // The default implementation returns null, which is correct for our use case.
    // In Java, unoverridden default methods do not appear in the class bytecode,
    // avoiding the reference to the @Internal CodeVisionPlaceholderCollector type.

    @NotNull
    @Override
    public List<Pair<TextRange, CodeVisionEntry>> computeForEditor(
            @NotNull Editor editor,
            @NotNull PsiFile file
    ) {
        var virtualFile = file.getVirtualFile();
        if (virtualFile == null) return Collections.emptyList();
        if (RescriptFileUtil.INSTANCE.isResiFile(virtualFile)) return Collections.emptyList();

        var project = file.getProject();
        var document = editor.getDocument();

        LspServer server = RescriptLspUtils.INSTANCE.getServer(project);
        if (server == null || server.getState() != LspServerState.Running) {
            return Collections.emptyList();
        }

        var fileUri = server.getDescriptor().getFileUri(virtualFile);
        var params = new CodeLensParams(new TextDocumentIdentifier(fileUri));

        List<CodeLens> codeLenses;
        try {
            @SuppressWarnings("unchecked")
            Function1<LanguageServer, java.util.concurrent.CompletableFuture<List<CodeLens>>> request =
                    languageServer -> (java.util.concurrent.CompletableFuture<List<CodeLens>>) (java.util.concurrent.CompletableFuture<?>)
                            languageServer.getTextDocumentService().codeLens(params);
            codeLenses = server.sendRequestSync(0, request);
        } catch (Exception e) {
            return Collections.emptyList();
        }

        if (codeLenses == null) return Collections.emptyList();

        var results = new ArrayList<Pair<TextRange, CodeVisionEntry>>();
        for (CodeLens codeLens : codeLenses) {
            Range range = codeLens.getRange();
            if (range == null) continue;

            Command command = codeLens.getCommand();
            if (command == null) continue;
            String title = command.getTitle();
            if (title == null) continue;

            int startLine = range.getStart().getLine();
            if (startLine < 0 || startLine >= document.getLineCount()) continue;

            int lineStartOffset = document.getLineStartOffset(startLine);
            int lineEndOffset = document.getLineEndOffset(startLine);
            var textRange = new TextRange(lineStartOffset, lineEndOffset);

            var entry = new TextCodeVisionEntry(
                    title,
                    ID,
                    null,
                    title,
                    title,
                    Collections.emptyList()
            );

            results.add(new Pair<>(textRange, entry));
        }

        return results;
    }
}

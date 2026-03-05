package com.rescript.plugin

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.Consumer
import java.awt.Component
import java.net.URLEncoder

/**
 * Error report submitter that opens a pre-filled GitHub issue in the user's browser.
 *
 * When an unhandled exception occurs in the plugin, this handler generates a
 * GitHub Issues URL containing the stack trace and environment information,
 * then opens it in the default browser for the user to review and submit.
 *
 * Registered via the `com.intellij.errorHandler` extension point.
 *
 * @see ErrorReportSubmitter
 */
class RescriptErrorReporter : ErrorReportSubmitter() {
    override fun getReportActionText(): String = "Report to ReScript Plugin GitHub"

    override fun submit(
        events: Array<out IdeaLoggingEvent>,
        additionalInfo: String?,
        parentComponent: Component,
        consumer: Consumer<in SubmittedReportInfo>,
    ): Boolean {
        val event = events.firstOrNull()
        val title = buildTitle(event)
        val body = buildBody(event, additionalInfo)

        val url = buildGitHubIssueUrl(title, body)
        BrowserUtil.browse(url)

        consumer.consume(
            SubmittedReportInfo(
                url,
                "GitHub Issue",
                SubmittedReportInfo.SubmissionStatus.NEW_ISSUE,
            ),
        )
        return true
    }

    companion object {
        private val LOG = logger<RescriptErrorReporter>()
        private const val GITHUB_REPO = "ngtz/rescript-intellij-plugin"

        // Maximum URL length to avoid browser/server limits
        private const val MAX_URL_LENGTH = 8000

        // Maximum stack trace length in the body
        private const val MAX_STACKTRACE_LENGTH = 4000

        /**
         * Builds a concise issue title from the exception event.
         *
         * @param event the logging event, or null if unavailable
         * @return a title string for the GitHub issue
         */
        internal fun buildTitle(event: IdeaLoggingEvent?): String {
            if (event == null) return "Unhandled exception in ReScript plugin"
            val throwable = event.throwable
            val message = event.message
            return when {
                !message.isNullOrBlank() -> "Exception: $message"
                throwable != null -> "Exception: ${throwable.javaClass.simpleName}: ${throwable.message ?: ""}"
                else -> "Unhandled exception in ReScript plugin"
            }.take(200)
        }

        /**
         * Builds the issue body with environment info and stack trace.
         *
         * @param event the logging event containing the exception
         * @param additionalInfo optional user-provided context
         * @return the formatted issue body
         */
        internal fun buildBody(
            event: IdeaLoggingEvent?,
            additionalInfo: String?,
        ): String {
            val sb = StringBuilder()

            sb.appendLine("## Environment")
            sb.appendLine("- **Plugin version:** ${pluginVersion()}")
            sb.appendLine("- **IDE:** ${ideVersion()}")
            sb.appendLine("- **OS:** ${System.getProperty("os.name")} ${System.getProperty("os.version")}")
            sb.appendLine("- **JDK:** ${System.getProperty("java.version")}")
            sb.appendLine()

            if (!additionalInfo.isNullOrBlank()) {
                sb.appendLine("## Additional Information")
                sb.appendLine(additionalInfo)
                sb.appendLine()
            }

            if (event != null) {
                sb.appendLine("## Stack Trace")
                sb.appendLine("```")
                val rawStacktrace = event.throwableText ?: event.throwable?.stackTraceToString() ?: "N/A"
                // Strip absolute file paths to avoid exposing user directory structure
                val stacktrace = sanitizeFilePaths(rawStacktrace)
                sb.appendLine(stacktrace.take(MAX_STACKTRACE_LENGTH))
                if (stacktrace.length > MAX_STACKTRACE_LENGTH) {
                    sb.appendLine("... (truncated)")
                }
                sb.appendLine("```")
            }

            return sb.toString()
        }

        /**
         * Constructs a GitHub new-issue URL with pre-filled title and body.
         *
         * @param title the issue title
         * @param body the issue body in Markdown
         * @return the full GitHub issue URL
         */
        internal fun buildGitHubIssueUrl(
            title: String,
            body: String,
        ): String {
            val encodedTitle = urlEncode(title)
            val baseUrl = "https://github.com/$GITHUB_REPO/issues/new?labels=bug&title=$encodedTitle&body="

            // Truncate body if URL would be too long
            val availableLength = MAX_URL_LENGTH - baseUrl.length
            val encodedBody =
                if (availableLength > 0) {
                    val truncatedBody =
                        if (urlEncode(body).length > availableLength) {
                            // Re-encode progressively shorter bodies until it fits
                            val truncated = body.take(availableLength / 3)
                            urlEncode("$truncated\n\n... (truncated due to URL length limit)")
                        } else {
                            urlEncode(body)
                        }
                    truncatedBody
                } else {
                    ""
                }

            return "$baseUrl$encodedBody"
        }

        // Matches absolute paths like /Users/foo/bar/project/ or C:\Users\foo\project\
        private val ABSOLUTE_PATH_REGEX = Regex("""/[^\s:]+/|[A-Z]:\\[^\s:]+\\""")

        /**
         * Strips absolute file paths from stack trace text, replacing directory prefixes
         * with a generic placeholder to avoid leaking user filesystem structure.
         *
         * @param text the raw stack trace text
         * @return sanitized text with paths redacted
         */
        internal fun sanitizeFilePaths(text: String): String =
            ABSOLUTE_PATH_REGEX.replace(text) { match ->
                val path = match.value
                // Keep just the last two path segments for context
                val segments = path.replace('\\', '/').trimEnd('/').split('/')
                if (segments.size > 2) {
                    "<...>/${segments.takeLast(2).joinToString("/")}/"
                } else {
                    path
                }
            }

        private fun urlEncode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8)

        private fun pluginVersion(): String =
            try {
                val pluginManager =
                    com.intellij.ide.plugins.PluginManagerCore.getPlugin(
                        com.intellij.openapi.extensions.PluginId
                            .getId("com.rescript.plugin"),
                    )
                pluginManager?.version ?: "unknown"
            } catch (e: Exception) {
                LOG.trace(e)
                "unknown"
            }

        private fun ideVersion(): String =
            try {
                val appInfo =
                    com.intellij.openapi.application.ApplicationInfo
                        .getInstance()
                "${appInfo.fullApplicationName} (${appInfo.build.asString()})"
            } catch (e: Exception) {
                LOG.trace(e)
                "unknown"
            }
    }
}

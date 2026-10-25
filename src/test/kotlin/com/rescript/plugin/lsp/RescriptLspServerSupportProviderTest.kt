package com.rescript.plugin.lsp

import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspClientDescriptor
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.intellij.platform.lsp.api.ProjectWideLspClientDescriptor
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Regression tests for the migration from the deprecated `LspServerSupportProvider` /
 * `ProjectWideLspServerDescriptor` API to `LspIntegrationProvider` /
 * `ProjectWideLspClientDescriptor`.
 *
 * The deprecated types are referenced by name only, so this test does not itself add
 * deprecated-API usages.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptLspServerSupportProviderTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    /** Records every descriptor the provider asks the platform to start. */
    private class RecordingStarter : LspIntegrationProvider.LspClientStarter {
        val started = mutableListOf<LspClientDescriptor>()

        override fun ensureClientStarted(descriptor: LspClientDescriptor) {
            started += descriptor
        }
    }

    @Test
    fun testProviderIsRegisteredUnderIntegrationProviderExtensionPoint() {
        val registered =
            LspIntegrationProvider.EP_NAME.extensionList.filterIsInstance<RescriptLspServerSupportProvider>()

        assertEquals(1, registered.size, "provider must be registered exactly once")
    }

    @Test
    fun testProviderDoesNotExtendDeprecatedSupertype() {
        val deprecated = Class.forName("com.intellij.platform.lsp.api.LspServerSupportProvider")

        assertFalse(deprecated.isAssignableFrom(RescriptLspServerSupportProvider::class.java))
    }

    @Test
    fun testPluginXmlDoesNotUseDeprecatedExtensionPoint() {
        val xml =
            javaClass.classLoader
                .getResourceAsStream("META-INF/plugin.xml")!!
                .bufferedReader()
                .use { it.readText() }

        assertFalse(xml.contains("platform.lsp.serverSupportProvider"))
        assertTrue(xml.contains("<platform.lsp.integrationProvider"))
    }

    @Test
    fun testFileOpenedStartsClientForRescriptSources() {
        val starter = RecordingStarter()
        val provider = RescriptLspServerSupportProvider()

        provider.fileOpened(project, myFixture.addFileToProject("Impl.res", "let x = 1").virtualFile, starter)
        provider.fileOpened(project, myFixture.addFileToProject("Impl.resi", "let x: int").virtualFile, starter)

        assertEquals(2, starter.started.size)
        assertTrue(starter.started.all { it is RescriptLspServerDescriptor })
    }

    @Test
    fun testFileOpenedIgnoresOtherFiles() {
        val starter = RecordingStarter()

        RescriptLspServerSupportProvider().fileOpened(
            project,
            myFixture.addFileToProject("notes.txt", "hello").virtualFile,
            starter,
        )

        assertTrue(starter.started.isEmpty())
    }

    @Test
    fun testDescriptorExtendsClientDescriptorAndKeepsItsContract() {
        val descriptor = RescriptLspServerDescriptor(project)
        val deprecated = Class.forName("com.intellij.platform.lsp.api.LspServerDescriptor")

        assertTrue(ProjectWideLspClientDescriptor::class.java.isInstance(descriptor))
        assertFalse(deprecated.isInstance(descriptor))
        assertEquals(RescriptLanguageServer::class.java, descriptor.lsp4jServerClass)
        assertTrue(descriptor.isSupportedFile(myFixture.addFileToProject("A.res", "").virtualFile))
        assertFalse(descriptor.isSupportedFile(myFixture.addFileToProject("A.js", "").virtualFile))
        assertNotNull(descriptor.lspCustomization.semanticTokensCustomizer)
    }

    @Test
    fun testClientLookupResolvesThroughClientManager() {
        // The light project is shared, so an earlier test may already have opened a
        // ReScript file and made the platform create a client. Either way the lookup
        // must resolve through LspClientManager and only ever yield our descriptor.
        val clients = LspClientManager.getInstance(project).getClients(RescriptLspServerSupportProvider::class.java)

        assertTrue(clients.all { it.descriptor is RescriptLspServerDescriptor })
        assertEquals(clients.firstOrNull(), RescriptLspUtils.getServer(project))
    }
}

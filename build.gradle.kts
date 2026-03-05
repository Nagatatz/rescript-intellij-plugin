import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java") // needed for JFlex-generated Java lexer
    id("org.jetbrains.kotlin.jvm") version "2.3.10"
    id("org.jetbrains.intellij.platform") version "2.11.0"
    id("org.jetbrains.grammarkit") version "2023.3.0.2"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
    id("org.jetbrains.kotlinx.kover") version "0.9.7"
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        bundledModule("intellij.spellchecker")
        bundledPlugin("com.intellij.modules.json")
        bundledPlugin("org.intellij.plugins.markdown")
        bundledPlugin("com.intellij.modules.vcs")
        bundledPlugin("tanvd.grazi")
        pluginVerifier()
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get() +
    "-" + providers.gradleProperty("platformVersion").get()

intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion").get() +
            "-" + providers.gradleProperty("platformVersion").get()
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }
    buildSearchableOptions = false
    pluginVerification {
        ides {
            recommended()
        }
        // CodeVisionPlaceholderCollector is listed as an exception ("Made public in 2024.2")
        // on https://plugins.jetbrains.com/docs/intellij/api-internal.html
        freeArgs.addAll(
            "-ignored-problems",
            layout.projectDirectory
                .file("plugin-verifier-ignored-problems.txt")
                .asFile.absolutePath,
        )
    }
    publishing {
        token =
            providers
                .environmentVariable("JETBRAINS_MARKETPLACE_TOKEN")
                .orElse(providers.gradleProperty("jetbrainsMarketplaceToken"))
    }
    signing {
        certificateChain =
            providers
                .environmentVariable("CERTIFICATE_CHAIN")
                .orElse(providers.gradleProperty("certificateChain"))
        privateKey =
            providers
                .environmentVariable("PRIVATE_KEY")
                .orElse(providers.gradleProperty("privateKey"))
        password =
            providers
                .environmentVariable("PRIVATE_KEY_PASSWORD")
                .orElse(providers.gradleProperty("privateKeyPassword"))
    }
}

ktlint {
    version.set("1.6.0")
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    filter {
        exclude("**/generated/**")
    }
}

kover {
    reports {
        total {
            xml {
                onCheck = false
            }
            html {
                onCheck = false
            }
        }
        filters {
            excludes {
                classes(
                    // Auto-generated lexer
                    "com.rescript.plugin.lang.RescriptFlexLexer",
                    // Pure type definitions (no logic)
                    "com.rescript.plugin.RescriptFileTypes",
                    "com.rescript.plugin.RescriptIcons",
                    "com.rescript.plugin.RescriptLanguage",
                    // IDE lifecycle (StartupActivity)
                    "com.rescript.plugin.lsp.RescriptLspStartupActivity",
                    "com.rescript.plugin.analysis.RescriptReanalyzeServerStartupActivity",
                    "com.rescript.plugin.run.RescriptBuildWatchStartupActivity",
                    // LSP server coupling
                    "com.rescript.plugin.lsp.RescriptLanguageServer",
                    "com.rescript.plugin.lsp.RescriptLsp4jClient",
                    "com.rescript.plugin.lsp.RescriptLspServerDescriptor",
                    "com.rescript.plugin.lsp.RescriptLspServerSupportProvider",
                    "com.rescript.plugin.lsp.RescriptLspDiagnosticParser",
                    "com.rescript.plugin.lsp.RescriptLspSignatureParser",
                    "com.rescript.plugin.refactor.RescriptRenameHandler",
                    // Settings UI (Configurable / CodeStyleSettingsProvider)
                    "com.rescript.plugin.settings.RescriptConfigurable",
                    "com.rescript.plugin.codestyle.RescriptCodeStyleSettingsProvider",
                    // Formatting service (IDE integration)
                    "com.rescript.plugin.formatter.RescriptFormattingService",
                    "com.rescript.plugin.highlight.RescriptSyntaxHighlighterFactory",
                    "com.rescript.plugin.config.RescriptJsonSchemaProviderFactory",
                    "com.rescript.plugin.editor.RescriptEditorNotificationProvider",
                    "com.rescript.plugin.completion.RescriptAutoImportOptionsProvider",
                    "com.rescript.plugin.worksheet.RescriptWorksheetFileType",
                    "com.rescript.plugin.config.RescriptFrameworkType",
                    // Actions requiring IDE context
                    "com.rescript.plugin.lsp.RescriptDumpLspStateAction",
                    "com.rescript.plugin.navigation.RescriptSwitchFileAction",
                    "com.rescript.plugin.debug.RescriptDebugCompiledJsAction",
                    "com.rescript.plugin.binding.DtsGenerateBindingAction",
                )
                // Run configuration UI classes
                packages(
                    "com.rescript.plugin.wizard.templates",
                )
                classes(
                    // Run configurations and settings editors
                    "com.rescript.plugin.run.RescriptRunConfiguration",
                    "com.rescript.plugin.run.RescriptRunConfigurationOptions",
                    "com.rescript.plugin.run.RescriptRunSettingsEditor",
                    "com.rescript.plugin.test.RescriptTestRunConfiguration",
                    "com.rescript.plugin.test.RescriptTestRunConfigurationOptions",
                    "com.rescript.plugin.test.RescriptTestRunSettingsEditor",
                    "com.rescript.plugin.debug.RescriptDebugRunConfiguration",
                    "com.rescript.plugin.debug.RescriptDebugRunConfigurationOptions",
                    "com.rescript.plugin.debug.RescriptDebugSettingsEditor",
                    // Wizard UI
                    "com.rescript.plugin.wizard.RescriptProjectWizardStep",
                    // Tool window panels (Swing UI)
                    "com.rescript.plugin.preview.RescriptCompiledJsPreviewPanel",
                    "com.rescript.plugin.preview.RescriptCompiledJsPreviewToolWindowFactory",
                    "com.rescript.plugin.dependencies.RescriptDependenciesPanel",
                    "com.rescript.plugin.dependencies.RescriptDependenciesToolWindowFactory",
                    "com.rescript.plugin.typeinfo.RescriptTypeInfoPanel",
                    "com.rescript.plugin.typeinfo.RescriptTypeInfoToolWindowFactory",
                    "com.rescript.plugin.ppx.RescriptPpxViewPanel",
                    "com.rescript.plugin.ppx.RescriptPpxViewToolWindowFactory",
                    "com.rescript.plugin.repl.RescriptReplPanel",
                    "com.rescript.plugin.repl.RescriptReplToolWindowFactory",
                )
            }
        }
        verify {
            rule {
                minBound(54)
            }
        }
    }
}

val generateRescriptLexer =
    tasks.register<GenerateLexerTask>("generateRescriptLexer") {
        sourceFile.set(file("src/main/java/com/rescript/plugin/lang/Rescript.flex"))
        targetOutputDir.set(file("src/main/java/com/rescript/plugin/lang"))
    }

tasks {
    compileJava {
        dependsOn(generateRescriptLexer)
    }
    compileKotlin {
        dependsOn(generateRescriptLexer)
    }
    named("runKtlintCheckOverMainSourceSet") {
        mustRunAfter(generateRescriptLexer)
    }
    runIde {
        systemProperty("idea.is.internal", true)
        // Disable bundled Ultimate plugins that cause errors in the sandbox
        systemProperty("idea.required.plugins.id", "com.intellij.java")
        jvmArgs("-Xmx2G")
        autoReload = true
    }
}

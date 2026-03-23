import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java") // needed for JFlex-generated Java lexer
    id("org.jetbrains.kotlin.jvm") version "2.3.10"
    id("org.jetbrains.intellij.platform") version "2.12.0"
    id("org.jetbrains.grammarkit") version "2023.3.0.3"
    id("org.jlleitschuh.gradle.ktlint") version "14.1.0"
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
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:2.0.3")
    // JUnit 3/4 TestCase is needed at compile time because IntelliJ's
    // BasePlatformTestCase extends UsefulTestCase which extends TestCase
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion").get()
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }
    buildSearchableOptions = false
    pluginVerification {
        ides {
            recommended()
        }
        // Suppresses known false-positive verifier warnings. See the file for
        // per-entry rationale and review dates.
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
                // ── Packages with 0% coverage (IDE-coupled, no unit-testable logic) ──
                packages(
                    "com.rescript.plugin.analysis",
                    "com.rescript.plugin.binding",
                    "com.rescript.plugin.breadcrumb",
                    "com.rescript.plugin.commenter",
                    "com.rescript.plugin.completion",
                    "com.rescript.plugin.debug",
                    "com.rescript.plugin.dependencies",
                    "com.rescript.plugin.diagram",
                    "com.rescript.plugin.editor",
                    "com.rescript.plugin.errorlens",
                    "com.rescript.plugin.formatter",
                    "com.rescript.plugin.grazie",
                    "com.rescript.plugin.hierarchy",
                    "com.rescript.plugin.hierarchy.call",
                    "com.rescript.plugin.imports",
                    "com.rescript.plugin.injection",
                    "com.rescript.plugin.inspection",
                    "com.rescript.plugin.intention",
                    "com.rescript.plugin.navbar",
                    "com.rescript.plugin.paste",
                    "com.rescript.plugin.ppx",
                    "com.rescript.plugin.preview",
                    "com.rescript.plugin.projectview",
                    "com.rescript.plugin.quickfix",
                    "com.rescript.plugin.refactor",
                    "com.rescript.plugin.repl",
                    "com.rescript.plugin.scratch",
                    "com.rescript.plugin.spellcheck",
                    "com.rescript.plugin.statusbar",
                    "com.rescript.plugin.surround",
                    "com.rescript.plugin.template",
                    "com.rescript.plugin.test",
                    "com.rescript.plugin.typeinfo",
                    "com.rescript.plugin.wizard",
                    "com.rescript.plugin.wizard.templates",
                    "com.rescript.plugin.worksheet",
                    "com.rescript.plugin.documentation",
                )
                // ── Individual class exclusions (IDE-coupled classes in covered packages) ──
                // Wildcard (*) suffix matches inner/companion classes ($Companion, $install$1, etc.)
                classes(
                    // Auto-generated lexer
                    "com.rescript.plugin.lang.RescriptFlexLexer",
                    // Pure type definitions (no logic)
                    "com.rescript.plugin.RescriptFileTypes",
                    "com.rescript.plugin.RescriptIcons",
                    "com.rescript.plugin.RescriptLanguage",
                    // Root package classes with 0% coverage
                    "com.rescript.plugin.RescriptErrorReporter*",
                    "com.rescript.plugin.RescriptReaderModeMatcher*",
                    // Parsers (PsiBuilder coupling)
                    "com.rescript.plugin.lang.RescriptDeclarationParser*",
                    "com.rescript.plugin.lang.RescriptJsxParser*",
                    // Find usages / usage type (IDE lifecycle)
                    "com.rescript.plugin.lang.RescriptFindUsagesProvider*",
                    "com.rescript.plugin.lang.RescriptUsageTypeProvider*",
                    "com.rescript.plugin.lang.RescriptElementDescriptionProvider*",
                    "com.rescript.plugin.lang.RescriptParserDefinition*",
                    // PSI elements requiring IDE lifecycle
                    "com.rescript.plugin.lang.psi.RescriptDeclarationPsiElement*",
                    "com.rescript.plugin.lang.psi.RescriptDeclarationElementType*",
                    "com.rescript.plugin.lang.psi.RescriptDeclarationStub*",
                    "com.rescript.plugin.lang.psi.RescriptFileStub*",
                    "com.rescript.plugin.lang.psi.RescriptFile",
                    "com.rescript.plugin.lang.psi.RescriptPsiUtils*",
                    // All navigation classes (IDE-coupled)
                    "com.rescript.plugin.navigation.*",
                    // All LSP classes (server coupling)
                    "com.rescript.plugin.lsp.*",
                    // All run configuration classes (IDE-coupled)
                    "com.rescript.plugin.run.*",
                    // All settings classes
                    "com.rescript.plugin.settings.*",
                    // All codestyle classes (IDE-coupled)
                    "com.rescript.plugin.codestyle.*",
                    // All generate action classes (IDE dialog coupling)
                    "com.rescript.plugin.generate.*",
                    // All highlight classes (IDE-coupled)
                    "com.rescript.plugin.highlight.*",
                    // All config classes (IDE-coupled)
                    "com.rescript.plugin.config.*",
                    // All structure view classes
                    "com.rescript.plugin.structure.*",
                    // Folding (IDE lifecycle)
                    "com.rescript.plugin.folding.*",
                    // Indexing (IDE lifecycle)
                    "com.rescript.plugin.indexing.*",
                    // CodeVision (IDE lifecycle)
                    "com.rescript.plugin.codevision.*",
                )
            }
        }
        verify {
            rule {
                minBound(85)
            }
        }
    }
}

// ── Quality check tasks ──

val checkKdoc =
    tasks.register("checkKdoc") {
        description = "Verify all class/object/interface declarations have KDoc comments"
        group = "verification"
        // Resolve file collection at configuration time for configuration cache compatibility
        val sourceFiles = fileTree("src/main/kotlin") { include("**/*.kt") }.files.toList()
        val baseDir = projectDir
        doLast {
            val declarationPattern =
                Regex(
                    """^(\s*)((?:public|internal|private|protected|open|abstract|sealed|data|inner|value|enum)\s+)*(class|object|interface)\s+\w+""",
                )
            val kdocEndPattern = Regex("""\*/\s*$""")
            val annotationPattern = Regex("""^\s*@""")
            val violations = mutableListOf<String>()

            sourceFiles.forEach { file ->
                val lines = file.readLines()
                lines.forEachIndexed { index, line ->
                    if (declarationPattern.containsMatchIn(line)) {
                        var checkIndex = index - 1
                        while (checkIndex >= 0 && annotationPattern.containsMatchIn(lines[checkIndex])) {
                            checkIndex--
                        }
                        val hasKdoc = checkIndex >= 0 && kdocEndPattern.containsMatchIn(lines[checkIndex])
                        if (!hasKdoc) {
                            val relativePath = file.relativeTo(baseDir)
                            violations.add("  $relativePath:${index + 1}: ${line.trim()}")
                        }
                    }
                }
            }

            if (violations.isNotEmpty()) {
                throw GradleException(
                    "KDoc missing on ${violations.size} declaration(s):\n${violations.joinToString("\n")}",
                )
            }
            logger.lifecycle("checkKdoc: All declarations have KDoc comments")
        }
    }

val checkTestFiles =
    tasks.register("checkTestFiles") {
        description = "Verify production classes have corresponding test files"
        group = "verification"
        // Resolve file collections at configuration time for configuration cache compatibility
        val productionFileList = fileTree("src/main/kotlin/com/rescript/plugin") { include("**/*.kt") }.files.toList()
        val productionBaseDir = file("src/main/kotlin/com/rescript/plugin")
        val testFileNames =
            fileTree("src/test/kotlin/com/rescript/plugin") {
                include("**/*Test.kt")
            }.files.map { it.name }.toSet()
        doLast {
            val exemptPatterns =
                listOf(
                    "Configurable",
                    "SettingsEditor",
                    "ToolWindowPanel",
                    "WizardStep",
                    "Panel",
                    "LspServerDescriptor",
                    "LspServerSupportProvider",
                    "Lsp4jClient",
                    "StartupActivity",
                    "ProjectManagerListener",
                    "RunConfiguration",
                    "ConfigurationOptions",
                    "RescriptIcons",
                    "RescriptFileTypes",
                    "RescriptLanguage",
                )
            val exemptPackages =
                listOf(
                    "wizard/templates",
                    "settings",
                    "codestyle",
                    "config",
                    "statusbar",
                    "navbar",
                    "projectview",
                    "typeinfo",
                    "preview",
                    "repl",
                    "scratch",
                    "worksheet",
                    "ppx",
                    "diagram",
                    "dependencies",
                )

            val missing = mutableListOf<String>()
            productionFileList.forEach { file ->
                val relativePath = file.relativeTo(productionBaseDir).path
                val className = file.nameWithoutExtension
                val expectedTest = "${className}Test.kt"

                if (exemptPackages.any { relativePath.startsWith(it) }) return@forEach
                if (exemptPatterns.any { className.contains(it) }) return@forEach

                if (expectedTest !in testFileNames) {
                    missing.add("  $relativePath -> $expectedTest")
                }
            }

            if (missing.isNotEmpty()) {
                logger.warn(
                    "checkTestFiles: ${missing.size} production file(s) without tests:\n${missing.joinToString("\n")}",
                )
            }
            logger.lifecycle(
                "checkTestFiles: ${productionFileList.size - missing.size}/${productionFileList.size} files have tests",
            )
        }
    }

val checkExtensionPointRegistration =
    tasks.register("checkExtensionPointRegistration") {
        description = "Verify plugin.xml EP registrations match existing classes"
        group = "verification"
        // Resolve file references at configuration time for configuration cache compatibility
        val pluginXmlFiles =
            (
                listOf(file("src/main/resources/META-INF/plugin.xml")) +
                    fileTree("src/main/resources/META-INF") { include("rescript-*.xml") }.files
            ).toList()
        val kotlinSrcDir = file("src/main/kotlin")
        val javaSrcDir = file("src/main/java")
        val kotlinSrcFiles = fileTree("src/main/kotlin") { include("**/*.kt") }.files.toList()
        doLast {
            val classAttrPattern =
                Regex("""(?:implementation|implementationClass|className|serviceImplementation|instance)="([^"]+)"""")
            val registeredClasses = mutableSetOf<String>()
            pluginXmlFiles.forEach { xmlFile ->
                if (xmlFile.exists()) {
                    xmlFile.readLines().forEach { line ->
                        classAttrPattern.findAll(line).forEach { match ->
                            registeredClasses.add(match.groupValues[1])
                        }
                    }
                }
            }

            // Build index of all class/object declarations in source files
            val declaredClasses = mutableSetOf<String>()
            kotlinSrcFiles.forEach { file ->
                file.readLines().forEach { line ->
                    // Match class, object, interface, enum declarations
                    val match = Regex("""(?:class|object|interface)\s+(\w+)""").find(line)
                    if (match != null) {
                        declaredClasses.add(match.groupValues[1])
                    }
                }
            }

            val missingClasses = mutableListOf<String>()
            registeredClasses.forEach { fqn ->
                // For inner classes (Foo$Bar), check the outer class file first
                val outerFqn = if ('$' in fqn) fqn.substringBefore('$') else fqn
                val basePath = outerFqn.replace('.', '/')
                val ktFile = File(kotlinSrcDir, "$basePath.kt")
                val javaFile = File(javaSrcDir, "$basePath.java")
                if (!ktFile.exists() && !javaFile.exists()) {
                    // Fallback: check if the class name is declared anywhere in source
                    val simpleName = fqn.substringAfterLast('.').substringAfterLast('$')
                    if (simpleName !in declaredClasses) {
                        missingClasses.add("  $fqn -> $basePath.kt (or .java)")
                    }
                }
            }

            if (missingClasses.isNotEmpty()) {
                val detail = missingClasses.joinToString("\n")
                throw GradleException(
                    "EP registration references ${missingClasses.size} missing class(es):\n$detail",
                )
            }
            logger.lifecycle("checkExtensionPointRegistration: All ${registeredClasses.size} registered classes exist")
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
    test {
        useJUnitPlatform()
    }
    runIde {
        systemProperty("idea.is.internal", true)
        // Disable bundled Ultimate plugins that cause errors in the sandbox
        systemProperty("idea.required.plugins.id", "com.intellij.java")
        jvmArgs("-Xmx2G")
        autoReload = true
    }
}

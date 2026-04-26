import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java") // needed for JFlex-generated Java lexer
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.intellij.platform)
    alias(libs.plugins.grammarkit)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
}

repositories {
    mavenCentral()
    maven { url = uri("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies") }
    intellijPlatform {
        defaultRepositories()
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        // Emit JVM default methods directly instead of Kotlin's DefaultImpls
        // bridge. Prevents bytecode references to deprecated Java-interface
        // default methods (e.g., ToolWindowFactory.isApplicable,
        // isDoNotActivateOnStart) for subclasses that do not override them.
        freeCompilerArgs.add("-jvm-default=no-compatibility")
    }
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
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    // JUnit 3/4 TestCase is needed at compile time because IntelliJ's
    // BasePlatformTestCase extends UsefulTestCase which extends TestCase
    testImplementation(libs.junit4)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
}

// ── UI Test (Remote-Robot) source set ──

sourceSets {
    create("uiTest") {
        kotlin.srcDir("src/uiTest/kotlin")
        resources.srcDir("src/uiTest/resources")
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    }
    create("integrationTest") {
        kotlin.srcDir("src/integrationTest/kotlin")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += output + compileClasspath
    }
}

val uiTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations["implementation"])
}
val uiTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations["runtimeOnly"])
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations["testImplementation"])
}
val integrationTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations["testRuntimeOnly"])
}

dependencies {
    uiTestImplementation(kotlin("stdlib"))
    uiTestImplementation("com.intellij.remoterobot:remote-robot:0.11.23")
    uiTestImplementation("com.intellij.remoterobot:remote-fixtures:0.11.23")
    uiTestImplementation(libs.junit.jupiter)
    uiTestRuntimeOnly(libs.junit.platform.launcher)

    integrationTestImplementation(kotlin("stdlib"))
    integrationTestImplementation(libs.junit.jupiter)
    integrationTestRuntimeOnly(libs.junit.platform.launcher)
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
    version.set(libs.versions.ktlint.asProvider())
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    filter {
        exclude("**/generated/**")
    }
}

kover {
    currentProject {
        instrumentation {
            // uiTest requires a running IDE instance (Remote-Robot) and must not run in CI
            disabledForTestTasks.add("uiTest")
        }
    }
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
                // ── UI test classes (Remote-Robot, not unit-testable) ──
                packages(
                    "com.rescript.plugin.uitest",
                )
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
                    "com.rescript.plugin.inspection",
                    "com.rescript.plugin.intention",
                    "com.rescript.plugin.injection",
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
                )
                // ── Individual class exclusions (IDE-coupled classes in covered packages) ──
                // Wildcard (*) suffix matches inner/companion classes ($Companion, $install$1, etc.)
                classes(
                    // Auto-generated lexer
                    "com.rescript.plugin.lang.RescriptFlexLexer",
                    // Pure type definitions (no logic)
                    "com.rescript.plugin.RescriptFileTypes",
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
                minBound(86)
            }
        }
    }
}

// ── UI Test tasks ──

val runIdeForUiTests by intellijPlatformTesting.runIde.registering {
    task {
        jvmArgumentProviders +=
            CommandLineArgumentProvider {
                listOf(
                    "-Drobot-server.port=8082",
                    "-Dide.mac.message.dialogs.as.sheets=false",
                    "-Djb.privacy.policy.text=<!--999.999-->",
                    "-Djb.consents.confirmation.enabled=false",
                )
            }
        jvmArgs("-Xmx2G")
        // Open the sample project automatically so UI tests can find IdeFrameImpl
        val sampleProjectPath =
            layout.projectDirectory
                .dir("src/uiTest/testData/sample-project")
                .asFile.absolutePath
        argumentProviders +=
            CommandLineArgumentProvider {
                listOf(sampleProjectPath)
            }
    }
    plugins {
        robotServerPlugin()
    }
}

// ── Template Integration Test task ──
//
// Runs each ProjectTemplate end-to-end: generate files into a temporary
// directory, then `pnpm install` and `rescript build` (plus a `pnpm build`
// for templates that bundle a JS app). This is intentionally excluded
// from the default `test` task because it requires Node.js + pnpm and
// may take several minutes per template.

tasks.register<Test>("integrationTest") {
    description = "Run template generation integration tests (requires Node.js + pnpm)."
    group = "verification"
    useJUnitPlatform()
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
    // Run sequentially so concurrent pnpm processes do not contend over the store
    maxParallelForks = 1
    systemProperty("template.test.pnpm", System.getenv("PNPM_BIN") ?: "pnpm")
    systemProperty("template.test.node", System.getenv("NODE_BIN") ?: "node")
}

tasks.register<Test>("uiTest") {
    description = "Run UI tests with Remote-Robot"
    group = "verification"
    useJUnitPlatform()
    testClassesDirs = sourceSets["uiTest"].output.classesDirs
    classpath = sourceSets["uiTest"].runtimeClasspath
    // Gson in Remote-Robot needs reflective access on JDK 16+
    jvmArgs(
        "--add-opens",
        "java.base/java.lang=ALL-UNNAMED",
        "--add-opens",
        "java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens",
        "java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens",
        "java.base/java.util=ALL-UNNAMED",
    )
    systemProperty("robot-server.port", "8082")
    systemProperty(
        "screenshot.output.dir",
        layout.buildDirectory
            .dir("screenshots")
            .get()
            .asFile.absolutePath,
    )
    systemProperty(
        "test.project.path",
        layout.projectDirectory
            .dir("src/uiTest/testData/sample-project")
            .asFile.absolutePath,
    )
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
    // Dokka V2 scans src/main/java where the JFlex lexer is generated, so declare
    // the dependency explicitly to satisfy Gradle's strict task-output validation.
    withType<org.jetbrains.dokka.gradle.tasks.DokkaGenerateTask>().configureEach {
        dependsOn(generateRescriptLexer)
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
    // Purge stale plugin jars from the sandbox before each prepareSandbox run.
    // Prevents the IDE from loading a previous build's jar (e.g. after a
    // pluginVersion bump leaves rescript-intellij-plugin-<old>.jar behind),
    // which can surface as "implementation class is not specified" or other
    // PluginException failures from out-of-date plugin.xml entries.
    matching {
        it.name.startsWith("prepareSandbox") || it.name == "prepareTestSandbox"
    }.configureEach {
        val sandboxRoot = layout.projectDirectory.dir(".intellijPlatform/sandbox").asFile
        val projectBaseDir = projectDir
        doFirst {
            if (sandboxRoot.exists()) {
                sandboxRoot
                    .walk()
                    .filter {
                        it.isDirectory && it.name == "lib" && it.parentFile?.name == "rescript-intellij-plugin"
                    }.forEach { libDir ->
                        libDir
                            .listFiles()
                            ?.filter {
                                it.name.startsWith("rescript-intellij-plugin-") && it.name.endsWith(".jar")
                            }?.forEach { jar ->
                                logger.lifecycle("Removing stale sandbox jar: ${jar.relativeTo(projectBaseDir)}")
                                jar.delete()
                            }
                    }
            }
        }
    }
}

// Dokka configuration — generates Kotlin KDoc API reference into build/dokka/html.
// The output is served at /api/ alongside the Sphinx user guide on GitHub Pages.
dokka {
    moduleName.set("ReScript IntelliJ Plugin")
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
        suppressInheritedMembers.set(true)
    }
    dokkaSourceSets.configureEach {
        includes.from("docs/dokka-module.md")
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl("https://github.com/Nagatatz/rescript-intellij-plugin/tree/main/src/main/kotlin")
            remoteLineSuffix.set("#L")
        }
        // Generated JFlex lexer is not useful API documentation
        perPackageOption {
            matchingRegex.set("com\\.rescript\\.plugin\\.lang\\.RescriptFlexLexer.*")
            suppress.set(true)
        }
    }
}

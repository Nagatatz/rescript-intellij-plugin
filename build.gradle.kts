import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java") // needed for JFlex-generated Java lexer
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.intellij.platform)
    alias(libs.plugins.grammarkit)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.pitest)
}

repositories {
    mavenCentral()
    maven { url = uri("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies") }
    intellijPlatform {
        defaultRepositories()
    }
}

kotlin {
    // IntelliJ Platform 2026.2 ships Java 25 bytecode (class file version 69.0), which a
    // JDK 21 javac refuses to read. 2026.1 already moved the bundled runtime to JBR 25,
    // so every IDE at or above `pluginSinceBuild` can load Java 25 output.
    jvmToolchain(25)
    compilerOptions {
        // Emit JVM default methods directly instead of Kotlin's DefaultImpls
        // bridge. Prevents bytecode references to deprecated Java-interface
        // default methods (e.g., ToolWindowFactory.isApplicable,
        // isDoNotActivateOnStart) for subclasses that do not override them.
        freeCompilerArgs.add("-jvm-default=no-compatibility")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        // 2026.2 extracted the test runner into the implementation-detail plugin
        // `intellij.testRunner.plugin`; com.intellij.execution.testframework.* no longer
        // resolves from the platform classpath without these module dependencies.
        // smRunner declares testRunner as a module dependency, but that is not
        // transitive on the compile classpath, so both must be listed.
        bundledModule("intellij.platform.smRunner")
        bundledModule("intellij.platform.testRunner")
        bundledModule("intellij.spellchecker")
        bundledPlugin("com.intellij.modules.json")
        bundledPlugin("org.intellij.plugins.markdown")
        bundledPlugin("tanvd.grazi")
        // 1.405 parses the 2026.2 EAP bundled-plugin layout that 1.403 failed on
        // (ClosedFileSystemException), so recommended() can include 2026.2 again.
        pluginVerifier("1.405")
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

// ── Mutation testing (PIT) ──
//
// Runs mutation analysis on a focused subset of packages where unit tests
// already exist. The goal is to surface tests that pass even when the
// underlying logic is mutated (e.g. boundary conditions changed, return
// values negated). The CI integration runs this only on pull requests
// because it is slow.
//
// Excludes IDE-coupled classes that cannot be exercised by JUnit alone
// (parsers driven by PsiBuilder, generated lexer, PSI element types).

// One inventory is consumed by PIT and the CI summary to prevent target-label drift.
val pitConfiguration =
    groovy.json.JsonSlurper().parse(layout.projectDirectory.file("config/quality/pit-targets.json").asFile) as Map<*, *>

pitest {
    pitestVersion.set(libs.versions.pitest.asProvider())
    junit5PluginVersion.set(libs.versions.pitest.junit5)
    // PIT runs tests inside a minion JVM whose classpath does NOT include the
    // IntelliJ Platform jars resolved by the `org.jetbrains.intellij.platform`
    // Gradle plugin. JUnit 5 fails to discover any test class whose target
    // production class transitively references `com.intellij.*` types
    // (NoClassDefFoundError: com/intellij/psi/PsiElement,
    //  com/intellij/openapi/vfs/VirtualFile, ...), and PIT then reports
    // "tests did not pass without mutation".
    //
    // Keep PIT limited to explicitly reviewed pure JVM targets and tests.
    // Paths/RegexPatterns are static constants with no default-mutator candidates;
    // GlobExpander provides executable workspace traversal branches. All three
    // remain declared so the report exposes configured versus actual scope.
    targetClasses.set((pitConfiguration["classes"] as List<*>).map { it.toString() })
    targetTests.set((pitConfiguration["tests"] as List<*>).map { it.toString() })
    threads.set(1)
    outputFormats.set(listOf("HTML", "XML"))
    timestampedReports.set(false)
    failWhenNoMutations.set(true)
    jvmArgs.set(listOf("-Xmx2G", "-Dsun.zip.disableMemoryMapping=true"))
    testSourceSets.set(listOf(sourceSets.test.get()))
    mainSourceSets.set(listOf(sourceSets.main.get()))
}

// ── UI Test (Remote-Robot) source set ──

sourceSets {
    main {
        // plugin-version.properties is generated by generatePluginVersionProperties
        // so the runtime can read the plugin version without the @Internal
        // PluginManager descriptor-lookup APIs (all internalized in 2026.2).
        resources.srcDir(layout.buildDirectory.dir("generated/pluginVersion/resources"))
    }
    create("uiTest") {
        kotlin.srcDir("src/uiTest/kotlin")
        resources.srcDir("src/uiTest/resources")
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    }
    create("integrationTest") {
        kotlin.srcDir("src/integrationTest/kotlin")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output + sourceSets["test"].compileClasspath
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
            // recommended() resolves the current IDE set (2025.3 / 2026.1 / 2026.2
            // EAP). Previously pinned to 2026.1.2 because verifier-cli 1.403 choked
            // on the 2026.2 EAP layout; 1.405 (above) parses it, so recommended() is
            // safe again. Re-pin only if a future EAP layout breaks the verifier.
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

// Managed exclusions are exact reviewed class names, never package-wide wildcards.
// New classes therefore enter the denominator by default. Full reports override all filters.
val managedCoverageClasses =
    layout.projectDirectory
        .file("config/quality/coverage-exclusions.tsv")
        .asFile
        .readLines()
        .filter { it.isNotBlank() && !it.startsWith("#") }
        .map { line ->
            val fields = line.split('\t')
            require(fields.size == 3 && fields.all { it.isNotBlank() }) { "Invalid coverage exclusion: $line" }
            require('*' !in fields[0] && '?' !in fields[0]) { "Coverage exclusions must name exact classes" }
            fields[0]
        }.flatMap { name -> listOf(name, "$name\$*") }

kover {
    currentProject {
        sources {
            // Both reports describe production main bytecode, not test/CLI/UI harness classes.
            includedSourceSets.add("main")
        }
        instrumentation {
            // Unit coverage must never trigger npm installs, template E2E or an external IDE.
            disabledForTestTasks.addAll(listOf("uiTest", "integrationTest", "integrationIdeTest"))
        }
        createVariant("Full") {
            add("jvm")
        }
    }
    reports {
        total {
            filters {
                excludes {
                    classes(managedCoverageClasses)
                }
            }
            xml {
                title = "Managed main classes — unit tests"
                onCheck = false
            }
            html {
                title = "Managed main classes — unit tests"
                onCheck = false
            }
            verify {
                // Keep the existing ratchet. An honest Full denominator is reported separately.
                rule {
                    minBound(87)
                }
            }
        }
        variant("Full") {
            // Explicitly unfiltered: generated code, IDE adapters and untested classes remain visible.
            filters {}
            xml {
                title = "All main classes, no exclusions — unit tests"
                xmlFile.set(layout.buildDirectory.file("reports/kover/full.xml"))
                onCheck = false
            }
            html {
                title = "All main classes, no exclusions — unit tests"
                htmlDir.set(layout.buildDirectory.dir("reports/kover/full-html"))
                onCheck = false
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
    filter { includeTestsMatching("com.rescript.plugin.wizard.*") }
    // Run sequentially so concurrent pnpm processes do not contend over the store
    maxParallelForks = 1
    systemProperty("template.test.pnpm", System.getenv("PNPM_BIN") ?: "pnpm")
    systemProperty("template.test.node", System.getenv("NODE_BIN") ?: "node")
}

// IDE-backed CLI regression tests use the official SDK/runtime and a dedicated sandbox.
// Keep the injected platform classpath; append the custom source set rather than replacing it.
intellijPlatformTesting.testIde.register("integrationIdeTest") {
    testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    plugins {
        bundledPlugin("com.intellij.java")
        disablePlugin("com.intellij.modules.ultimate")
    }
    task {
        description = "Run IDE-backed CLI regression tests without template installation suites."
        useJUnitPlatform()
        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath += sourceSets["integrationTest"].runtimeClasspath
        filter { excludeTestsMatching("com.rescript.plugin.wizard.*") }
        maxParallelForks = 1
        shouldRunAfter(tasks.test)
    }
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

// Writes the plugin version into a generated resource so RescriptErrorReporter
// can read it at runtime without the @Internal PluginManager lookup APIs that
// 2026.2 removed from the public surface.
val generatePluginVersionProperties =
    tasks.register("generatePluginVersionProperties") {
        val outputDir = layout.buildDirectory.dir("generated/pluginVersion/resources")
        val pluginVersionValue = providers.gradleProperty("pluginVersion")
        inputs.property("pluginVersion", pluginVersionValue)
        outputs.dir(outputDir)
        doLast {
            val target =
                outputDir
                    .get()
                    .file("com/rescript/plugin/plugin-version.properties")
                    .asFile
            target.parentFile.mkdirs()
            target.writeText("version=${pluginVersionValue.get()}\n")
        }
    }

tasks {
    processResources {
        dependsOn(generatePluginVersionProperties)
    }
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
        // Opt-in test slicing via -Pscope=<fast|perf|cli>.
        // `fast` skips long-running suites for tight PR feedback;
        // `perf` and `cli` isolate the smoke benchmarks and the
        // mmdc/dot CLI tests that ship in their own packages.
        // No -Pscope flag → full default behaviour (every test runs),
        // which is what CI continues to use.
        val scope = providers.gradleProperty("scope").orNull
        when (scope) {
            "fast" -> {
                filter {
                    excludeTestsMatching("*PerfTest")
                    excludeTestsMatching("*IntegrationTest")
                    excludeTestsMatching("com.rescript.plugin.cli.*")
                }
            }

            "perf" -> {
                filter {
                    includeTestsMatching("com.rescript.plugin.perf.*")
                }
            }

            "cli" -> {
                filter {
                    includeTestsMatching("com.rescript.plugin.cli.*")
                }
            }

            null -> {
                // default: no filter
            }

            else -> {
                throw GradleException(
                    "Unknown -Pscope=$scope. Supported values: fast | perf | cli (or omit -Pscope for the full suite).",
                )
            }
        }
    }
    named<org.jetbrains.intellij.platform.gradle.tasks.PrepareSandboxTask>("prepareTestSandbox") {
        // 2026.2.3 gives an Ultimate startup activity the same obfuscated name as
        // a core class. The flattened unit-test classpath resolves the core class
        // instead of the activity; the normal IDE uses separate plugin classloaders.
        disabledPlugins.add("com.intellij.modules.ultimate")
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
    //
    // Each task only purges its own destination sandbox (plugins/ vs
    // plugins-test/). Sweeping the whole sandbox root from here used to
    // delete the test sandbox's jar after prepareTestSandbox had already
    // run, making a chained `buildPlugin test` invocation fail with
    // NoClassDefFoundError during test discovery.
    matching {
        it.name.startsWith("prepareSandbox") || it.name == "prepareTestSandbox"
    }.configureEach {
        val projectBaseDir = projectDir
        doFirst {
            val sandboxPluginsDir = (this as? Sync)?.destinationDir ?: return@doFirst
            sandboxPluginsDir
                .resolve("rescript-intellij-plugin/lib")
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

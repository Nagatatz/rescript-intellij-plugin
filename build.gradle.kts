import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java") // needed for JFlex-generated Java lexer
    id("org.jetbrains.kotlin.jvm") version "2.3.10"
    id("org.jetbrains.intellij.platform") version "2.11.0"
    id("org.jetbrains.grammarkit") version "2023.3.0.1"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
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

sourceSets {
    test {
        kotlin.srcDir("src/test-local/kotlin")
    }
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
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

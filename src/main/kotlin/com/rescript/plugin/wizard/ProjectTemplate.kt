package com.rescript.plugin.wizard

import com.rescript.plugin.wizard.templates.AwsLambdaTemplateFiles
import com.rescript.plugin.wizard.templates.BasicTemplateFiles
import com.rescript.plugin.wizard.templates.CliToolTemplateFiles
import com.rescript.plugin.wizard.templates.CloudflareWorkersTemplateFiles
import com.rescript.plugin.wizard.templates.ElectronTemplateFiles
import com.rescript.plugin.wizard.templates.GoogleCloudRunTemplateFiles
import com.rescript.plugin.wizard.templates.HonoTemplateFiles
import com.rescript.plugin.wizard.templates.MonorepoTemplateFiles
import com.rescript.plugin.wizard.templates.NextjsTemplateFiles
import com.rescript.plugin.wizard.templates.NpmLibraryTemplateFiles
import com.rescript.plugin.wizard.templates.ReactNativeTemplateFiles
import com.rescript.plugin.wizard.templates.ViteReactTemplateFiles

/**
 * Categories for grouping project templates in the wizard UI.
 */
enum class TemplateCategory(
    val displayName: String,
) {
    BASIC("Basic"),
    FRONTEND("Frontend"),
    DESKTOP("Desktop"),
    BACKEND("Backend"),
    SERVERLESS("Serverless"),
    MOBILE("Mobile"),
    LIBRARY("Library"),
    TOOL("Tool"),
    FULL_STACK("Full Stack"),
}

/**
 * Available project templates for the ReScript project wizard.
 *
 * Each entry defines a template with display metadata and delegates file generation
 * to a corresponding object in the `templates` package. The [generateFiles] method
 * returns a map of relative file paths to their content strings.
 *
 * @see ProjectFileBuilders for shared file generation utilities
 * @see RescriptModuleBuilder for wizard integration
 */
enum class ProjectTemplate(
    val displayName: String,
    val description: String,
    val category: TemplateCategory,
    val sourceRoots: List<String> = listOf("src"),
) {
    BASIC(
        "Basic",
        "Minimal ReScript project with console output",
        TemplateCategory.BASIC,
    ),
    VITE_REACT(
        "Vite + React",
        "React single-page application with Vite bundler",
        TemplateCategory.FRONTEND,
    ),
    NEXTJS(
        "Next.js",
        "Server-side rendered React application with Next.js",
        TemplateCategory.FRONTEND,
    ),
    ELECTRON(
        "Electron",
        "Cross-platform desktop application with Electron",
        TemplateCategory.DESKTOP,
    ),
    HONO(
        "Hono (Node.js)",
        "Lightweight web server with Hono framework on Node.js",
        TemplateCategory.BACKEND,
    ),
    CLOUDFLARE_WORKERS(
        "Cloudflare Workers",
        "Serverless API on Cloudflare Workers with Hono",
        TemplateCategory.SERVERLESS,
    ),
    AWS_LAMBDA(
        "AWS Lambda",
        "Serverless function on AWS Lambda with Hono",
        TemplateCategory.SERVERLESS,
    ),
    GOOGLE_CLOUD_RUN(
        "Google Cloud Run",
        "Container-based service on Google Cloud Run with Hono",
        TemplateCategory.SERVERLESS,
    ),
    REACT_NATIVE(
        "React Native (Expo)",
        "Mobile application with React Native and Expo",
        TemplateCategory.MOBILE,
    ),
    NPM_LIBRARY(
        "npm Library",
        "Publishable npm package with ReScript",
        TemplateCategory.LIBRARY,
    ),
    CLI_TOOL(
        "CLI Tool",
        "Command-line tool with argument parsing",
        TemplateCategory.TOOL,
    ),
    MONOREPO(
        "Monorepo (Hono + React)",
        "Full-stack monorepo with Hono backend and React frontend",
        TemplateCategory.FULL_STACK,
        sourceRoots = listOf("packages/shared/src", "packages/server/src", "packages/client/src"),
    ),
    ;

    /**
     * Generates all project files for this template.
     *
     * @param projectName the name of the project being created
     * @return a map of relative file paths to their string content
     */
    fun generateFiles(projectName: String): Map<String, String> =
        when (this) {
            BASIC -> BasicTemplateFiles.generate(projectName)
            VITE_REACT -> ViteReactTemplateFiles.generate(projectName)
            NEXTJS -> NextjsTemplateFiles.generate(projectName)
            ELECTRON -> ElectronTemplateFiles.generate(projectName)
            HONO -> HonoTemplateFiles.generate(projectName)
            CLOUDFLARE_WORKERS -> CloudflareWorkersTemplateFiles.generate(projectName)
            AWS_LAMBDA -> AwsLambdaTemplateFiles.generate(projectName)
            GOOGLE_CLOUD_RUN -> GoogleCloudRunTemplateFiles.generate(projectName)
            REACT_NATIVE -> ReactNativeTemplateFiles.generate(projectName)
            NPM_LIBRARY -> NpmLibraryTemplateFiles.generate(projectName)
            CLI_TOOL -> CliToolTemplateFiles.generate(projectName)
            MONOREPO -> MonorepoTemplateFiles.generate(projectName)
        }
}

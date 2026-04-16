package com.rescript.plugin.wizard

import com.rescript.plugin.wizard.templates.AwsLambdaTemplateFiles
import com.rescript.plugin.wizard.templates.BasicTemplateFiles
import com.rescript.plugin.wizard.templates.CliToolTemplateFiles
import com.rescript.plugin.wizard.templates.CloudflareWorkersTemplateFiles
import com.rescript.plugin.wizard.templates.ElectronTemplateFiles
import com.rescript.plugin.wizard.templates.FullStackTemplateFiles
import com.rescript.plugin.wizard.templates.GoogleCloudRunTemplateFiles
import com.rescript.plugin.wizard.templates.HonoGraphqlTemplateFiles
import com.rescript.plugin.wizard.templates.HonoTemplateFiles
import com.rescript.plugin.wizard.templates.MonorepoTemplateFiles
import com.rescript.plugin.wizard.templates.NextjsTemplateFiles
import com.rescript.plugin.wizard.templates.NpmLibraryTemplateFiles
import com.rescript.plugin.wizard.templates.ReactNativeCliTemplateFiles
import com.rescript.plugin.wizard.templates.ReactNativeTemplateFiles
import com.rescript.plugin.wizard.templates.TemplateContext
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
        "Lightweight web server with Hono framework on Node.js, SQLite (Drizzle), and OpenAPI/Scalar UI",
        TemplateCategory.BACKEND,
    ),
    HONO_GRAPHQL(
        "Hono GraphQL",
        "GraphQL API on Hono with graphql-yoga, GraphiQL playground, and SQLite (Drizzle)",
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
    REACT_NATIVE_CLI(
        "React Native (Community CLI)",
        "Mobile app with React Native Community CLI (bare workflow) for native Android/iOS access",
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
        "Full-stack pnpm/npm/yarn workspace with Hono + Drizzle backend and React frontend",
        TemplateCategory.FULL_STACK,
        sourceRoots = listOf("packages/shared/src", "packages/server/src", "packages/client/src"),
    ),
    FULL_STACK(
        "Full-Stack (single package)",
        "Single-package full-stack app with Hono backend, Vite+React frontend, and SQLite (Drizzle)",
        TemplateCategory.FULL_STACK,
        sourceRoots = listOf("src/shared", "src/server", "src/client"),
    ),
    ;

    /**
     * Generates all project files for this template using the full template context.
     *
     * Templates that have migrated to the context-aware generator receive [ctx] directly;
     * those still on the legacy signature fall back to [generateFiles(String)] which is
     * wired to dispatch to the same per-template objects.
     *
     * @param ctx the template context (project name + selected package manager)
     * @return a map of relative file paths to their string content
     */
    fun generateFiles(ctx: TemplateContext): Map<String, String> =
        when (this) {
            BASIC -> BasicTemplateFiles.generate(ctx)
            VITE_REACT -> ViteReactTemplateFiles.generate(ctx)
            NEXTJS -> NextjsTemplateFiles.generate(ctx)
            ELECTRON -> ElectronTemplateFiles.generate(ctx)
            HONO -> HonoTemplateFiles.generate(ctx)
            HONO_GRAPHQL -> HonoGraphqlTemplateFiles.generate(ctx)
            CLOUDFLARE_WORKERS -> CloudflareWorkersTemplateFiles.generate(ctx)
            AWS_LAMBDA -> AwsLambdaTemplateFiles.generate(ctx)
            GOOGLE_CLOUD_RUN -> GoogleCloudRunTemplateFiles.generate(ctx)
            REACT_NATIVE -> ReactNativeTemplateFiles.generate(ctx)
            REACT_NATIVE_CLI -> ReactNativeCliTemplateFiles.generate(ctx)
            NPM_LIBRARY -> NpmLibraryTemplateFiles.generate(ctx)
            CLI_TOOL -> CliToolTemplateFiles.generate(ctx)
            MONOREPO -> MonorepoTemplateFiles.generate(ctx)
            FULL_STACK -> FullStackTemplateFiles.generate(ctx)
        }

    /**
     * Back-compatible entry point that defaults to pnpm when no package manager is specified.
     *
     * @param projectName the name of the project being created
     */
    fun generateFiles(projectName: String): Map<String, String> =
        generateFiles(TemplateContext(projectName, PackageManager.PNPM))
}

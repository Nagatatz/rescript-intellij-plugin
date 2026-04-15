---
myst:
  html_meta:
    "keywords": "recipes, how-to, guides, tutorials, workflows"
---

# Recipes

Task-oriented guides for common ReScript development workflows. Each recipe walks you through a specific use case step by step.

::::{grid} 1 1 2 2
:gutter: 3

:::{grid-item-card} Create a React Component
:link: create-react-component
:link-type: doc
Scaffold a new React component using file templates and live templates.

{bdg-success}`Native`
:::

:::{grid-item-card} Find Dead Code
:link: find-dead-code
:link-type: doc
Detect and remove unused exports, values, and types.

{bdg-success}`Native` {bdg-warning}`Configuration Required`
:::

:::{grid-item-card} Set Up a Monorepo
:link: setup-monorepo
:link-type: doc
Configure the plugin to work with monorepo project structures.

{bdg-primary}`LSP Required`
:::

:::{grid-item-card} Debug ReScript
:link: debug-rescript
:link-type: doc
Debug compiled JavaScript output using the built-in debugger.

{bdg-success}`Native`
:::

:::{grid-item-card} Convert from TypeScript
:link: convert-from-typescript
:link-type: doc
Convert TypeScript/JavaScript code to ReScript and generate bindings.

{bdg-success}`Native`
:::

:::{grid-item-card} Optimize Imports
:link: optimize-imports
:link-type: doc
Clean up duplicate and unused open statements.

{bdg-success}`Native`
:::

:::{grid-item-card} Add a Hono Endpoint
:link: add-hono-endpoint
:link-type: doc
Extend the Hono REST template with a new route, validation, and storage.

{bdg-info}`Template`
:::

:::{grid-item-card} Add a GraphQL Resolver
:link: add-graphql-resolver
:link-type: doc
Expand the SDL and wire resolvers for the Hono + GraphQL template.

{bdg-info}`Template`
:::

:::{grid-item-card} Set Up Drizzle
:link: setup-drizzle
:link-type: doc
Change the schema, run migrations, and point at Turso for production.

{bdg-info}`Template`
:::

:::{grid-item-card} Add OpenAPI Docs
:link: add-openapi-docs
:link-type: doc
Enrich the Scalar UI documentation shipped with the Hono REST template.

{bdg-info}`Template`
:::
::::

```{toctree}
:hidden:
:maxdepth: 1

create-react-component
find-dead-code
setup-monorepo
debug-rescript
convert-from-typescript
optimize-imports
add-hono-endpoint
add-graphql-resolver
setup-drizzle
add-openapi-docs
```

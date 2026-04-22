// Common HTML shell shared by every page. Loads HTMX from unpkg and leaves a
// `<main>` landmark for page-specific content. Swap the CDN URL for a bundled
// asset once you wire up `assets/` through the Vite plugin.
@jsx.component
let make = (~title: string, ~children: Hjsx.element) => {
  <html lang="en">
    <head>
      <meta charSet="utf-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1" />
      <title>{Hjsx.string(title)}</title>
      <script
        src={`https://unpkg.com/htmx.org@{{htmxVersion}}`}
        crossOrigin="anonymous"
      />
    </head>
    <body>
      <main> {children} </main>
    </body>
  </html>
}

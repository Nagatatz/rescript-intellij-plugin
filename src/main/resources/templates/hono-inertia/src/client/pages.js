// Inertia page resolver shim.
//
// ReScript 12's `Js.import` requires a statically resolvable identifier, so we
// can't write `import("./Pages/" ++ name ++ ".res.mjs")` directly from
// ReScript. Instead, we use Vite's `import.meta.glob` here in plain JS and
// expose a typed lookup function that the ReScript entry imports.
//
// Every page module is compiled from `@react.component let make = ...`, so
// the resolved module exposes `make` as the React component. Inertia reads
// `.default` off whatever `resolve` returns, so we re-export `make` there.
const pages = import.meta.glob("./Pages/**/*.res.mjs");

export async function resolvePage(name) {
  const path = `./Pages/${name}.res.mjs`;
  const loader = pages[path];
  if (!loader) {
    throw new Error(`Inertia page not found: ${name}`);
  }
  const mod = await loader();
  if (!mod.make) {
    throw new Error(
      `Inertia page module ${name} does not export 'make'; did you forget @react.component?`,
    );
  }
  return { default: mod.make };
}

// Inertia page resolver shim.
//
// ReScript 12's `Js.import` requires a statically resolvable identifier, so we
// can't write `import("./Pages/" ++ name ++ ".res.mjs")` directly from
// ReScript. Instead, we use Vite's `import.meta.glob` here in plain JS and
// expose a typed lookup function that the ReScript entry imports.
//
// Inertia reads `.default` off whatever resolve returns; ReScript-compiled
// modules expose their React component as `make`, so we re-export it under
// `default` for Inertia to pick up.
const pages = import.meta.glob("./Pages/**/*.res.mjs");

export async function resolvePage(name) {
  const path = `./Pages/${name}.res.mjs`;
  const loader = pages[path];
  if (!loader) {
    throw new Error(`Inertia page not found: ${name}`);
  }
  const mod = await loader();
  return { default: mod.make ?? mod.default ?? mod };
}

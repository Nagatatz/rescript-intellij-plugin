// Ambient module declarations that let TypeScript see compiled ReScript output.
// `rescript` emits `*.res.mjs` (runtime) and genType emits `*.gen.tsx` / `*.gen.d.ts`
// (typed). `.gen.*` files carry real type information; the `.res.mjs` declaration
// below covers the untyped runtime entry points (e.g. Next.js route handler shims).

declare module "*.res.mjs";

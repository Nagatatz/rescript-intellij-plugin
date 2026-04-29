// ReScript compiles `.res` files to `.res.mjs` (configured in rescript.json).
// Tell TypeScript that imports from those compiled outputs are valid so that
// TSX routes can pull in ReScript components without `// @ts-ignore`.
declare module "*.res.mjs" {
  const value: any;
  export = value;
}

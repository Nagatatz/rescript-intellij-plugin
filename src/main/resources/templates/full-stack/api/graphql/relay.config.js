// Relay compiler configuration. Run via `pnpm relay` or `pnpm relay:watch`;
// the compiler reads %relay() tags in src/client and emits typed artifacts
// under src/client/__generated__/ (gitignored).
module.exports = {
  src: "./src/client",
  schema: "./src/server/schema.graphql",
  language: "rescript",
  artifactDirectory: "./src/client/__generated__",
  exclude: ["**/node_modules/**", "**/__generated__/**", "**/lib/**"],
};

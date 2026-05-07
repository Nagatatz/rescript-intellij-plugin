// Drizzle-kit configuration. `drizzle-kit generate` inspects the schema and
// emits SQL migration files into ./drizzle/.
import { defineConfig } from "drizzle-kit";

export default defineConfig({
  schema: "./src/Schema.res.mjs",
  out: "./drizzle",
  dialect: "mysql",
  dbCredentials: {
    url: process.env.DATABASE_URL ?? "mysql://root:dev@localhost:3306/app",
  },
});

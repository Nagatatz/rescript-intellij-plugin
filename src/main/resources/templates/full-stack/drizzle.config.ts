import { defineConfig } from "drizzle-kit";

export default defineConfig({
  schema: "./src/server/Schema.res.mjs",
  out: "./drizzle",
  dialect: "sqlite",
  dbCredentials: {
    url: process.env.DATABASE_URL ?? "file:./data/app.db",
  },
});
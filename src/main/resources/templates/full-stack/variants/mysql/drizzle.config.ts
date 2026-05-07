import { defineConfig } from "drizzle-kit";

export default defineConfig({
  schema: "./src/server/Schema.res.mjs",
  out: "./drizzle",
  dialect: "mysql",
  dbCredentials: {
    url: process.env.DATABASE_URL ?? "mysql://root:dev@localhost:3306/app",
  },
});

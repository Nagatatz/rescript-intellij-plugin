# syntax=docker/dockerfile:1

# ---------- deps: install node_modules with the lockfile pnpm uses ----------
FROM oven/bun:1 AS deps
WORKDIR /app
COPY package.json ./
COPY pnpm-lock.yaml* bun.lockb* ./
RUN bun install --ignore-scripts

# ---------- builder: compile ReScript + build client assets ----------------
FROM oven/bun:1 AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN bunx rescript && bun run build

# ---------- runtime: slim image that just runs the compiled server ---------
FROM oven/bun:1-slim AS runtime
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/src ./src
COPY --from=builder /app/dist ./dist
COPY package.json rescript.json ./
EXPOSE 4444
CMD ["bun", "run", "src/App.res.mjs"]

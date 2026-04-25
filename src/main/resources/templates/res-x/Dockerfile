# syntax=docker/dockerfile:1.7

# ---------- deps: install node_modules with Bun's text-based lockfile --------
FROM oven/bun:1 AS deps
WORKDIR /app
COPY package.json ./
COPY bun.lock* ./
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
# Copy compiled artifacts into the image, then hand ownership to the
# unprivileged `bun` user (uid 1000) that ships with the base image.
COPY --from=builder --chown=bun:bun /app/node_modules ./node_modules
COPY --from=builder --chown=bun:bun /app/src ./src
COPY --from=builder --chown=bun:bun /app/dist ./dist
COPY --chown=bun:bun package.json rescript.json ./
USER bun
EXPOSE 4444
CMD ["bun", "run", "src/App.res.mjs"]

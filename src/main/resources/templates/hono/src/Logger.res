// hono/logger: structured request logging middleware.
@module("hono/logger") external logger: unit => Hono.middleware = "logger"

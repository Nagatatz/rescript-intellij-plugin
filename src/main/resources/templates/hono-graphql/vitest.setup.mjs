// Pin DATABASE_URL to libsql's in-memory database so the `Db.res` import
// chain (Db → Schema → Resolvers) does not try to open `./data/app.db`
// when the test runner statically imports `Server.res`.
process.env.DATABASE_URL = ":memory:";

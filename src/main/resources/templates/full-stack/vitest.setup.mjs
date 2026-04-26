// Pin DATABASE_URL to libsql's in-memory database before any test file
// imports so the server's top-level `Db.res` `createClient(...)` doesn't
// try to open `./data/app.db` (which doesn't exist in the test temp dir).
process.env.DATABASE_URL = ":memory:";

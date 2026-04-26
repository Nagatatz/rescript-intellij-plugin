// Pin DATABASE_URL to libsql's in-memory database before any module imports
// fire. Without this the top-level `createClient(...)` in `Db.res` tries to
// open `./data/app.db`, which doesn't exist in fresh checkouts or the
// integration-test temp dir.
process.env.DATABASE_URL = ":memory:";

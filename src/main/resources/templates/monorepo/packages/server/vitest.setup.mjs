// Pin DATABASE_URL to libsql's in-memory database so importing
// `Server.res` (transitively `Db.res`) inside vitest doesn't try to
// open the on-disk SQLite file.
process.env.DATABASE_URL = ":memory:";

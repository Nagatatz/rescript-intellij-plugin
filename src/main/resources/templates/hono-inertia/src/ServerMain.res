// Entry point invoked by `vp dev` (via the Vite middleware proxy) and by
// `node src/ServerMain.res.mjs` in production. Delegates to `Server.res` so
// the app definition stays importable from tests without binding a port.
Server.start()

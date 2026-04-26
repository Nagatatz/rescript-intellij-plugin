// Entry point invoked by `npm run dev` / `npm run start`. Delegates to
// `Server.res` so the app definition stays importable from tests without
// binding a port.
Server.start()

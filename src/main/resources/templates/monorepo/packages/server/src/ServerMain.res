// Entry point invoked by the workspace `dev` / `start` scripts. Delegates to
// `Server.res` so the app definition stays importable from tests without
// binding a port.
Server.start()

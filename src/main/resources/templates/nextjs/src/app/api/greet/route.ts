// Thin re-export shim. Next.js requires `route.(ts|js|mjs)` as the filename,
// but ReScript requires module filenames to start with an uppercase letter.
// The handler body lives in `GreetRoute.res`; this file just surfaces POST.
export { post as POST } from "./GreetRoute.res.mjs";

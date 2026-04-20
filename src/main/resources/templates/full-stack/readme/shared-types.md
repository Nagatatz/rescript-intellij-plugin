`src/shared/Shared.res` groups all shared types into nested modules
(`Shared.Types.*`, `Shared.Api.*`) that `src/server/` and `src/client/`
consume directly. When you change a field on a request or response record,
both sides fail to compile until they agree — that is the point.

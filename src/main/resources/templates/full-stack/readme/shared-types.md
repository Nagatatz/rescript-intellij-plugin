`src/shared/Types.res` and `src/shared/Api.res` are imported from both
`src/server/` and `src/client/` as `Shared.Types` / `Shared.Api`. When you
change a field on a request or response record, both sides fail to compile
until they agree — that is the point.

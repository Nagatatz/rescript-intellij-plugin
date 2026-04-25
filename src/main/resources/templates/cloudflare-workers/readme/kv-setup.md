Workers KV uses two separate namespaces: one for `wrangler deploy`
(production) and one for `wrangler dev` (preview). Keeping them split
prevents local experimentation from mutating production data.

### 1. Create both namespaces

```bash
npx wrangler kv namespace create GREETINGS
npx wrangler kv namespace create GREETINGS --preview
```

Each command prints a JSON snippet with an `id`. Copy them into
`wrangler.jsonc`:

```jsonc
"kv_namespaces": [
  {
    "binding": "GREETINGS",
    "id": "<production-id-from-step-1>",
    "preview_id": "<preview-id-from-step-1>"
  }
]
```

### 2. Verify the wiring

```bash
# Lists every namespace on the account, including the two you just made.
npx wrangler kv namespace list
```

### 3. Local vs production reads/writes

```bash
# Reads/writes against the *preview* namespace (preview_id):
npx wrangler kv key put --binding=GREETINGS hello world

# Targets the *production* namespace (id) explicitly:
npx wrangler kv key put --binding=GREETINGS --remote hello world
```

`wrangler dev` always uses `preview_id`. `wrangler deploy` uses `id`.
If `preview_id` is omitted, Wrangler falls back to a one-off in-memory
store that is wiped between sessions — fine for throwaway demos, but
brittle for anything you want to inspect later.

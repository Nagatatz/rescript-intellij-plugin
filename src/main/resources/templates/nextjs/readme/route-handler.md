`src/app/api/greet/route.ts` is a Next.js Route Handler (edge/node runtime).
Swap the hand-rolled parsing for `zod` / `valibot` before production:

```ts
import { z } from "zod";
const Body = z.object({ name: z.string().min(1) });
export async function POST(req: NextRequest) {
  const parsed = Body.safeParse(await req.json());
  if (!parsed.success) return NextResponse.json({ error: parsed.error }, { status: 400 });
  return NextResponse.json({ message: `Hello, ${parsed.data.name}!` });
}
```
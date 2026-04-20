// POST /api/greet — minimal Route Handler. Parses { name } and returns a greeting.
// Extend with zod/valibot for production validation.
import { NextRequest, NextResponse } from "next/server";

export async function POST(req: NextRequest) {
  const body = await req.json().catch(() => ({}));
  const name = typeof body?.name === "string" ? body.name : "stranger";
  return NextResponse.json({ message: `Hello, ${name}!` });
}
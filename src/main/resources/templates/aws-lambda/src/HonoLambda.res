// hono/aws-lambda adapter: converts a Hono app into a Lambda handler.
type lambdaEvent
type lambdaResult
type handler = lambdaEvent => promise<lambdaResult>

@module("hono/aws-lambda") external handle: Hono.app => handler = "handle"

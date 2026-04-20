Persist orders to DynamoDB:

```bash
pnpm add @aws-sdk/client-dynamodb @aws-sdk/lib-dynamodb
```

```rescript
type docClient
@module("@aws-sdk/lib-dynamodb") @new
external makeClient: 'opts => docClient = "DynamoDBDocumentClient"
@send external send: (docClient, 'command) => promise<'result> = "send"
```

Grant the Lambda IAM role `dynamodb:PutItem` / `dynamodb:GetItem` on the target table.
Build & upload:

```bash
{{cmdBuild}}
cd dist && zip lambda.zip index.mjs
aws lambda update-function-code --function-name {{projectName}} \\
  --zip-file fileb://lambda.zip
```

Runtime: Node.js 20. Handler: `index.handler`. Use API Gateway HTTP API as the trigger.
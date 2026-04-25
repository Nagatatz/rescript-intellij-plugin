Build & upload:

```bash
{{cmdBuild}}
cd dist && zip lambda.zip index.mjs
aws lambda update-function-code --function-name {{projectName}} \\
  --zip-file fileb://lambda.zip
```

Runtime: Node.js {{nodeMajor}}. Handler: `index.handler`. Use API Gateway HTTP API as the trigger.

See the **Bundling Strategy** section below for guidance on shrinking
the deploy artifact (Lambda Layers, `--external`, tree-shaking).
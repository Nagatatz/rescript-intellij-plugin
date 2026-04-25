The default `bundle` script ships every dependency in a single ESM file
(`dist/index.mjs`). That keeps cold-start latency low and the deploy
package self-contained, but it also means every dependency you add
inflates the upload size.

When the artifact starts approaching the **50 MB direct-upload limit**
(or 250 MB unzipped), reach for one of the following:

### Mark Lambda-provided deps as external

The AWS SDK v3 (`@aws-sdk/*`) is preinstalled on the Node.js Lambda
runtime. Excluding it from the bundle saves several MB per command:

```bash
esbuild src/Server.res.mjs --bundle --platform=node \
  --outfile=dist/index.mjs --format=esm \
  --external:@aws-sdk/* --external:aws-sdk
```

Any module you mark `--external` must already exist at runtime — either
because Lambda ships it (`@aws-sdk/*`) or because you ship it via a
Lambda Layer.

### Lambda Layers

For large native dependencies (Sharp, Prisma engines, custom binaries),
publish them once as a Layer and exclude them from `--external`:

```bash
aws lambda publish-layer-version \
  --layer-name {{projectName}}-deps \
  --compatible-runtimes nodejs{{nodeMajor}}.x \
  --zip-file fileb://layer.zip

aws lambda update-function-configuration \
  --function-name {{projectName}} \
  --layers arn:aws:lambda:<region>:<account>:layer:{{projectName}}-deps:1
```

Layers are cached per execution environment, so cold-start cost is paid
once per container instead of every invocation.

### Tree-shaking and source maps

Add `--tree-shaking=true --minify --sourcemap` for production builds.
Source maps stay outside the deployment artifact (`dist/index.mjs.map`)
but you can upload them to your APM (Datadog, Sentry, CloudWatch RUM)
for symbolicated stack traces.

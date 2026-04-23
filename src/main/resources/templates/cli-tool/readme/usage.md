```bash
{{cmdStart}} -- greet Alice
{{cmdStart}} -- greet Alice --shout
{{cmdStart}} -- init --name my-project --dir ./projects/my-project
{{cmdStart}} -- --help
```

`init` validates the `--name` / `--dir` options through `Validation.parseInitOptions`
using the {{validationLibrary}} library selected in the Project Wizard. Missing
or malformed options are reported with a `init: …` error.
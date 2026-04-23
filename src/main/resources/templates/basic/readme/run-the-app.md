```bash
# Print hello with the default name
{{cmdStart}}

# Pass a name (arguments after -- are forwarded to Node)
{{cmdStart}} -- Alice

# Tell the app to read a file
{{cmdStart}} -- Alice --file hello.txt

# Validate config.sample.json through Validation.parseConfig ({{validationLibrary}}) and use
# the `greeting` / `repeat` fields to drive the output
{{cmdStart}} -- Alice --config config.sample.json
```
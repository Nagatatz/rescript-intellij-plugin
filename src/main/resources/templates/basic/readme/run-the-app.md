```bash
# Print hello with the default name
{{cmdStart}}

# Pass a name (arguments after -- are forwarded to Node)
{{cmdStart}} -- Alice

# Tell the app to read a file
{{cmdStart}} -- Alice --file hello.txt
```
---
myst:
  html_meta:
    "keywords": "typescript, conversion, migration, binding generation, d.ts"
---

# Convert from TypeScript

{bdg-success}`Native`

## Goal

Migrate TypeScript or JavaScript code to ReScript using the plugin's automatic conversion features, and generate ReScript bindings from `.d.ts` type definition files.

## Steps

### Method 1: Paste and Convert

#### 1. Copy TypeScript/JSX code

Copy any TypeScript, JavaScript, or JSX/TSX code to your clipboard. For example:

```typescript
import React, { useState } from "react";

interface Props {
  name: string;
  count: number;
}

const Greeting: React.FC<Props> = ({ name, count }) => {
  const [visible, setVisible] = useState(true);
  return visible ? <div className="greeting">Hello, {name}!</div> : null;
};
```

#### 2. Paste into a `.res` file

Press {kbd}`Ctrl+V` ({kbd}`Cmd+V` on macOS) to paste into a `.res` file. The plugin automatically detects TypeScript/JavaScript content and converts it to ReScript:

```rescript
// interface Props { ... } — commented out (manual conversion needed)

@react.component
let make = (~name: string, ~count: int) => {
  let (visible, setVisible) = React.useState(() => true)
  visible ? <div className="greeting"> {React.string("Hello, " ++ name ++ "!")} </div> : React.null
}
```

The converter handles:

- **JSX patterns** --- `className`, `onClick`, etc. are preserved as-is (ReScript JSX uses the same names)
- **Type annotations** --- TypeScript types are removed (ReScript infers types)
- **`interface` / `enum` declarations** --- Commented out with a note for manual conversion
- **Import statements** --- Removed (ReScript uses `open` instead)

#### 3. Review and adjust

The automatic conversion is a starting point. Review the output and adjust:

- Replace commented-out `interface` blocks with ReScript `type` declarations
- Add `open` statements for modules you need
- Fix any syntax that the converter could not handle automatically

### Method 2: Generate Bindings from `.d.ts`

For TypeScript libraries you want to call from ReScript, generate `external` bindings from `.d.ts` files.

#### 1. Open or select a `.d.ts` file

In the **Project** panel, right-click on a `.d.ts` file (e.g., `node_modules/some-lib/index.d.ts`).

#### 2. Generate ReScript Binding

Select **Generate > ReScript Binding** from the context menu. The plugin parses the TypeScript type definitions and generates corresponding ReScript `external` declarations:

```rescript
// Generated from index.d.ts
@module("some-lib")
external createClient: (~apiKey: string, ~options: options=?) => client = "createClient"

type options = {
  timeout?: int,
  retries?: int,
}

type client = {
  fetch: (~url: string) => promise<response>,
}
```

#### 3. Save and refine

Save the generated binding to a `.res` file and refine as needed. The generator handles common patterns but may require adjustments for complex TypeScript generics or overloads.

## Expected Result

- TypeScript/JSX code is converted to valid ReScript with minimal manual editing
- `.d.ts` type definitions are transformed into usable `external` bindings

## Tips

- For large codebases, convert file by file rather than all at once
- The paste converter works best with self-contained code snippets
- Use {kbd}`Ctrl+Z` ({kbd}`Cmd+Z`) immediately after paste to revert if the conversion is not useful

## Related Features

- [Code Editing](../features/code-editing.md) --- Paste conversion details
- [Advanced Features](../features/advanced.md) --- `.d.ts` binding generation

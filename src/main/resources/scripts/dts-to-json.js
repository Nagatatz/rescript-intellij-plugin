#!/usr/bin/env node
// dts-to-json.js — Parse a .d.ts file using the TypeScript Compiler API
// and output a JSON representation of its declarations to stdout.
//
// Usage: node dts-to-json.js <path-to-dts-file>
// Environment: TS_PATH — path to the typescript package directory

"use strict";

const path = require("path");
const fs = require("fs");

const tsPath = process.env.TS_PATH;
if (!tsPath) {
  process.stderr.write("Error: TS_PATH environment variable not set\n");
  process.exit(1);
}

let ts;
try {
  ts = require(path.join(tsPath, "lib", "typescript.js"));
} catch (e) {
  process.stderr.write(`Error: Cannot load TypeScript from ${tsPath}: ${e.message}\n`);
  process.exit(1);
}

const filePath = process.argv[2];
if (!filePath) {
  process.stderr.write("Error: No input file specified\n");
  process.exit(1);
}

if (!fs.existsSync(filePath)) {
  process.stderr.write(`Error: File not found: ${filePath}\n`);
  process.exit(1);
}

// Create a compiler host and program
const compilerOptions = {
  target: ts.ScriptTarget.ESNext,
  module: ts.ModuleKind.ESNext,
  declaration: true,
  strict: true,
};

const program = ts.createProgram([filePath], compilerOptions);
const sourceFile = program.getSourceFile(filePath);
const checker = program.getTypeChecker();

if (!sourceFile) {
  process.stderr.write(`Error: Could not parse ${filePath}\n`);
  process.exit(1);
}

const errors = [];
const declarations = [];

// Infer module name from file name
const baseName = path.basename(filePath, path.extname(filePath));
const moduleName = baseName.replace(/\.d$/, "");

// Convert a TypeScript type to our JSON type node
function convertType(type, node) {
  if (!type) return { kind: "unknown", text: "" };

  // String literal type
  if (type.isStringLiteral()) {
    return { kind: "stringLiteral", value: type.value };
  }

  // Numeric literal type
  if (type.isNumberLiteral()) {
    return { kind: "numericLiteral", value: String(type.value) };
  }

  const flags = type.getFlags();

  // Primitive types
  if (flags & ts.TypeFlags.String) return { kind: "primitive", name: "string" };
  if (flags & ts.TypeFlags.Number) return { kind: "primitive", name: "number" };
  if (flags & ts.TypeFlags.Boolean) return { kind: "primitive", name: "boolean" };
  if (flags & ts.TypeFlags.Void) return { kind: "primitive", name: "void" };
  if (flags & ts.TypeFlags.Undefined) return { kind: "primitive", name: "undefined" };
  if (flags & ts.TypeFlags.Null) return { kind: "primitive", name: "null" };
  if (flags & ts.TypeFlags.Never) return { kind: "primitive", name: "never" };
  if (flags & ts.TypeFlags.Any) return { kind: "primitive", name: "any" };
  if (flags & ts.TypeFlags.Unknown) return { kind: "primitive", name: "unknown" };
  if (flags & ts.TypeFlags.BigInt) return { kind: "primitive", name: "bigint" };
  if (flags & ts.TypeFlags.ESSymbol) return { kind: "primitive", name: "symbol" };

  // Boolean literal (true/false)
  if (flags & ts.TypeFlags.BooleanLiteral) return { kind: "primitive", name: "boolean" };

  // Union type
  if (type.isUnion()) {
    // Boolean is often represented as true | false union
    const types = type.types;
    if (types.length === 2 &&
        types.every(t => t.getFlags() & ts.TypeFlags.BooleanLiteral)) {
      return { kind: "primitive", name: "boolean" };
    }
    return {
      kind: "union",
      types: types.map(t => convertType(t, node)),
    };
  }

  // Intersection type
  if (type.isIntersection()) {
    return {
      kind: "intersection",
      types: type.types.map(t => convertType(t, node)),
    };
  }

  // Tuple type
  if (checker.isTupleType(type)) {
    const typeArgs = checker.getTypeArguments(type);
    return {
      kind: "tuple",
      elements: typeArgs.map(t => convertType(t, node)),
    };
  }

  // Array type
  if (checker.isArrayType(type)) {
    const typeArgs = checker.getTypeArguments(type);
    return {
      kind: "array",
      elementType: typeArgs.length > 0 ? convertType(typeArgs[0], node) : { kind: "primitive", name: "any" },
    };
  }

  // Function / callable type
  const callSignatures = type.getCallSignatures();
  if (callSignatures.length > 0 && !type.getProperties().length) {
    const sig = callSignatures[0];
    return {
      kind: "function",
      parameters: sig.parameters.map(p => convertParameter(p)),
      returnType: convertType(checker.getReturnTypeOfSignature(sig), node),
    };
  }

  // Object type with symbol (reference)
  if (type.symbol) {
    const name = type.symbol.getName();

    // Check for generic type arguments
    const typeArgs = type.typeArguments || [];

    // Index signature check (Record-like)
    if (name === "__type" || name === "__object") {
      // Check for index signatures
      const indexType = type.getStringIndexType();
      if (indexType) {
        return {
          kind: "indexSignature",
          keyType: { kind: "primitive", name: "string" },
          valueType: convertType(indexType, node),
        };
      }

      // Object literal with members
      const members = [];
      for (const prop of type.getProperties()) {
        const propType = checker.getTypeOfSymbolAtLocation(prop, node || sourceFile);
        members.push({
          name: prop.getName(),
          type: convertType(propType, node),
          optional: !!(prop.getFlags() & ts.SymbolFlags.Optional),
          readonly: false,
        });
      }
      if (members.length > 0) {
        return { kind: "objectLiteral", members };
      }
    }

    if (typeArgs.length > 0) {
      return {
        kind: "reference",
        name: name,
        typeArguments: typeArgs.map(t => convertType(t, node)),
      };
    }

    // Simple reference (e.g., Date, RegExp, custom types)
    if (name !== "__type" && name !== "__object") {
      return { kind: "reference", name, typeArguments: [] };
    }
  }

  // Fallback
  const text = checker.typeToString(type);
  return { kind: "unknown", text };
}

function convertParameter(symbol) {
  const decl = symbol.valueDeclaration;
  const type = checker.getTypeOfSymbolAtLocation(symbol, decl || sourceFile);
  const optional = decl && ts.isParameter(decl) ? !!decl.questionToken || !!decl.initializer : false;
  return {
    name: symbol.getName(),
    type: convertType(type, decl),
    optional,
  };
}

// Visit top-level declarations
function visit(node) {
  // Skip non-exported declarations in module files
  const isExported = !!(ts.getCombinedModifierFlags(node) & ts.ModifierFlags.Export) ||
                     sourceFile.isDeclarationFile;

  try {
    // Function declaration
    if (ts.isFunctionDeclaration(node) && node.name) {
      const sig = checker.getSignatureFromDeclaration(node);
      if (sig) {
        declarations.push({
          kind: "function",
          name: node.name.text,
          exported: isExported,
          parameters: sig.parameters.map(p => convertParameter(p)),
          returnType: convertType(checker.getReturnTypeOfSignature(sig), node),
        });
      }
    }

    // Interface declaration
    else if (ts.isInterfaceDeclaration(node)) {
      const type = checker.getTypeAtLocation(node);
      const members = [];
      for (const prop of type.getProperties()) {
        const propType = checker.getTypeOfSymbolAtLocation(prop, node);
        members.push({
          name: prop.getName(),
          type: convertType(propType, node),
          optional: !!(prop.getFlags() & ts.SymbolFlags.Optional),
          readonly: false,
        });
      }
      declarations.push({
        kind: "interface",
        name: node.name.text,
        exported: isExported,
        members,
      });
    }

    // Type alias declaration
    else if (ts.isTypeAliasDeclaration(node)) {
      const type = checker.getTypeAtLocation(node);
      declarations.push({
        kind: "typeAlias",
        name: node.name.text,
        exported: isExported,
        type: convertType(type, node),
      });
    }

    // Variable declaration (const/let/var)
    else if (ts.isVariableStatement(node)) {
      for (const decl of node.declarationList.declarations) {
        if (ts.isIdentifier(decl.name)) {
          const type = checker.getTypeAtLocation(decl);
          declarations.push({
            kind: "variable",
            name: decl.name.text,
            exported: isExported,
            type: convertType(type, decl),
            isConst: !!(node.declarationList.flags & ts.NodeFlags.Const),
          });
        }
      }
    }

    // Enum declaration
    else if (ts.isEnumDeclaration(node)) {
      const members = [];
      let isStringEnum = false;
      for (const member of node.members) {
        const name = ts.isIdentifier(member.name) ? member.name.text : String(member.name);
        const constValue = checker.getConstantValue(member);
        if (typeof constValue === "string") {
          isStringEnum = true;
          members.push({ name, value: constValue });
        } else if (typeof constValue === "number") {
          members.push({ name, value: String(constValue) });
        } else {
          members.push({ name, value: null });
        }
      }
      declarations.push({
        kind: "enum",
        name: node.name.text,
        exported: isExported,
        members,
        isStringEnum,
      });
    }

    // Class declaration
    else if (ts.isClassDeclaration(node) && node.name) {
      const type = checker.getTypeAtLocation(node);
      const constructors = [];
      const methods = [];
      const properties = [];

      for (const member of node.members) {
        if (ts.isConstructorDeclaration(member)) {
          const sig = checker.getSignatureFromDeclaration(member);
          if (sig) {
            constructors.push({
              parameters: sig.parameters.map(p => convertParameter(p)),
            });
          }
        } else if (ts.isMethodDeclaration(member) && member.name) {
          const sig = checker.getSignatureFromDeclaration(member);
          if (sig) {
            const methodName = ts.isIdentifier(member.name) ? member.name.text : String(member.name);
            methods.push({
              name: methodName,
              parameters: sig.parameters.map(p => convertParameter(p)),
              returnType: convertType(checker.getReturnTypeOfSignature(sig), member),
            });
          }
        } else if (ts.isPropertyDeclaration(member) && member.name) {
          const propName = ts.isIdentifier(member.name) ? member.name.text : String(member.name);
          const propType = checker.getTypeAtLocation(member);
          properties.push({
            name: propName,
            type: convertType(propType, member),
            optional: !!member.questionToken,
            readonly: !!(ts.getCombinedModifierFlags(member) & ts.ModifierFlags.Readonly),
          });
        }
      }

      declarations.push({
        kind: "class",
        name: node.name.text,
        exported: isExported,
        constructors,
        methods,
        properties,
      });
    }
  } catch (e) {
    errors.push(`Error processing ${node.kind}: ${e.message}`);
  }
}

ts.forEachChild(sourceFile, visit);

const result = {
  fileName: path.basename(filePath),
  moduleName,
  declarations,
  errors,
};

process.stdout.write(JSON.stringify(result, null, 2));

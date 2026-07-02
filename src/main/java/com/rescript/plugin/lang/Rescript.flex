package com.rescript.plugin.lang;

import com.intellij.lexer.*;
import com.intellij.psi.tree.*;
import com.rescript.plugin.lang.RescriptTokenTypes;

import static com.intellij.psi.TokenType.*;

@SuppressWarnings("ALL")
%%

%{
    private int tokenStartIndex;
    private int commentDepth;
    private boolean inJsxOpenTag = false;
    private int jsxAttrBraceDepth = 0;

    private void tokenStart() {
        tokenStartIndex = zzStartRead;
    }

    private void tokenEnd() {
        zzStartRead = tokenStartIndex;
    }
%}

%public
%class RescriptFlexLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

EOL=\n|\r|\r\n
WHITE_SPACE_CHAR=[\ \t\f]
WHITE_SPACE={WHITE_SPACE_CHAR}+

LOWERCASE=[a-z_]
UPPERCASE=[A-Z]
IDENTCHAR=[A-Za-z_0-9']

DECIMAL=[0-9]
DECIMAL_SEP=[0-9_]
HEXA=[0-9A-Fa-f]
HEXA_SEP=[0-9A-Fa-f_]
OCTAL=[0-7]
OCTAL_SEP=[0-7_]

DECIMAL_LITERAL={DECIMAL} {DECIMAL_SEP}*
HEXA_LITERAL="0" [xX] {HEXA} {HEXA_SEP}*
OCT_LITERAL="0" [oO] {OCTAL} {OCTAL_SEP}*
BIN_LITERAL="0" [bB] [0-1] [0-1_]*
INT_LITERAL= {DECIMAL_LITERAL} | {HEXA_LITERAL} | {OCT_LITERAL} | {BIN_LITERAL}
FLOAT_LITERAL={DECIMAL} {DECIMAL_SEP}* ("." {DECIMAL_SEP}* )? ([eE] [+-]? {DECIMAL} {DECIMAL_SEP}* )?
HEXA_FLOAT_LITERAL="0" [xX] {HEXA} {HEXA_SEP}* ("." {HEXA_SEP}* )? ([pP] [+-]? {DECIMAL} {DECIMAL_SEP}* )?
LITERAL_MODIFIER=[G-Zg-z]

LIDENT={LOWERCASE}{IDENTCHAR}*

ESCAPE_BACKSLASH="\\\\"
ESCAPE_SINGLE_QUOTE="\\'"
ESCAPE_LF="\\n"
ESCAPE_TAB="\\t"
ESCAPE_BACKSPACE="\\b"
ESCAPE_CR="\\r"
ESCAPE_QUOTE="\\\""
ESCAPE_DECIMAL="\\" {DECIMAL} {DECIMAL} {DECIMAL}
ESCAPE_HEXA="\\x" {HEXA} {HEXA}
ESCAPE_OCTAL="\\o" [0-3] {OCTAL} {OCTAL}
ESCAPE_CHAR= {ESCAPE_BACKSLASH} | {ESCAPE_SINGLE_QUOTE} | {ESCAPE_LF} | {ESCAPE_TAB} | {ESCAPE_BACKSPACE } | { ESCAPE_CR } | { ESCAPE_QUOTE } | {ESCAPE_DECIMAL} | {ESCAPE_HEXA} | {ESCAPE_OCTAL}

%state INITIAL
%state AFTER_IDENT
%state IN_LOWER_DECLARATION
%state IN_TEMPLATE
%state IN_STRING
%state IN_ML_COMMENT
%state IN_SL_COMMENT
%state IN_JSX_TAG_NAME
%state IN_ANNOTATION

%%

<YYINITIAL>  {
      [^]   { yybegin(INITIAL); yypushback(1); }
}

<INITIAL> {
    {WHITE_SPACE} { return WHITE_SPACE; }
    {EOL}         { return RescriptTokenTypes.EOL; }

    "async"       { return RescriptTokenTypes.ASYNC; }
    "await"       { return RescriptTokenTypes.AWAIT; }
    "and"         { return RescriptTokenTypes.AND; }
    "as"          { return RescriptTokenTypes.AS; }
    "assert"      { return RescriptTokenTypes.ASSERT; }
    "begin"       { return RescriptTokenTypes.BEGIN; }
    "catch"       { return RescriptTokenTypes.CATCH; }
    "class"       { return RescriptTokenTypes.CLASS; }
    "constraint"  { return RescriptTokenTypes.CONSTRAINT; }
    "do"          { return RescriptTokenTypes.DO; }
    "done"        { return RescriptTokenTypes.DONE; }
    "downto"      { return RescriptTokenTypes.DOWNTO; }
    "else"        { return RescriptTokenTypes.ELSE; }
    "end"         { return RescriptTokenTypes.END; }
    "exception"   { return RescriptTokenTypes.EXCEPTION; }
    "external"    { yybegin(IN_LOWER_DECLARATION); return RescriptTokenTypes.EXTERNAL; }
    "for"         { return RescriptTokenTypes.FOR; }
    "functor"     { return RescriptTokenTypes.FUNCTOR; }
    "if"          { return RescriptTokenTypes.IF; }
    "in"          { return RescriptTokenTypes.IN; }
    "include"     { return RescriptTokenTypes.INCLUDE; }
    "inherit"     { return RescriptTokenTypes.INHERIT; }
    "initializer" { return RescriptTokenTypes.INITIALIZER; }
    "lazy"        { return RescriptTokenTypes.LAZY; }
    "let"         { yybegin(IN_LOWER_DECLARATION); return RescriptTokenTypes.LET; }
    "list"        { return RescriptTokenTypes.LIST; }
    "dict"        { return RescriptTokenTypes.DICT; }
    "module"      { return RescriptTokenTypes.MODULE; }
    "mutable"     { return RescriptTokenTypes.MUTABLE; }
    "new"         { return RescriptTokenTypes.NEW; }
    "nonrec"      { return RescriptTokenTypes.NONREC; }
    "object"      { return RescriptTokenTypes.OBJECT; }
    "of"          { return RescriptTokenTypes.OF; }
    "open"        { return RescriptTokenTypes.OPEN; }
    "or"          { return RescriptTokenTypes.OR; }
    "pub"         { return RescriptTokenTypes.PUB; }
    "pri"         { return RescriptTokenTypes.PRI; }
    "raw"         { return RescriptTokenTypes.RAW; }
    "ffi"         { return RescriptTokenTypes.FFI; }
    "rec"         { return RescriptTokenTypes.REC; }
    "sig"         { return RescriptTokenTypes.SIG; }
    "struct"      { return RescriptTokenTypes.STRUCT; }
    "switch"      { return RescriptTokenTypes.SWITCH; }
    "then"        { return RescriptTokenTypes.THEN; }
    "try"         { return RescriptTokenTypes.TRY; }
    "type"        { return RescriptTokenTypes.TYPE; }
    "unpack"      { return RescriptTokenTypes.UNPACK; }
    "val"         { return RescriptTokenTypes.VAL; }
    "virtual"     { return RescriptTokenTypes.VIRTUAL; }
    "when"        { return RescriptTokenTypes.WHEN; }
    "while"       { return RescriptTokenTypes.WHILE; }
    "with"        { return RescriptTokenTypes.WITH; }

    "mod"         { return RescriptTokenTypes.MOD; }
    "land"        { return RescriptTokenTypes.LAND; }
    "lor"         { return RescriptTokenTypes.LOR; }
    "lxor"        { return RescriptTokenTypes.LXOR; }
    "lsl"         { return RescriptTokenTypes.LSL; }
    "lsr"         { return RescriptTokenTypes.LSR; }
    "asr"         { return RescriptTokenTypes.ASR; }

    "unit"        { return RescriptTokenTypes.UNIT; }
    "ref"         { return RescriptTokenTypes.REF; }
    "raise"       { return RescriptTokenTypes.RAISE; }
    "method"      { return RescriptTokenTypes.METHOD; }
    "private"     { return RescriptTokenTypes.PRIVATE; }
    "match"       { return RescriptTokenTypes.MATCH; }

    "option"    { yybegin(AFTER_IDENT); return RescriptTokenTypes.OPTION; }
    "None"      { yybegin(AFTER_IDENT); return RescriptTokenTypes.NONE; }
    "Some"      { yybegin(AFTER_IDENT); return RescriptTokenTypes.SOME; }

    "false"     { yybegin(AFTER_IDENT); return RescriptTokenTypes.BOOL_VALUE; }
    "true"      { yybegin(AFTER_IDENT); return RescriptTokenTypes.BOOL_VALUE; }

    "_"         { yybegin(AFTER_IDENT); return RescriptTokenTypes.UNDERSCORE; }

    "'" ( {ESCAPE_CHAR} | . ) "'"    { yybegin(AFTER_IDENT); return RescriptTokenTypes.CHAR_VALUE; }
    {LIDENT}                         { yybegin(AFTER_IDENT); return RescriptTokenTypes.LIDENT; }
    {UPPERCASE}{IDENTCHAR}*          { yybegin(AFTER_IDENT); return RescriptTokenTypes.UIDENT; }
    {INT_LITERAL}{LITERAL_MODIFIER}? { yybegin(AFTER_IDENT); return RescriptTokenTypes.INT_VALUE; }
    ({FLOAT_LITERAL} | {HEXA_FLOAT_LITERAL}){LITERAL_MODIFIER}? { yybegin(AFTER_IDENT); return RescriptTokenTypes.FLOAT_VALUE; }
    "'"{LOWERCASE}{IDENTCHAR}*       { yybegin(AFTER_IDENT); return RescriptTokenTypes.TYPE_ARGUMENT; }
    "#"{UPPERCASE}{IDENTCHAR}*       { yybegin(AFTER_IDENT); return RescriptTokenTypes.POLY_VARIANT; }
    "#"{LOWERCASE}{IDENTCHAR}*       { yybegin(AFTER_IDENT); return RescriptTokenTypes.POLY_VARIANT; }

    "`"  { yybegin(IN_TEMPLATE); return RescriptTokenTypes.JS_STRING_OPEN; }
    "\"" { yybegin(IN_STRING); tokenStart(); }
    "/*" { yybegin(IN_ML_COMMENT); commentDepth = 1; tokenStart(); }
    "//" { yybegin(IN_SL_COMMENT); tokenStart(); }

    "++"  { return RescriptTokenTypes.STRING_CONCAT; }
    "##"  { return RescriptTokenTypes.SHARPSHARP; }
    "::"  { return RescriptTokenTypes.SHORTCUT; }
    "=>"  { return RescriptTokenTypes.ARROW; }
    "->"  { return RescriptTokenTypes.RIGHT_ARROW; }
    "<-"  { return RescriptTokenTypes.LEFT_ARROW; }
    "|>"  { return RescriptTokenTypes.PIPE_FORWARD; }
    "</"  { inJsxOpenTag = false; jsxAttrBraceDepth = 0; yybegin(IN_JSX_TAG_NAME); return RescriptTokenTypes.TAG_LT_SLASH; }
    "/>"  { inJsxOpenTag = false; jsxAttrBraceDepth = 0; return RescriptTokenTypes.TAG_AUTO_CLOSE; }

    "===" { return RescriptTokenTypes.EQEQEQ; }
    "=="  { return RescriptTokenTypes.EQEQ; }
    "="   { return RescriptTokenTypes.EQ; }
    "!==" { return RescriptTokenTypes.NOT_EQEQ; }
    "!="  { return RescriptTokenTypes.NOT_EQ; }
    ":="  { return RescriptTokenTypes.COLON_EQ; }
    ":>"  { return RescriptTokenTypes.COLON_GT; }
    "<="  { return RescriptTokenTypes.LT_OR_EQUAL; }
    "&&"  { return RescriptTokenTypes.L_AND; }
    "||"  { return RescriptTokenTypes.L_OR; }

    ","   { return RescriptTokenTypes.COMMA; }
    ":"   { return RescriptTokenTypes.COLON; }
    ";"   { return RescriptTokenTypes.SEMI; }
    "'"   { return RescriptTokenTypes.SINGLE_QUOTE; }
    "..." { return RescriptTokenTypes.DOTDOTDOT; }
    ".."  { return RescriptTokenTypes.DOTDOT; }
    "."   { return RescriptTokenTypes.DOT; }
    "|"   { return RescriptTokenTypes.PIPE; }
    "("   { return RescriptTokenTypes.LPAREN; }
    ")"   { yybegin(AFTER_IDENT); return RescriptTokenTypes.RPAREN; }
    "{"   { if (inJsxOpenTag) { jsxAttrBraceDepth++; } return RescriptTokenTypes.LBRACE; }
    "}"   { if (inJsxOpenTag && jsxAttrBraceDepth > 0) { jsxAttrBraceDepth--; } return RescriptTokenTypes.RBRACE; }
    "["   { return RescriptTokenTypes.LBRACKET; }
    "]"   { yybegin(AFTER_IDENT); return RescriptTokenTypes.RBRACKET; }
    "@"   { yybegin(IN_ANNOTATION); return RescriptTokenTypes.ARROBASE; }
    "#"   { return RescriptTokenTypes.SHARP; }
    "?"   { return RescriptTokenTypes.QUESTION_MARK; }
    "!"   { return RescriptTokenTypes.EXCLAMATION_MARK; }
    "~"   { return RescriptTokenTypes.TILDE; }
    "&"   { return RescriptTokenTypes.AMPERSAND; }

    "<" / [A-Za-z]  { yybegin(IN_JSX_TAG_NAME); return RescriptTokenTypes.TAG_LT; }
    "<"              { return RescriptTokenTypes.LT; }
    ">"              { if (inJsxOpenTag && jsxAttrBraceDepth == 0) { inJsxOpenTag = false; return RescriptTokenTypes.TAG_GT; } return RescriptTokenTypes.GT; }

    "\^"  { return RescriptTokenTypes.CARRET; }
    "+."  { return RescriptTokenTypes.PLUSDOT; }
    "-."  { return RescriptTokenTypes.MINUSDOT; }
    "/."  { return RescriptTokenTypes.SLASHDOT; }
    "*."  { return RescriptTokenTypes.STARDOT; }
    "+"   { return RescriptTokenTypes.PLUS; }
    "-"   { return RescriptTokenTypes.MINUS; }
    "/"   { return RescriptTokenTypes.SLASH; }
    "*"   { return RescriptTokenTypes.STAR; }
    "%"   { return RescriptTokenTypes.PERCENT; }
    "\\"  { return RescriptTokenTypes.BACKSLASH; }
}

<IN_JSX_TAG_NAME> {
    {LOWERCASE}{IDENTCHAR}*   { return RescriptTokenTypes.JSX_TAG_NAME; }
    {UPPERCASE}{IDENTCHAR}*   { return RescriptTokenTypes.JSX_COMPONENT_NAME; }
    "."                       { return RescriptTokenTypes.DOT; }
    ">"                       { yybegin(INITIAL); return RescriptTokenTypes.TAG_GT; }
    [^]                       { inJsxOpenTag = true; jsxAttrBraceDepth = 0; yybegin(INITIAL); yypushback(1); }
}

<IN_ANNOTATION> {
    ({LIDENT} | {UPPERCASE}{IDENTCHAR}*) ("." ({LIDENT} | {UPPERCASE}{IDENTCHAR}*))* { yybegin(INITIAL); return RescriptTokenTypes.ANNOTATION_NAME; }
    [^]   { yybegin(INITIAL); yypushback(1); }
}

// After an identifier/value token, "<" followed by a letter is NOT JSX
// (it is a type parameter or comparison). On EOL, reset to INITIAL
// so that JSX on the next line is correctly recognized.
<AFTER_IDENT> {
    {WHITE_SPACE}    { return WHITE_SPACE; }
    {EOL}            { yybegin(INITIAL); return RescriptTokenTypes.EOL; }
    "<" / [A-Za-z]   { yybegin(INITIAL); return RescriptTokenTypes.LT; }
    [^]              { yybegin(INITIAL); yypushback(1); }
}

<IN_LOWER_DECLARATION> {
  "rec"         { return RescriptTokenTypes.REC; }
  "\\"          { return RescriptTokenTypes.BACKSLASH; }
  "_"           { return RescriptTokenTypes.UNDERSCORE; }
  {LIDENT}      { yybegin(INITIAL); return RescriptTokenTypes.LIDENT; }
  {WHITE_SPACE} { return WHITE_SPACE; }
  {EOL}         { return RescriptTokenTypes.EOL; }
  <<EOF>>       { yybegin(INITIAL); }
  .             { yybegin(INITIAL); yypushback(1); tokenEnd();}
}

<IN_TEMPLATE> {
    {WHITE_SPACE} { return WHITE_SPACE; }
    "$"           { return RescriptTokenTypes.DOLLAR; }
    "{"           { return RescriptTokenTypes.LBRACE; }
    "}"           { return RescriptTokenTypes.RBRACE; }
    "`"           { yybegin(AFTER_IDENT); return RescriptTokenTypes.JS_STRING_CLOSE; }
    // A newline is literal template content; emit it as STRING_VALUE so every
    // character stays covered by a token. An empty action here would leave a
    // 1-char lexer gap when the EOL sits between `}`/`$` boundaries.
    {EOL}         { return RescriptTokenTypes.STRING_VALUE; }
    <<EOF>>       { yybegin(INITIAL); }
    ([^`{}$])+    { return RescriptTokenTypes.STRING_VALUE; }
}

<IN_STRING> {
    "\"" { yybegin(AFTER_IDENT); tokenEnd(); return RescriptTokenTypes.STRING_VALUE; }
    "\\" {EOL} ([ \t] *) { }
    "\\" [\\\'\"ntbr ] { }
    "\\" [0-9] [0-9] [0-9] { }
    "\\" "o" [0-3] [0-7] [0-7] { }
    "\\" "x" [0-9a-fA-F] [0-9a-fA-F] { }
    "\\" . { }
    {EOL}  { }
    . { }
    <<EOF>> { yybegin(AFTER_IDENT); tokenEnd(); return RescriptTokenTypes.STRING_VALUE; }
}

<IN_ML_COMMENT> {
    // ReScript block comments are C/JS-style: they nest but do not lex string
    // literals specially, so `*/` always closes (respecting nesting depth).
    // A double-quote is an ordinary comment character — treating it as a string
    // toggle previously latched on an odd quote count and swallowed the rest of
    // the file.
    "/*" { commentDepth += 1; }
    "*/" { commentDepth -= 1; if(commentDepth == 0) { yybegin(INITIAL); tokenEnd(); return RescriptTokenTypes.MULTI_COMMENT; } }
    . | {EOL} { }
    <<EOF>> { yybegin(INITIAL); tokenEnd(); return RescriptTokenTypes.MULTI_COMMENT; }
}

<IN_SL_COMMENT> {
    .         { }
    {EOL}     { yybegin(INITIAL); tokenEnd(); return RescriptTokenTypes.SINGLE_COMMENT; }
    <<EOF>>   { yybegin(INITIAL); tokenEnd(); return RescriptTokenTypes.SINGLE_COMMENT; }
}

[^] { return BAD_CHARACTER; }

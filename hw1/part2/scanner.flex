/* ------------------------------ User Code --------------------------------- */

import java_cup.runtime.*;


%%
/* ------------------------ Options and Declarations ------------------------ */

%class Scanner
%line
%column
%cup
%unicode

// The following two methods create java_cup.runtime.Symbol objects
%{
    StringBuffer strBuffer = new StringBuffer();

    private Symbol symbol(int type) {
       return new Symbol(type, yyline, yycolumn);
    }

    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

// Macro Declarations - Regular Expressions
LineTerminator  = \r|\n|\r\n

WhiteSpace = {LineTerminator} | [ \t\f]

SpecialSym = [)]{WhiteSpace}*[{]

Identifier = [:jletter:] [:jletterdigit:]*

// States Declarations
%state STRING


%%
/* ---------------------------- Lexical Rules ------------------------------- */

<YYINITIAL> {
    "+"        { return symbol(sym.CONCAT);  }
    "prefix"   { return symbol(sym.PREFIX);  }
    "suffix"   { return symbol(sym.SUFFIX);  }
    "if"       { return symbol(sym.IF);      }
    "else"     { return symbol(sym.ELSE);    }
    "("        { return symbol(sym.LPAREN);  }
    ")"        { return symbol(sym.RPAREN);  }
    "}"        { return symbol(sym.RBRACE);  }
    ","        { return symbol(sym.COMMA);   }
    \"         { strBuffer.setLength(0); 
                 yybegin(STRING);            }
    {WhiteSpace}  { /* Do nothing. Just skip what was found. */ }
    {SpecialSym}  { return symbol(sym.SPECIAL);                 }
    {Identifier}  { return symbol(sym.IDENTIFIER, yytext());    }
}

<STRING> {
    [^\n\r\"\\]+  { strBuffer.append(yytext());                           }
    \\t           { strBuffer.append('\t');                               }
    \\n           { strBuffer.append('\n');                               }
    \\r           { strBuffer.append('\r');                               } 
    \\\"          { strBuffer.append('\"');                               }
    \\            { strBuffer.append('\\');                               }
    \"            { yybegin(YYINITIAL);
                    return symbol(sym.STR_LITERAL, strBuffer.toString()); }
}

[^]           { throw new Error("Illegal character <" + yytext() + ">"); }

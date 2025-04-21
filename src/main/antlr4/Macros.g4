grammar Macros;

body: (macros | text | quote | slash)+;

emptyMacros: macrosStart macrosEnd | macrosStart QUO QUO macrosEnd;

macros: emptyMacros | macrosStart macroParams macrosEnd ;

macroParams: macroParam (SEPARATOR macroParam)+ | macroParam;

macroParam : QUO (macroArg)? QUO | macroArg? ;

macroArg:  (text | macros)+ ;

macrosStart: MACROS;

macrosEnd: CLOSE;

quote: QUO;

slash: SLASH;

SEPARATOR : WS* COMMA WS*;
MACROS: MACROS_MARKER MACROS_NAME OPEN;
MACROS_NAME: (LETTER | DIGIT | LOW_LINE)+;

text:
ESC+?
| TEXT
| MACROS_NAME
| MACROS_MARKER
| OPEN
| CLOSE
| SEPARATOR
;

MACROS_MARKER: '#' | '$';
OPEN : '(';
CLOSE: ')';
QUO: QUOTE;
ESC: ESC_CHAR (ESC_SYMB | 'n' | 't' | 'r');
TEXT: ~[$#()'\\,]+;
SLASH: '\\';

fragment LETTER : [a-zA-Z];
fragment SPECIAL: [-_+$#:;*|];
fragment LOW_LINE: [_];
fragment DIGIT  : [0-9] ;
fragment WS     : [ \r\t];
fragment ESC_CHAR : '\\';
fragment ESC_SYMB : ([&$"'\\\][<>]);
fragment COMMA   : ',';
fragment QUOTE   :'\'';
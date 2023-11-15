import compilerTools.Token;

%%
%class Lexer
%type Token
%line
%column
%{
    private Token token(String lexeme, String lexicalComp, int line, int column){
    return new Token (lexeme, lexicalComp, line+1, column+1);
  }
%}
/* Variables basicas de comentrios y espacios */
TerminadorDeLinea = \r|\n|\r|\n
EntradaDeCaracter = [^ \r\n]
EspacioEnBlanco = {TerminadorDeLinea} | [ \t\f]
ComentarioTradicional = "/*" [^*] ~"*/" | "/*" "*"+ "/"
FinDeLineaComentario = "//" {EntradaDeCaracter}*  {TerminadorDeLinea}?
ContenidoComentario = ( [^*] | \*+ [^/*] )*
ComentarioDeDocumentacion = "/**" {ContenidoComentario} "*"+ "/"

/* Comentario */
Comentario = {ComentarioTradicional} | {FinDeLineaComentario} | {ComentarioDeDocumentacion}

/* Identificadores */
Letra = [A-Za-zÑñ_ÁÉÍÓÚáéíóúüÜ]
Digito = [0-9]
Identificador = {Letra} ({Letra} | {Digito})*

/* Numero */
Numero = 0 | [1-9] [0-9]*
%%

/* Comentarios o espacios en blanco */
{Comentario} | {EspacioEnBlanco} { /*Ignorar*/ }

/* Identificador */
\${Identificador} {return token(yytext(), "IDENTIFICADOR", yyline, yycolumn); }

/* tipos de datos */
numero |
color { return token(yytext(), "TIPO_DATO", yyline, yycolumn); }

/*Numeros */
{Numero} {return token(yytext(), "NUMERO", yyline, yycolumn); }

/* Colores */
# [{Letra} | {Digito}] {6} {return token(yytext(), "NUMERO", yyline, yycolumn);}

/* Operadores de agrupacion */
"(" {return token(yytext(), "PARENTESIS_A", yyline, yycolumn);}
")" {return token(yytext(), "PARENTESIS_C", yyline, yycolumn);}
"{" {return token(yytext(), "LLAVE_A", yyline, yycolumn);}
"}" {return token(yytext(), "LLAVE_C", yyline, yycolumn);}

/* Signos de puntuacion */
"," {return token(yytext(), "COMA", yyline, yycolumn);}
";" {return token(yytext(), "PUNTOYCOMA", yyline, yycolumn);}

/* Operador de Asignacion*/
--> |
= {return token(yytext(), "OPDEASIGNACION", yyline, yycolumn);}

/* Operadores Aritmeticos*/
"+" |
"-" |
"*" |
"/" |
"%" {return token(yytext(), "OPDEARITMETICOS", yyline, yycolumn);}

/* Operadores Relacionales*/
"==" |
"!=" |
"<" |
">" |
"<=" |
">=" {return token(yytext(), "OPRELACIONALES", yyline, yycolumn);}

/* Movimiento */
adelante |
atras |
izquierda |
derecha |
norte |
sur |
este |
oeste {return token(yytext(), "MOVIMIENTO", yyline, yycolumn);}

/* Palabras Reservadas */
while |
do |
for |
println {return token(yytext(), "PALABRA_RESERVADA", yyline, yycolumn);}

/* Sentencias True */
if |
"else if" |
switch {return token(yytext(), "SENTENCIA_TRUE", yyline, yycolumn);}

/* Sentencias False */
else |
break {return token(yytext(), "SENTENCIA_FALSE", yyline, yycolumn);}

/* Sentencias Condiciones */
condicion1 |
condicion2 {return token(yytext(), "CONDICIONES", yyline, yycolumn);}

/* Pintar */
pintar {return token(yytext(), "PINTAR", yyline, yycolumn);}

/*Detener pintar*/
detenerPintar {return token(yytext(), "DETENER_PINTAR", yyline, yycolumn);}

/*Tomar*/
tomar |
poner {return token(yytext(), "TOMAR", yyline, yycolumn);}

/*Lanzar Moneda*/
lanzarMoneda {return token(yytext(), "LANZARMONEDA", yyline, yycolumn);}

/*REPETIR*/
repetir|
repetirMientras {return token(yytext(), "REPETIR", yyline, yycolumn);}

/*Detener Repetir*/
interrumpir {return token(yytext(), "DETENER_REPETIR", yyline, yycolumn);}

/*Estructura SI*/
si|
sino {return token(yytext(), "ESTRUCTURA_SI", yyline, yycolumn);}

/*Operadores Logicos*/
"&"|
"|" {return token(yytext(), "OPERADOR_LOGICO", yyline, yycolumn);}

/*FINAL*/
final {return token(yytext(), "FINAL", yyline, yycolumn);}

/*Numero Erroneo*/
0{Numero} {return token(yytext(), "ERROR", yyline, yycolumn);}

/*Identificador Erroneo*/
{Identificador} {return token(yytext(), "ERROR_2", yyline, yycolumn);}

. {return token(yytext(), "ERROR", yyline, yycolumn);}
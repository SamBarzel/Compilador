import compilerTools.TextColor;
import java.awt.Color;

%%
%class LexerColor
%type TextColor
%char
%{
private TextColor textColor(long start, int size, Color color) {
    return new TextColor ((int) start, size, color);

   }
%}
/* Variables basicas de comentarios y espacios */
TerminadorDeLinea = \r|\n|\r\n
EntradaDeCaracter = [^\r\n]
EspacioEnBlanco = {TerminadorDeLinea} | [ \t\f]
ComentarioTradicional = "/*" [^*] ~"*/" | "/*" "*"+ "/"
FinDeLineaComentario = "//" {EntradaDeCaracter}* {TerminadorDeLinea}?
ContenidoComentario = ( [^*] | \*+ [^/*] )*
ComentarioDeDocumentacion = "/**" {ContenidoComentario} "*"+ "/"

/*Comentario*/
Comentario = {ComentarioTradicional} | {FinDeLineaComentario} | {ComentarioDeDocumentacion}

/*Identificador */
Letra = [A-Za-zÑñ_ÁÉÍÓÚáéíóúüÜ]
Digito = [0-9]
Identificador = {Letra} ({Letra} | {Digito})*

/* Numero */
Numero = 0 | [1-9] [0-9]*
%%
/*Comentarios o espacios en blanco*/
{Comentario} {return textColor(yychar, yylength(), new Color(146,146,146));}
{EspacioEnBlanco} {/*Ignorar*/}

/*identificador*/
\${Identificador} {/*Ignorar*/}

/*tipo de dato*/
numero |
color {return textColor(yychar, yylength(), Color.red);}

/*Numeros*/
{Numero} {return textColor(yychar, yylength(), new Color(251, 140, 0));}

/*Colores*/
#[{Letra} | {Digito}] {6} {return textColor(yychar, yylength(), new Color(0, 255, 127));}

/*Operadores de agrupacion*/
"(" | ")" {return textColor(yychar, yylength(), Color.red);}

/*Signos de puntuacion*/
"," | ";" {return textColor(yychar, yylength(), new Color(0, 0, 0));}

/*Operador de Asignacion*/
--> {return textColor(yychar, yylength(), new Color(255, 215, 0));}

/*Palabras reservadas*/
While|
Do |
For |
Else |
If {return textColor(yychar, yylength(), new Color(128, 0, 12));}

/* Sentencias True */
if |
"else if" |
switch {return textColor(yychar, yylength(), new Color(235, 86, 54));}

/* Sentencias False */
else |
break {return textColor(yychar, yylength(), new Color(255, 159, 51));}

/* Sentencias Condiciones */
condicion1 |
condicion2 {return textColor(yychar, yylength(), new Color(40, 50, 0));}



/*Pintar*/
pintar {return textColor(yychar, yylength(), new Color(255, 255, 0));}

/*Detener Pintar*/
detenerPintar {return textColor(yychar, yylength(), new Color(255, 64, 129));}

/*Repetir*/
repetir|
repetirMientras {return textColor(yychar, yylength(), new Color(121, 107, 255));}

/*Detener repetir*/
interrumpir {return textColor(yychar, yylength(), new Color(255, 64, 129));}

/*Estructura si*/
si|
sino {return textColor(yychar, yylength(), new Color(48, 63, 129));}

/*Operadores Logicos*/
"&"|
"|" {return textColor(yychar, yylength(), new Color(112, 128, 144));}

/*Final*/
final {return textColor(yychar, yylength(), new Color(198, 40, 40));}

/*Numero erroneo*/
0{Numero} {/*Ignorar*/}

/*Identificador erroneo*/
{Identificador} {/*Ignorar*/}

. {/*Ignorar*/}



import compilerTools.CodeBlock;
import compilerTools.Directory;
import compilerTools.ErrorLSSL;
import compilerTools.Functions;
import compilerTools.Grammar;
import compilerTools.Production;
import compilerTools.TextColor;
import compilerTools.Token;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.Timer;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author rodri
 */
public class Compilador extends javax.swing.JFrame {

   private String title; // Primer variable de tipo string para el titulo
   private Directory directorio; // la 2da sera el directorio
   private ArrayList<Token> tokens; //Array List donde se guardan los tokens
   private ArrayList<ErrorLSSL> errors; /* ArrayList donde se guardan los errores
   pueden ser lexico, sintacticos, sementicos y logicos */
   private ArrayList<TextColor> textsColor; // Seguardaran los colores de las palabras reservada
   private Timer timerKeyReleased; //El uso de esta variable se utiliza para ejecutar
   //para que se coloren las palabras del editor de codigo
   private ArrayList<Production> identProd;// extrae los identificadores del analizador sintactico
   private HashMap<String, String> identificadores;// Se guardan los identificadores
   private boolean codeHasBeenCompiled = false;//indicara si el compilador si ya a sifdo compilado por defecto se pone el false

   
    public Compilador() { // Se crea el metodo init
        initComponents();
        init();//se crea el metodo llamado init
    }
    
    private void init (){
        title = "Compilador"; //se inicia la variable con el nombre del compilador 
        setLocationRelativeTo(null);// se ejecuta el metodo para centrar la ventana        
        setTitle(title); //Se ejecuta para ponerle titulo a la ventana 
        directorio = new Directory(this, jtpCode, title, ".comp");/*Se inicialara la variable directorio donde el primer parametro 
        es el jframe, editor de codigo, titulo, extencion de los archivos del codigo*/
        addWindowListener(new WindowAdapter(){
        @Override
        public void windowClosing(WindowEvent e){
            directorio.Exit();
            System.exit(0);
        }
        });
        Functions.setLineNumberOnJTextComponent(jtpCode); 
        timerKeyReleased = new Timer((int)(1000*0.3), (ActionEvent e) -> {
        timerKeyReleased.stop();
        colorAnalysis();
        });
        Functions.insertAsteriskInName(this,jtpCode, () -> {
            timerKeyReleased.restart();       
        });
        tokens = new ArrayList<>();
        errors = new ArrayList<>();
        textsColor = new ArrayList<>();
        identProd = new ArrayList<>();
        identificadores = new HashMap<>();
        Functions.setAutocompleterJTextComponent(new String[] {"Color", "Numero", "Samuel","Monse"} ,jtpCode, () ->{
        timerKeyReleased.restart();
    });
    }
    
   private void compile (){
       clearFields();
       lexicaAnalysis();
       fillTableTokens();
       syntacticAnalysis();
       semanticAnalysis();
       printConsole();
       codeHasBeenCompiled = true;
   }
   
   private void clearFields(){
   Functions.clearDataInTable(tblTokens);
   jtaOutputConsole.setText("");
   tokens.clear();
   errors.clear();
   identProd.clear();
   identificadores.clear();
   codeHasBeenCompiled = false;
   }
   private void lexicaAnalysis(){
   Lexer lexer;
   try{
       File codigo = new File("code.encrypter");
       FileOutputStream output = new FileOutputStream(codigo);
       byte[] bytesText = jtpCode.getText().getBytes();
       output.write(bytesText);
       BufferedReader entrada = new BufferedReader(new InputStreamReader (new FileInputStream(codigo), "UTF8"));
       lexer = new Lexer (entrada);
       while (true){
           Token token = lexer.yylex();
           if (token == null){
               break;
           }
           tokens.add(token);
       }
    }catch (FileNotFoundException ex){
       System.out.println("El archivo no puedo ser encontrado..."+ ex.getMessage());
   }catch (IOException ex) {
       System.out.println("Error al escribir en el archivo..."+ ex.getMessage());
   }
   }
   private void fillTableTokens(){
   tokens.forEach(token -> {
        Object[] data = new Object[]{token.getLexicalComp(), token.getLexeme(), "["+ token.getLine()+ ","+ token.getColumn() + "]"};
        Functions.addRowDataInTable(tblTokens, data);
   });
   }
   private void syntacticAnalysis(){
       Grammar gramatica = new Grammar(tokens, errors);
       
       /*Eliminacion de errores*/
       gramatica.delete(new String[] {"ERROR", "ERROR_1", "ERROR_2"}, 1);
       gramatica.group("VALOR", "NUMERO | COLOR | CONDICIONES | OPRELACIONALES");
       
       gramatica.group("VARIABLE", "TIPO_DATO IDENTIFICADOR OPDEASIGNACION VALOR", true);
       gramatica.group("VARIABLE", "INDENTIFICADOR OPDEASIGNACION VALOR", true,
               2, "ERROR SINTACTICO {}: Falta el tipo de dato de la variable [#,%]");
       
       gramatica.finalLineColumn();
       
       gramatica.group("VARIABLE", "TIPO_DATO OPDEASIGNACION VALOR", true,
               3, "ERROR Sintactico {}: Falta el identificador en la variable [#,%]");
       gramatica.finalLineColumn();
       
       gramatica.group("VARIABLE", "TIPO_DATO IDENTIFICADOR VALOR", true,
               4, "ERROR Sintactico {}: Falta el Operador de Asigacion en la variable [#,%]");
       gramatica.finalLineColumn();
       gramatica.group("VARIABLE", "TIPO_DATO IDENTIFICADOR OPDEASIGNACION", true,
               5, "ERROR Sintactico {}: Falta el Valor en la variable [#,%]");
       gramatica.initialLineColumn();
       gramatica.finalLineColumn();
       
       
       /*Eliminacion de tipo de datos y operadores de asignacion*/
       
       gramatica.delete("TIPO_DATO", 6, "ERROR Sintactico {}: El tipo de dato no esta en una declaracion [#,%]");
       gramatica.delete("OPDEASIGNACION",
               7, "ERROR Sintactico {}: El operador de asignacion no esta en una declaracion [#,%]");
       
       /*Agrupar identificadores y definicones de parametros*/
       gramatica.group("VALOR", "IDENTIFICADOR", true);
       gramatica.group("PARAMETROS", "VALOR (COMA VALOR)+");
       
       gramatica.group("FUNCION", "PALABRA_RESERVADA | MOVIMIENTO | PINTAR | DETENER_PINTAR | REPETIR | DETENER_REPETIR | ESTRUCTURA_SI | TOMAR | LANZARMONEDA | VER", true);
       gramatica.group("FUNCION_COMP", "FUNCION PARENTESIS_A (VALOR | PARAMETROS)? PARENTESIS_C", true);
       gramatica.group("FUNCION_COMP", "FUNCION (VALOR | PARAMETROS)? PARANTESIS_C", true,
               8, "ERROR Sintactico {}: Falta el parentesis que abre en la funcion [#,%]");
       gramatica.finalLineColumn();
       
       gramatica.group("SENTENCIA", "SENTENCIA_TRUE", true);
       gramatica.group("SENTENCIA_COMP", "SENTENCIA PARENTESIS_A (VALOR | PARAMETROS) PARENTESIS_C LLAVE_A (VALOR | PARAMETROS | FUNCION_COMP) LLAVE_C", true);
       gramatica.group("SENTENCIA_COMP", "SENTENCIA (VALOR | PARAMETROS) PARANTESIS_C (VALOR | PARAMETROS | FUNCION_COMP) LLAVE_C", true,
               20, "ERROR Sintactico {}: Falta el parentesis que abre en la sentencia if [#,%]");
       gramatica.finalLineColumn();
       
       
        gramatica.group("SENTENCIAF", "SENTENCIA_FALSE", true);
       gramatica.group("SENTENCIAF_COMP", "SENTENCIAF LLAVE_A (VALOR | PARAMETROS | FUNCION_COMP) LLAVE_C ", true);
       gramatica.group("SENTENCIAF_COMP", "SENTENCIAF (VALOR | PARAMETROS | FUNCION_COMP) LLAVE_C ", true,
               25, "ERROR Sintactico {}: Falta la llave que abre en la sentenciaif [#,%]");
       gramatica.finalLineColumn();
       
       gramatica.group("FUNCION_COMP", "FUNCION PARENTESIS_A (VALOR | PARAMETROS)", true,
               9, "ERROR Sintactico {}: Falta el parentesis que cierra en la funcion [#,%]");
       gramatica.initialLineColumn();
       
       gramatica.group("SENTENCIA_COMP", "SENTENCIA PARENTESIS_A (VALOR | PARAMETROS) ", true,
               28, "ERROR Sintactico {}: Falta alguna llave en la sentencia [#,%]");
       gramatica.initialLineColumn();
       
       gramatica.group("SENTENCIA_COMP", "SENTENCIA PARENTESIS_A  PARANTESIS_C ", true,
               30, "ERROR Sintactico {}: Falta los parametros de la sentencia if [#,%]");
       gramatica.initialLineColumn();
       
       gramatica.group("SENTENCIAF_COMP", "SENTENCIAF LLAVE_A (VALOR | PARAMETROS)", true,
               29, "ERROR Sintactico {}: Falta la llave que cierra en la sentencia if [#,%]");
       gramatica.initialLineColumn();
       
       /*Eliminacion de funciones incompletas*/
       gramatica.delete("FUNCION", 10, "ERROR Sintactico {}: La funcion no esta declarada correctamente [#,%]");
       gramatica.loopForFunExecUntilChangeNotDetected(() -> { //metodo que detecta todas las agrupaciones y se detiene hasta que no encuentra algun 
       gramatica.group("EXP_LOGICA", "(FUNCION_COMP | EXP_LOGICA) OPERADOR_LOGICO (FUNCION_COMP | EXP_LOGICA)+");
       gramatica.group("EXP_LOGICA", "PARENTESIS_A (EXP_LOGICA | FUNCION_COMP) PARENTESIS_C");
       });
       /*Eliminacion de Sentencias incompletas*/
       gramatica.delete("SENTENCIA", 21, "ERROR Sintactico {}: Faltan los parametros de la condicion o bloque de sentencia [#,%]");
       gramatica.loopForFunExecUntilChangeNotDetected(() -> { //metodo que detecta todas las agrupaciones y se detiene hasta que no encuentra algun 
       gramatica.group("EXP_LOGICA", "(SENTENCIA_COMP | EXP_LOGICA) OPERADOR_LOGICO (SENTENCIA_COMP | EXP_LOGICA)+");
       gramatica.group("EXP_LOGICA", "PARENTESIS_A (EXP_LOGICA | SENTENCIA_COMP) PARENTESIS_C LLAVE_A (VALOR | PARAMETROS | FUNCION_COMP) LLAVE_C");
       });
       /*Eliminacion de Sentencias incompletas*/
       gramatica.delete("SENTENCIAF", 27, "ERROR Sintactico {}: Faltan los parametros de la condicion o bloque de sentencia [#,%]");
       gramatica.loopForFunExecUntilChangeNotDetected(() -> { //metodo que detecta todas las agrupaciones y se detiene hasta que no encuentra algun 
       gramatica.group("EXP_LOGICA", "(SENTENCIAF_COMP | EXP_LOGICA) OPERADOR_LOGICO (SENTENCIAF_COMP | EXP_LOGICA)+");
       gramatica.group("EXP_LOGICA", "LLAVE_A (EXP_LOGICA | SENTENCIA_COMP) LLAVE_C");
       });

       /*Eliminacion de un operador logico*/
       gramatica.delete("OPERADOR_LOGICO", 11, "ERROR Sintactico {}: El operador Logico no esta contenido en una expresion");
       
       /*Agrupacion de expresiones logicas*/
       gramatica.group("VALOR", "EXP_LOGICA");
       gramatica.group("PARAMETROS", "VALOR (COMA VALOR)+");
       
       /*Agrupacion de estructuras de Control*/
       gramatica.group("EST_CONTROL", "REPETIR | ESTRUCTURA_SI");
       gramatica.group("EST_CONTROL_COMP", "EST_CONTROL PARENTESIS_A PARENTESIS_C");
       gramatica.group("EST_CONTROL_COMP", "EST_CONTROL (VALOR | PARAMETROS)");
       gramatica.group("EST_CONTROL_COMP", "EST_CONTROL PARENTESIS_A (VALOR | PARAMETROS) PARENTESIS_C");
       
       
       /*Eliminacion d eestructuras de control incompletas*/
       gramatica.delete("EST_CONTROL", 12, "Error Sintactico {}: La estructura de control nom esta declarada correctamente [#,%]");
       
       /*Eliminacion de parentesis*/
       gramatica.delete(new String[] {"PARENTESIS_A", "PARENTESIS_C"}, 
       13, "ERROR Sintactico {}: El parentesis  [] no esta declarado correctamente [#,%]");
       gramatica.finalLineColumn();
       
       /*Eliminacion de llaves*/
       gramatica.delete(new String[] {"LLAVE_A", "LLAVE_C"}, 
       22, "ERROR Sintactico {}: La llave [] no esta declarado correctamente [#,%]");
       gramatica.finalLineColumn();
       /*Eliminacion de llaves*/
       gramatica.delete(new String[] {"(VALOR | PARAMETROS)"}, 
       31, "ERROR Sintactico {}: Faltan los pasametros de los parentesis  [#,%]");
       gramatica.finalLineColumn();
       
       /*Verificacion de punto y coma*/
       gramatica.group("VARIABLE_PC", "VARIABLE PUNTOYCOMA");
       gramatica.group("VARIABLE_PC", "VARIABLE", true,
               14, "ERROR Sintactico {}:Falta el punto y coma [] al final de variable [#,%]");
       
       
       /*Funciones */
       gramatica.group("FUNCION_COM_PC", "FUNCION_COMP SENTENCIA_COMP PUNTOYCOMA");
       gramatica.group("FUNCION_COMP_PC", "FUNCION_COMP FUNCION_COMP", true,
               15, "ERROR Sintactico {}: Falta el punto y coma [] de la delcaracion de la funcion [#,%]");
       gramatica.initialLineColumn();
       
         /*Eliminar punto y coma*/
         gramatica.delete("PUNTOYCOMA", 
                 16,"ERROR Sintactico {}: El punto y coma no esta al final de una sentencia[#,%]");
         
         /*Sentencias*/
    /*     gramatica.group("SENTENCIAS", "SENTENCIA_TRUE", true);
         gramatica.loopForFunExecUntilChangeNotDetected(() -> { 
         gramatica.group("EST_CONTROL_COMP_LASLC", "EST_CONTROL_COMP LLAVE_A (SENTENCIAS) LLAVE_C", true);
         gramatica.group("SENTENCIAS", "(SENTENCIAS | EST_CONTROL_COMP)");
         });*/
         
         
         /*Estructura de funcion incompleta */
       /*  gramatica.loopForFunExecUntilChangeNotDetected(() ->{ 
         gramatica.initialLineColumn();
         gramatica.group("EST_CONTROL_COMP_LASLC", "EST_CONTROL_COMP (SENTENCIAS)? LLAVE_C", true,
                 17, "ERROR Sintactico {}: Falta de llave que abre en la estructura de control [#,%]");
         
         gramatica.finalLineColumn();
         gramatica.group("EST_CONTROL_COMP_LASLC", "EST_CONTROL_COMP LLAVE_A (SENTENCIAS)?", true,
                 18, "ERROR Sintactico {}: Falta la llava que cierra en la estructura de control [#,%]");
         gramatica.group("SENTENCIAS", "SENTENCIAS | EST_CONTROL_COMP_LASLC");
         });
         gramatica.delete(new String[] {"LLAVE_A", "LLAVE_C"},
         19, "ERROR Sintactico {}: La llave [] no esta conteniada a una agrupacion [#,&]");
         */
         gramatica.show();
   }
   private void semanticAnalysis(){
   HashMap<String, String> identDataType = new HashMap<>();
   identDataType.put("color", "COLOR");
   identDataType.put("numero", "NUMERO");
   for (Production id : identProd){
       if(!identDataType.get(id.lexemeRank(0)).equals(id.lexicalCompRank(-1))){ 
           errors.add(new ErrorLSSL(1, "Error semantico {} : Valor no compatible con el tipo de dato [#,%]", id, true));
       }else if (id.lexicalCompRank(-1).equals("COLOR")&& !id.lexemeRank(-1).matches("#[0-9a-fA-F]+")){
           errors.add(new ErrorLSSL(2, "Error semantico{} : El color no es un numero hexadecimal [#,%]", id,false ));
   }else{
           identificadores.put(id.lexemeRank(1), id.lexemeRank(-1));
           }
     }
   }
   private void printConsole(){
       int sizeErrors = errors.size();
       if (sizeErrors > 0){
           Functions.sortErrorsByLineAndColumn(errors);
           String strErrors = "\n";
           for (ErrorLSSL error : errors){
               String strError = String.valueOf(error);
                       strErrors += strError + "\n";
           }
           jtaOutputConsole.setText("Compilacion terminada...\n" + strErrors + "\nLa compilacion termino con errores...");
       }else {
           jtaOutputConsole.setText("Compilacion terminada");
       }
       jtaOutputConsole.setCaretPosition(0);
   }
   private void colorAnalysis(){
       textsColor.clear();
       LexerColor lexerColor;
       try{
           File codigo = new File("color.encrypter");
           FileOutputStream output = new FileOutputStream(codigo);
           byte[] bytesText = jtpCode.getText().getBytes();
           output.write(bytesText);
           BufferedReader entrada = new BufferedReader(new InputStreamReader (new FileInputStream (codigo), "UTF8"));
           lexerColor = new LexerColor(entrada);
           while (true){
              TextColor textColor = lexerColor.yylex();
               if(textColor == null){
                   break;
               }
               textsColor.add(textColor);
           }
       }catch (FileNotFoundException ex){
           System.out.println("El archivo no puedo ser encontrado...."+ ex.getMessage());
       }catch(IOException ex){
           System.out.println("Error al escribir en el archivo"+ ex.getMessage());
       }
       Functions.colorTextPane(textsColor, jtpCode, new Color(40,40,40));
   }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        BtnNuevo = new javax.swing.JButton();
        BtnAbrir = new javax.swing.JButton();
        BtnGuardar = new javax.swing.JButton();
        BtnGuardarComo = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        BtnCompilar = new javax.swing.JButton();
        BtnEjecutar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtaOutputConsole = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblTokens = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jtpCode = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        BtnNuevo.setText("Nuevo");
        BtnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnNuevoActionPerformed(evt);
            }
        });

        BtnAbrir.setText("Abrir");
        BtnAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAbrirActionPerformed(evt);
            }
        });

        BtnGuardar.setText("Guardar");
        BtnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnGuardarActionPerformed(evt);
            }
        });

        BtnGuardarComo.setText("Guardar Como");
        BtnGuardarComo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnGuardarComoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(BtnNuevo)
                .addGap(29, 29, 29)
                .addComponent(BtnAbrir)
                .addGap(27, 27, 27)
                .addComponent(BtnGuardar)
                .addGap(41, 41, 41)
                .addComponent(BtnGuardarComo)
                .addContainerGap(77, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BtnNuevo)
                    .addComponent(BtnAbrir)
                    .addComponent(BtnGuardar)
                    .addComponent(BtnGuardarComo))
                .addContainerGap(58, Short.MAX_VALUE))
        );

        BtnCompilar.setText("Compilar");
        BtnCompilar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCompilarActionPerformed(evt);
            }
        });

        BtnEjecutar.setText("Ejecutar");
        BtnEjecutar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnEjecutarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(BtnCompilar)
                .addGap(62, 62, 62)
                .addComponent(BtnEjecutar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BtnCompilar)
                    .addComponent(BtnEjecutar))
                .addContainerGap(55, Short.MAX_VALUE))
        );

        jtaOutputConsole.setColumns(20);
        jtaOutputConsole.setRows(5);
        jScrollPane2.setViewportView(jtaOutputConsole);

        tblTokens.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Componente Lexico", "Lexema", "[Linea, Columnas]"
            }
        ));
        jScrollPane3.setViewportView(tblTokens);

        jLabel1.setText("Pineda Rodriguez Samuel");

        jScrollPane4.setViewportView(jtpCode);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane2)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel1))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 498, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 437, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 403, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnNuevoActionPerformed
        directorio.New();
        clearFields();
    }//GEN-LAST:event_BtnNuevoActionPerformed

    private void BtnAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAbrirActionPerformed
        if(directorio.Open()){
            colorAnalysis();
            clearFields();
        }
    }//GEN-LAST:event_BtnAbrirActionPerformed

    private void BtnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGuardarActionPerformed
        if(directorio.Save()){
            clearFields();
        }
    }//GEN-LAST:event_BtnGuardarActionPerformed

    private void BtnGuardarComoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGuardarComoActionPerformed
        if(directorio.SaveAs()){
            clearFields();
            
        }
    }//GEN-LAST:event_BtnGuardarComoActionPerformed

    private void BtnCompilarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCompilarActionPerformed
        if (getTitle().contains("*") || getTitle().equals(title)){
            compile();
        }else{
            compile();
        }
    }//GEN-LAST:event_BtnCompilarActionPerformed

    private void BtnEjecutarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEjecutarActionPerformed
        BtnEjecutar.doClick();
        if(codeHasBeenCompiled){
            if(!errors.isEmpty()){
                JOptionPane.showMessageDialog(null, "No se puede ejecutar el codigo ya que se encontro uno o mas errores",
                "ERROR en la compilacion", JOptionPane.ERROR_MESSAGE);
            } else {
                CodeBlock codeBlock = Functions.splitCodeInCodeBlocks(tokens, "{", "}",";");
                System.out.println(codeBlock);
                ArrayList<String>blocksOfCode = codeBlock.getBlocksOfCodeInOrderOfExec();
                System.out.println(blocksOfCode);
            }
        }
    }//GEN-LAST:event_BtnEjecutarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Compilador.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Compilador.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Compilador.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Compilador.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Compilador().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BtnAbrir;
    private javax.swing.JButton BtnCompilar;
    private javax.swing.JButton BtnEjecutar;
    private javax.swing.JButton BtnGuardar;
    private javax.swing.JButton BtnGuardarComo;
    private javax.swing.JButton BtnNuevo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextArea jtaOutputConsole;
    private javax.swing.JTextPane jtpCode;
    private javax.swing.JTable tblTokens;
    // End of variables declaration//GEN-END:variables
}

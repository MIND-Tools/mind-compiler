/**
 * Copyright (C) 2009 France Telecom
 *
 * This file is part of "Mind Compiler" is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: mind@ow2.org
 *
 * Authors: Matthieu ANNE
 * Contributors: Olivier Lobry, Matthieu Leclercq
 */
 
grammar CPL;

options {
	output = template;
	backtrack = true;
	superClass = AbstractCPLParser;
}

tokens{
	METH 		= 'METH';
    CONSTRUCTOR = 'CONSTRUCTOR';
    DESTRUCTOR  = 'DESTRUCTOR';
    GET_MY_INTERFACE = 'GET_MY_INTERFACE';
    BIND_MY_INTERFACE = 'BIND_MY_INTERFACE';
    IS_BOUND    = 'IS_BOUND';
    GET_COLLECTION_SIZE = 'GET_COLLECTION_SIZE';
	CALL 		= 'CALL';
	CALL_PTR 	= 'CALL_PTR';
	ATTR 		= 'ATTR';
	PRIVATE 	= 'PRIVATE';
	METH_PTR	= 'METH_PTR' ;
	VOID 		= 'void';
	STRUCT 	= 'struct';
}

@header {
	package org.ow2.mind.preproc.parser;
    import java.io.*;
    import java.util.List;
    import java.util.ArrayList;
    import org.objectweb.fractal.adl.ADLException;
    import org.objectweb.fractal.adl.error.BasicErrorLocator;
    import org.objectweb.fractal.adl.error.Error;
    import org.ow2.mind.error.ErrorManager;
    import org.ow2.mind.preproc.CPLChecker;
    import org.ow2.mind.preproc.MPPErrors;
}

@lexer::header {
	package org.ow2.mind.preproc.parser;
	import java.util.regex.Matcher;
	import java.util.regex.Pattern;
	import org.antlr.runtime.ANTLRStringStream;
}

@lexer::members {

  static final Pattern sourceLinePattern = Pattern
                                             .compile("\\#\\s*(\\d+)\\s*\"(.*)\"");
  static final Pattern shortSourceLinePattern = Pattern
                                             .compile("\\#\\s*(\\d+)\\s*");
  static final int     lineIndex         = 1;
  static final int     fileIndex         = 2;
  
  public String getSourceFileName() {
  	return ((ANTLRStringStream) input).name;
  }

  public void processSourceLine() {
    // handle standard preprocessors
    Matcher standardMatcher = sourceLinePattern.matcher(input.substring(state.tokenStartCharIndex, getCharIndex()-1));
    
    // handle preprocessors that output "#line n" information without file reference
    Matcher shortMatcher = shortSourceLinePattern.matcher(input.substring(state.tokenStartCharIndex, getCharIndex()-1));
    
    int line;
    String file;
    if (standardMatcher.matches()) {
      // handle standard preprocessors
      line = Integer.parseInt(standardMatcher.group(lineIndex));
      file = standardMatcher.group(fileIndex);
      input.setLine(line - 1);
      ((ANTLRStringStream) input).name = file;
      return;
    } else if (shortMatcher.matches()) {
      // handle preprocessors that output "#line n" information without file reference
      line = Integer.parseInt(shortMatcher.group(lineIndex));
      input.setLine(line - 1);
    }    
  }
}

@members{
	
	String getSourceFile() {
		return ((CPLLexer) input.getTokenSource()).getSourceFileName();
	}
	
	public void displayRecognitionError(String[] tokenNames,
                                        			RecognitionException e) {
	String sourceFile = getSourceFile();

    try {
		  errorManager.logError(MPPErrors.PARSE_ERROR, new BasicErrorLocator(sourceFile,
		    (e.line), e.charPositionInLine),  
		    super.getErrorMessage(e, tokenNames));
		} catch (ADLException e1) {
		  // ignore
		}
	}
	
	public void preprocess() throws RecognitionException {
	  parseFile();
	}
	
	protected String wstext(String ws) {
	  return ws == null ? "" : ws;
	}
}

parseFile returns [String res]
@init{StringBuilder sb = new StringBuilder(); }
@after{$res=sb.toString(); out.println($res);}
  :(methDef    {sb.append($methDef.res);}
  | methCall    {sb.append($methCall.res); }
  | attAccess     {sb.append($attAccess.res); }
  | privateAccess   {sb.append($privateAccess.res); }
  | structDecl    {sb.append($structDecl.res); }
  | methPtrDef    {sb.append($methPtrDef.res); }
//  | sourceLineInfo		{sb.append($sourceLineInfo.res); }
  | e= ~ (METH | CALL | ATTR | PRIVATE | STRUCT | METH_PTR | CALL_PTR
           | CONSTRUCTOR | DESTRUCTOR | GET_MY_INTERFACE | BIND_MY_INTERFACE
           | IS_BOUND )
        {sb.append($e.text);}
  )+  
  ;


protected methPtrDef returns [StringBuilder res = new StringBuilder()] 
	: METH_PTR ws1=ws* { $res.append(wstext($ws1.text)); }
	  (
        '(' ws2=ws* ID ws3=ws* ')' { $res.append(wstext($ws2.text)).append($ID.text).append(wstext($ws3.text)); } 
        | ptrMethArg             { $res.append($ptrMethArg.res); }
      )
      (
		ws4=ws* ')' { $res.append(wstext($ws4.text)).append(")"); }
      )* // handle case of (((... METH_PTR(foo) )))(...
      ws5=ws* { $res.append(wstext($ws5.text)); } 
	( paramsDef { $res.append($paramsDef.res); } ) ?
	;

protected methDef returns [StringBuilder res]
	: serverMethDef	{$res = $serverMethDef.res;}
	|privateMethDef {$res = $privateMethDef.res;}
    |constructorDef {$res = $constructorDef.res;}
    |destructorDef  {$res = $destructorDef.res;}
	;
		
protected serverMethDef returns [StringBuilder res = new StringBuilder()]
@init{String tmp = ""; String itfIdx = null;}
  : METH ws1=ws* '(' ws2=ws* id=ID ws3=ws* ( '[' ws4=ws* INT ws5=ws* ']' ws6=ws* { itfIdx=$INT.text; } )? ',' ws7=ws* meth=ID ws8=ws* ')' ws9=ws*
      (
        e = ws* ')' { tmp += wstext($e.text) + ")"; }
      )* // handle case of (((... METH(foo) )))(...
    {
      try {
        cplChecker.serverMethDef($id, $meth, getSourceFile());
      } catch (ADLException e1) {
        // ignore
      }
      
      if (itfIdx == null) {
          $res.append("INTERFACE_METHOD").append(wstext($ws1.text)).append("(")
              .append(wstext($ws2.text)).append($id.text).append(wstext($ws3.text)).append(",")
              .append(wstext($ws7.text)).append($meth.text).append(wstext($ws8.text)).append(")").append(wstext($ws9.text))
              .append(tmp);
      } else {
          $res.append("INTERFACE_COLLECTION_METHOD").append(wstext($ws1.text)).append("(")
              .append(wstext($ws2.text)).append($id.text).append(wstext($ws3.text)).append(",")
              .append(wstext($ws4.text)).append(itfIdx).append(wstext($ws5.text)).append(wstext($ws6.text)).append(",")
              .append(wstext($ws7.text)).append($meth.text).append(wstext($ws8.text)).append(")").append(wstext($ws9.text))
              .append(tmp);
      }
    }
    (
      paramsDef ws10=ws* { $res.append($paramsDef.res).append(wstext($ws10.text)); }
      (
         '{'
          {
            if (!singletonMode) 
              $res.append("{ CHECK_CONTEXT_PTR "); 
            else
              $res.append("{");
            if (headerOut != null) headerOut.println("#define INTERFACE_METHOD_" + $id.text + "_" + $meth.text + "_IMPLEMENTED"); 
          }
      )?
    )?
    ;

protected privateMethDef returns [StringBuilder res = new StringBuilder()]
@init{String tmp = "";}
    : METH ws1=ws* '(' ws2=ws* id=ID ws3=ws* ')' ws4=ws*
      (
        e = ws* ')' { tmp += wstext($e.text) + ")"; }
	  )* // handle case of (((... PRV(foo) )))(...
      {
        $res.append("PRIVATE_METHOD").append(wstext($ws1.text)).append("(")
            .append(wstext($ws2.text)).append($id.text).append(wstext($ws3.text)).append(")").append(wstext($ws4.text))
            .append(tmp);
      }
      ( paramsDef ws5=ws* { $res.append($paramsDef.res).append(wstext($ws5.text)); } 
        (
          '{'
            {
              if (!singletonMode) 
                $res.append("{ CHECK_CONTEXT_PTR "); 
              else
                $res.append("{");
            }
        )?
      )?
	;
	
protected constructorDef returns [StringBuilder res = new StringBuilder()]
    : CONSTRUCTOR ws1=ws* '(' ws2=ws* { $res.append("CONSTRUCTOR_METHOD").append(wstext($ws1.text)).append(wstext($ws2.text)); }
      ( VOID ws3=ws* { $res.append(wstext($ws3.text)); } ) ? 
      ')'
      ( 
        ws4=ws* '{'
          {
            if (singletonMode) 
              $res.append(wstext($ws4.text)).append("{"); 
            else 
              $res.append(wstext($ws4.text)).append("{ CHECK_CONTEXT_PTR ");
            if (headerOut != null) headerOut.println("#define CONSTRUCTOR_METHOD_IMPLEMENTED"); 
          }
      )?
    ;
        
protected destructorDef returns [StringBuilder res = new StringBuilder()]
    : DESTRUCTOR ws1=ws* '(' ws2=ws* { $res.append("DESTRUCTOR_METHOD").append(wstext($ws1.text)).append(wstext($ws2.text)); }
      ( VOID ws3=ws* { $res.append(wstext($ws3.text)); } ) ? 
      ')'
      ( 
        ws4=ws* '{'
          {
            if (singletonMode) 
              $res.append(wstext($ws4.text)).append("{"); 
            else 
              $res.append(wstext($ws4.text)).append("{ CHECK_CONTEXT_PTR ");
            if (headerOut != null) headerOut.println("#define DESTRUCTOR_METHOD_IMPLEMENTED"); 
          }
      )?
    ;

protected ptrMethArg returns [StringBuilder res = new StringBuilder()]
    : '(' ws1=ws* { $res.append("(").append(wstext($ws1.text)); }
      (
        methDef                   { $res.append($methDef.res); }
        | methPtrDef              { $res.append($methPtrDef.res); }
        | pma=ptrMethArg          { $res.append($pma.res); }
        | t2= ~(METH | ')' | '(') { $res.append($t2.text);}
      )+
      ws2=ws* ')' { $res.append(wstext($ws2.text)).append(")");}
   ;

protected expr  returns [String res = ""]
	: methCall 			{$res += $methCall.res; }
	| attAccess 		{$res += $attAccess.res; }
	| privateAccess 	{$res += $privateAccess.res; }
	;
	
protected methCall returns [StringBuilder res ]
	: itfMethCall           { $res = $itfMethCall.res; }
	| collItfMethCall       { $res = $collItfMethCall.res; }
	| prvMethCall           { $res = $prvMethCall.res; }
	| ptrMethCall           { $res = $ptrMethCall.res; }
	| getMyInterfaceCall    { $res = $getMyInterfaceCall.res; }
	| bindMyInterfaceCall   { $res = $bindMyInterfaceCall.res; }
  | isBoundCall           { $res = $isBoundCall.res; }
  | getCollectionSizeCall { $res = $getCollectionSizeCall.res; }
	;

protected attAccess returns [StringBuilder res = new StringBuilder()]
    : ATTR ws1=ws* '(' ws2=ws* att=ID ws3=ws* ')' 
      { 
	      try {
          cplChecker.attAccess($att, getSourceFile());
	      } catch (ADLException e) {
	        // ignore
	      }
    	  $res.append("ATTRIBUTE_ACCESS").append(wstext($ws1.text)).append("(")
            .append(wstext($ws2.text)).append($att.text).append(wstext($ws3.text)).append(")");
      }
    ;
	
protected structDecl returns [StringBuilder res = new StringBuilder()]
@init{StringBuilder str = new StringBuilder(); boolean isPrivate = false;}
    : STRUCT ws1=ws* 
      (
        (structfield) => 
            structfield 
            (
              (
                ws2=ws* PRIVATE { isPrivate=true; } 
                ( t = ~('='|';'|',') { str.append($t.text); } )* 
              ) ws4=ws* 
              | ( t = ~(';'| PRIVATE) {str.append($t.text); } ) * 
            ) ';'
            {
              String structContent = $structfield.text.substring(1); // (NB: removes first '{'
              if (isPrivate) {
                cplChecker.prvDecl(structContent, getSourceFile());
              }
              if (singletonMode) {
                $res.append($text); 
              } else if (isPrivate) {
                $res.append("typedef struct").append(wstext($ws1.text)).append("{");
                $res.append(" COMP_DATA; ");
                $res.append(structContent);
                $res.append(wstext($ws2.text)).append(" PRIVATE_DATA_T");
                $res.append(str);
                $res.append(";");
              } else {
                $res.append("struct ").append(wstext($ws1.text)).append($structfield.text)
                    .append(str).append(";");
              }
            }
        | ( e=~LCURLY { $res.append("struct").append(wstext($ws1.text)).append($e.text); } )
      )
    ;

protected structinitializer
	: LCURLY (
		structinitializer |~(LCURLY|RCURLY))* 
	RCURLY;
	
protected structfield 
	:LCURLY
	( structfield 
	| ~(LCURLY|RCURLY) 
	)*
	RCURLY
	;

protected privateAccess returns [StringBuilder res = new StringBuilder()]
    : PRIVATE ws1=ws* '.' ws2=ws* field=ID
      {
        if (singletonMode) $res.append($text); 
        else {
          boolean isDataField;
          try {
	    	isDataField = cplChecker.prvAccess($field, getSourceFile());
	      } catch (ADLException e) {
	        isDataField = false;
	      }
	      if (isDataField) {
	      	$res.append("DATA_FIELD_ACCESS(").append(wstext($ws1.text)).append(wstext($ws2.text)).append($field.text).append(")");
	      } else {
            $res.append("CONTEXT_PTR_ACCESS").append(wstext($ws1.text)).append("->").append(wstext($ws2.text)).append($field.text);
	      }
        } 
      }
    | {singletonMode==true}? PRIVATE { $res.append($text); } 
    ;

protected itfMethCall returns [StringBuilder res = new StringBuilder()]
	: CALL ws1=ws* '(' ws2=ws* itf=ID ws3=ws* ',' ws4=ws* meth=ID ws5=ws* ')' ws6=ws* params
      {
	      try {
	    	  cplChecker.itfMethCall($itf, $meth, getSourceFile());
	      } catch (ADLException e) {
	        // ignore
	      }
 
        if ($params.res == null)
          $res.append("CALL_INTERFACE_METHOD_WITHOUT_PARAM").append(wstext($ws1.text)).append("(")
              .append(wstext($ws2.text)).append($itf.text).append(wstext($ws3.text)).append(",")
              .append(wstext($ws4.text)).append($meth.text).append(wstext($ws5.text)).append(")")
              .append(wstext($ws6.text));
       else
          $res.append("CALL_INTERFACE_METHOD_WITH_PARAM").append(wstext($ws1.text)).append("(")
              .append(wstext($ws2.text)).append($itf.text).append(wstext($ws3.text)).append(",")
              .append(wstext($ws4.text)).append($meth.text).append(wstext($ws5.text)).append(")")
              .append(wstext($ws6.text)).append($params.res);
      }
    ;
		
protected collItfMethCall returns [StringBuilder res = new StringBuilder()]
    : CALL ws1=ws* '(' ws2=ws* itf=ID ws3=ws* index ws4=ws* ',' ws5=ws* meth=ID ws6=ws* ')' ws7=ws* params
      {
        try {
          cplChecker.collItfMethCall($itf, $meth, $index.res, getSourceFile());
        } catch (ADLException e) {
          // ignore
        }
 
        if ($params.res == null)
          $res.append("CALL_COLLECTION_INTERFACE_METHOD_WITHOUT_PARAM").append(wstext($ws1.text)).append("(")
              .append(wstext($ws2.text)).append($itf.text).append(wstext($ws3.text)).append(",")
              .append($index.res).append(wstext($ws4.text)).append(",")
              .append(wstext($ws5.text)).append($meth.text).append(wstext($ws5.text)).append(")")
              .append(wstext($ws7.text));
        else
          $res.append("CALL_COLLECTION_INTERFACE_METHOD_WITH_PARAM").append(wstext($ws1.text)).append("(")
              .append(wstext($ws2.text)).append($itf.text).append(wstext($ws3.text)).append(",")
              .append($index.res).append(wstext($ws4.text)).append(",")
              .append(wstext($ws5.text)).append($meth.text).append(wstext($ws5.text)).append(")")
              .append(wstext($ws7.text)).append($params.res);
       }
	;
	
protected index returns [StringBuilder res]
	:
	'[' inIndex ']'	{ $res = $inIndex.res; }
	;
	
protected inIndex returns [StringBuilder res = new StringBuilder()]
    : (
        '[' i = inIndex ']' { $res.append("[").append($i.res).append("]"); }
        | expr              { $res.append($expr.res); }
        | e = ~('[' | ']')  { $res.append($e.text); }
      )+
    ;
	
protected prvMethCall returns [StringBuilder res = new StringBuilder()]
	: CALL ws1=ws* '(' ws2=ws* ID ws3=ws* ')' ws4=ws* params
      {
        if ($params.res == null)
          $res.append("CALL_PRIVATE_METHOD_WITHOUT_PARAM").append(wstext($ws1.text)).append("(")
              .append(wstext($ws2.text)).append($ID.text).append(wstext($ws3.text)).append(")")
              .append(wstext($ws4.text));
        else
          $res.append("CALL_PRIVATE_METHOD_WITH_PARAM").append(wstext($ws1.text)).append("(")
              .append(wstext($ws2.text)).append($ID.text).append(wstext($ws3.text)).append(")")
              .append(wstext($ws4.text)).append($params.res);
      }
    ;
	
protected ptrMethCall returns [StringBuilder res = new StringBuilder()]
    : CALL_PTR ws1=ws*
      (
        '(' ws2=ws* meth=ID ws3=ws* ')' ws4=ws* p1=params
          {
            $res.append("CALL_METHOD_PTR_").append($p1.res == null ? "WITHOUT_PARAM" : "WITH_PARAM")
                .append(wstext($ws1.text)).append("(")
                .append(wstext($ws2.text)).append($meth.text).append(wstext($ws3.text)).append(")")
                .append(wstext($ws4.text)).append($p1.res == null ? "" : $p1.res);
          }
		| '(' ws5=ws* methExpr=ptrMethCallArg ws6=ws* ')' ws7=ws* p2=params
          {
            $res.append("CALL_METHOD_PTR_").append($p2.res == null ? "WITHOUT_PARAM" : "WITH_PARAM")
                .append(wstext($ws1.text)).append("(").append(wstext($ws5.text)).append($methExpr.res).append(wstext($ws6.text)).append(")")
                .append(wstext($ws7.text)).append($p2.res == null ? "" : $p2.res); 
          }
		| '(' ws8=ws* itfExpr=ptrMethCallArg ws9=ws* ',' ws10=ws* methName=ID  ws11=ws* ')' ws12=ws* p3=params
		  {
		    $res.append("CALL_INTERFACE_PTR_").append($p3.res == null ? "WITHOUT_PARAM" : "WITH_PARAM")
		        .append(wstext($ws1.text)).append("(").append(wstext($ws8.text)).append($itfExpr.res).append(wstext($ws9.text)).append(",")
		        .append(wstext($ws10.text)).append($methName.text).append(wstext($ws11.text)).append(")")
                .append(wstext($ws12.text)).append($p3.res == null ? "" : $p3.res);
		  }
      )
    ;

protected ptrMethCallArg returns [StringBuilder res = new StringBuilder()]
    : (
        '(' pma = ptrMethCallArg1 ')' { $res.append("(").append($pma.res).append(")") ; }
        | expr { $res.append($expr.res); }
        | e = ~('(' | ')' | ',') { $res.append($e.text);}
      ) +
	;
protected ptrMethCallArg1 returns [StringBuilder res = new StringBuilder()]
    : ( 
        '(' pma = ptrMethCallArg1 ')' { $res.append("(").append($pma.res).append(")") ; }
        | expr { $res.append($expr.res); }
        | e = ~('(' | ')') { $res.append($e.text); }
      ) +
	;

protected getMyInterfaceCall returns [StringBuilder res = new StringBuilder()]
@init{StringBuilder idx = null;}
	: GET_MY_INTERFACE ws1=ws* '(' ws2=ws* ID ws3=ws* ( index ws4=ws* {idx = $index.res;} ) ? ')'
      {
        try {
          cplChecker.getMyItf($ID, idx, getSourceFile());
        } catch (ADLException e) {
          // ignore
        }
 
        if (idx == null)
          $res.append("GET_MY_INTERFACE").append(wstext($ws1.text)).append("(")
              .append(wstext($ws2.text)).append($ID.text).append(wstext($ws3.text)).append(")");
        else
          $res.append("GET_MY_INTERFACE_COLLECTION").append(wstext($ws1.text)).append("(")
              .append(wstext($ws2.text)).append($ID.text).append(wstext($ws3.text)).append(",")
              .append(idx).append(wstext($ws4.text)).append(")");
      }
    ;
	
protected bindMyInterfaceCall returns [StringBuilder res = new StringBuilder()]
@init{StringBuilder idx = null;}
	: BIND_MY_INTERFACE ws1=ws* '(' ws2=ws* ID ws3=ws* ( index ws4=ws* {idx = $index.res;} ) ? 
	  ',' ws5=ws* sItf=macroParam ws6=ws* ')'
      {
        try {
          cplChecker.bindMyItf($ID, idx, getSourceFile());
        } catch (ADLException e) {
          // ignore
        }
        if (idx == null)
          $res.append("BIND_MY_INTERFACE").append(wstext($ws1.text)).append("(")
              .append(wstext($ws2.text)).append($ID.text).append(wstext($ws3.text)).append(",")
              .append(wstext($ws5.text)).append($sItf.res).append(wstext($ws6.text)).append(")");
        else
          $res.append("BIND_MY_INTERFACE_COLLECTION").append(wstext($ws1.text)).append("(")
              .append(wstext($ws2.text)).append($ID.text).append(wstext($ws3.text)).append(",")
              .append(idx).append(wstext($ws4.text)).append(",")
              .append(wstext($ws5.text)).append($sItf.res).append(wstext($ws6.text)).append(")");
      }
    ;
  
protected isBoundCall returns [StringBuilder res = new StringBuilder()]
@init{StringBuilder idx = null;}
  : IS_BOUND ws1=ws* '(' ws2=ws* ID ws3=ws* ( index ws4=ws* {idx = $index.res;} ) ? ')'
      {
        try {
          cplChecker.isBound($ID, idx, getSourceFile());
        } catch (ADLException e) {
          // ignore
        }
        if (idx == null)
          $res.append("IS_BOUND").append(wstext($ws1.text)).append("(")
              .append(wstext($ws2.text)).append($ID.text).append(wstext($ws3.text)).append(")");
        else
          $res.append("IS_BOUND_COLLECTION").append(wstext($ws1.text)).append("(")
              .append(wstext($ws2.text)).append($ID.text).append(wstext($ws3.text)).append(",")
              .append(idx).append(wstext($ws4.text)).append(")");
      }
    ;
  
protected getCollectionSizeCall returns [StringBuilder res = new StringBuilder()]
  : GET_COLLECTION_SIZE ws1=ws* '(' ws2=ws* ID ws3=ws* ')'
      {
        try {
          cplChecker.getCollectionSize($ID, getSourceFile());
        } catch (ADLException e) {
          // ignore
        }
        $res.append("GET_COLLECTION_SIZE").append(wstext($ws1.text)).append("(")
            .append(wstext($ws2.text)).append($ID.text).append(wstext($ws3.text)).append(")");
      }
    ;
	
protected paramsDef returns [StringBuilder res = new StringBuilder()]
	: '(' ws1=ws* ')'             { $res.append(wstext($ws1.text)).append(" NO_PARAM_DECL "); }
	| '(' ws2=ws* VOID ws3=ws* ')' { $res.append(wstext($ws2.text)).append(" NO_PARAM_DECL ").append(wstext($ws3.text)); }
	| '(' inParamsDef ')'        
	  {
	     $res.append(" PARAM_DECL_BEGIN ").append($inParamsDef.res).append(" PARAM_DECL_END ");
	  }
	;
protected inParamsDef returns [StringBuilder res = new StringBuilder()] 
    : (
        '(' ip = inParamsDef ')' { $res.append("(").append($ip.res).append(")"); }
        | e = ~('(' | ')')       { $res.append($e.text); }
      ) +
    ;

protected params returns [StringBuilder res = new StringBuilder()]
    : '(' ws* ')'       { $res = null; }
    | '(' inParams ')' 
      { 
        $res.append($inParams.res).append(" PARAMS_RPARENT ");
      }
    ;

protected inParams  returns [StringBuilder res = new StringBuilder()] 
    : ( '(' ws* ')'              { $res.append("(").append(wstext($ws.text)).append(")");}
        |'(' ip = inParams ')'  { $res.append("(").append($ip.res).append(")"); }
        | expr                  { $res.append($expr.res); }
        | e = ~('(' | ')')      { $res.append($e.text); }
      )+
    ;

protected macroParam returns [StringBuilder res = new StringBuilder()]
    : (
        expr                     { $res.append($expr.res); }
        | '(' inParams ')'       { $res.append("(").append($inParams.res).append(")"); }
        | e = ~('(' | ')' | ',') { $res.append($e.text); }
      )+
    ;

protected ws
	: (WS | LINE_INFO) ; //{out.print($WS.text);}

//protected sourceLineInfo returns [StringBuilder res = new StringBuilder()]
//	: '#' ws1=ws {$res.append("#").append($ws1.text);} ( 'line' {$res.append("line");})? ws2=ws line=INT ws3=ws filename=. {$res.append($ws2.text).append($line.text).append($ws3.text).append($filename.text);}
//	{sourceLineShift=Integer.parseInt($line.text) - $line.line - 1;
//	 sourceFile = $filename.text;
//	 if (sourceFile.startsWith("\"") && sourceFile.endsWith("\"")) {
//	   sourceFile = sourceFile.substring(1, sourceFile.length()-1);
//	 }
//	}
//	;
  
LINE_INFO
	: '#' (' ' | '\t')* ('line' (' ' | '\t')*)? INT (' ' | '\t')* STRING_LITERAL* {
		processSourceLine();
	}
	;

STRING_LITERAL
    :  '"' ( EscapeSequence | ~('\\'|'"') )* '"'
    ;
  

//CHAR_LITERAL
//    :  '\'' ( EscapeSequence | ~('\'') ) '\''
//    ;
    
fragment
EscapeSequence
    :   '\\' ('a'|'b'|'t'|'n'|'f'|'r'|'v'|'"'|'\''|'\\'|'?')
    |   OctalEscape
    |   HexaEscape
    ;

fragment
OctalEscape
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;
  

fragment
HexaEscape
    :  '\\x' HexaDigit+
    ;

fragment
HexaDigit
	:  '0'..'9'|'a'..'f'|'A'..'F'
	;
	
ID
	: ( 'a'..'z'|'A'..'Z'|'_')( 'a'..'z'|'A'..'Z'|'_'|'0'..'9')*
	;
WS : ((' ' |'\t' |'\r' |'\n' ))+;

INT : ('0'..'9')+;

LCURLY		:	'{' ;

RCURLY		:	'}' ;
ANY 		:	 .;

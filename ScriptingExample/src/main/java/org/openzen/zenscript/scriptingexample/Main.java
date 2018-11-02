package org.openzen.zenscript.scriptingexample;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.openzen.zencode.java.JavaNativeModule;
import org.openzen.zencode.java.ScriptingEngine;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.FileSourceFile;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.BracketExpressionParser;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.expression.ParsedExpressionString;

public class Main {
	public static void main(String[] args) throws CompileException, ParseException, IOException {
		ScriptingEngine scriptingEngine = new ScriptingEngine();
		
		JavaNativeModule module = scriptingEngine.createNativeModule("globals", "org.openzen.zenscript.scriptingexample");
		module.addGlobals(Globals.class);
		scriptingEngine.registerNativeProvided(module);
		
		File inputDirectory = new File("scripts");
		File[] inputFiles = Optional.ofNullable(inputDirectory.listFiles((dir, name) -> name.endsWith(".zs"))).orElseGet(() -> new File[0]);
		SourceFile[] sourceFiles = new SourceFile[inputFiles.length];
		for (int i = 0; i < inputFiles.length; i++)
			sourceFiles[i] = new FileSourceFile(inputFiles[i].getName(), inputFiles[i]);
		
		SemanticModule scripts = scriptingEngine.createScriptedModule("script", sourceFiles, new TestBracketParser());
		if (!scripts.isValid())
			return;
		
		scriptingEngine.registerCompiled(scripts);
		scriptingEngine.run();
	}
	
	private static class TestBracketParser implements BracketExpressionParser {
		@Override
		public ParsedExpression parse(CodePosition position, ZSTokenParser tokens) throws ParseException {
			StringBuilder result = new StringBuilder();
			while (tokens.optional(ZSTokenType.T_GREATER) == null) {
				ZSToken token = tokens.next();
				result.append(token.content);
				result.append(tokens.getLastWhitespace());
			}
			
			return new ParsedExpressionString(position.until(tokens.getPosition()), result.toString(), false);
		}
	}
}

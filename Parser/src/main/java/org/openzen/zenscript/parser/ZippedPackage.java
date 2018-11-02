/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.openzen.zencode.shared.LiteralSourceFile;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.compiler.ModuleSpace;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.lexer.ParseException;

/**
 *
 * @author Hoofdgebruiker
 */
public class ZippedPackage {
	private Map<String, List<SourceFile>> files = new HashMap<>();
	
	public ZippedPackage(InputStream input) throws IOException {
		try (ZipInputStream zipInput = new ZipInputStream(new BufferedInputStream(input))) {
			ZipEntry entry = zipInput.getNextEntry();
			while (entry != null) {
				int slash = entry.getName().indexOf("/src/");
				if (slash > 0) {
					String moduleName = entry.getName().substring(0, slash);
					String filename = entry.getName().substring(slash + 5);
					if (!files.containsKey(moduleName))
						files.put(moduleName, new ArrayList<>());
					
					byte[] data = new byte[(int)entry.getSize()];
					int read = 0;
					while (read < data.length)
						read += zipInput.read(data, read, data.length - read);
					
					files.get(moduleName).add(new LiteralSourceFile(filename, new String(data, StandardCharsets.UTF_8)));
				}
				
				zipInput.closeEntry();
				entry = zipInput.getNextEntry();
			}
		}
	}
	
	public SemanticModule loadModule(ModuleSpace space, String name, BracketExpressionParser bracketParser, SemanticModule[] dependencies) throws ParseException {
		return loadModule(space, name, bracketParser, dependencies, new ZSPackage(space.rootPackage, name));
	}
	
	public SemanticModule loadModule(ModuleSpace space, String name, BracketExpressionParser bracketParser, SemanticModule[] dependencies, ZSPackage pkg) throws ParseException {
		List<SourceFile> sourceFiles = files.get(name);
		if (sourceFiles == null)
			return null; // no such module
		
		Module scriptModule = new Module(name);
		CompilingPackage scriptPackage = new CompilingPackage(pkg, scriptModule);
		ParsedFile[] files = new ParsedFile[sourceFiles.size()];
		for (int i = 0; i < files.length; i++)
			files[i] = ParsedFile.parse(scriptPackage, bracketParser, sourceFiles.get(i));
		
		SemanticModule scripts = ParsedFile.compileSyntaxToSemantic(
				dependencies,
				scriptPackage,
				files,
				space,
				ex -> ex.printStackTrace());
		return scripts.normalize();
	}
}
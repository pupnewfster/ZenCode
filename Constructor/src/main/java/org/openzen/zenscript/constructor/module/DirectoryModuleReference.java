/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import org.openzen.zenscript.compiler.ModuleSpace;
import org.openzen.zenscript.compiler.SemanticModule;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.FileSourceFile;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.compiler.CompilationUnit;
import org.openzen.zenscript.constructor.ConstructorException;
import org.openzen.zenscript.constructor.JSONUtils;
import org.openzen.zenscript.constructor.ParsedModule;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.codemodel.type.TypeSymbol;
import org.openzen.zenscript.codemodel.type.storage.StorageType;
import org.openzen.zenscript.parser.ParsedFile;

/**
 *
 * @author Hoofdgebruiker
 */
public class DirectoryModuleReference implements ModuleReference {
	private final String moduleName;
	private final File directory;
	private final boolean isStdlib;
	private SourcePackage rootPackage = null;
	
	public DirectoryModuleReference(String moduleName, File directory, boolean isStdlib) {
		this.moduleName = moduleName;
		this.directory = directory;
		this.isStdlib = isStdlib;
	}
	
	@Override
	public String getName() {
		return moduleName;
	}

	@Override
	public SemanticModule load(ModuleLoader loader, CompilationUnit unit, Consumer<CompileException> exceptionLogger) {
		if (!directory.exists())
			throw new ConstructorException("Error: module directory not found: " + directory);
		
		File jsonFile = new File(directory, "module.json");
		if (!jsonFile.exists())
			throw new ConstructorException("Error: module.json file not found in module " + moduleName);
		
		try {
			JSONObject json = JSONUtils.load(jsonFile);

			List<String> dependencyNames = new ArrayList<>();
			if (!isStdlib)
				dependencyNames.add("stdlib");

			JSONArray jsonDependencies = json.optJSONArray("dependencies");
			if (jsonDependencies != null) {
				for (int i = 0; i < jsonDependencies.length(); i++) {
					dependencyNames.add(jsonDependencies.getString(i));
				}
			}

			// TODO: annotation type registration
			ModuleSpace space = new ModuleSpace(unit, new ArrayList<>(), StorageType.getStandard());
			SemanticModule[] dependencies = new SemanticModule[dependencyNames.size()];
			for (int i = 0; i < dependencies.length; i++) {
				String dependencyName = dependencyNames.get(i);
				SemanticModule module = loader.getModule(dependencyName);
				dependencies[i] = module;
				
				try {
					space.addModule(dependencyName, module);
				} catch (CompileException ex) {
					throw new ConstructorException("Error: exception during compilation", ex);
				}
			}

			ParsedModule parsedModule = new ParsedModule(moduleName, directory, jsonFile, exceptionLogger);
			ZSPackage pkg = isStdlib ? unit.globalTypeRegistry.stdlib : new ZSPackage(null, parsedModule.packageName);
			Module module = new Module(moduleName);
			CompilingPackage compilingPackage = new CompilingPackage(pkg, module);
			
			ParsedFile[] parsedFiles = parsedModule.parse(compilingPackage);
			SemanticModule result = ParsedFile.compileSyntaxToSemantic(parsedModule.name, dependencies, compilingPackage, parsedFiles, space, exceptionLogger);
			
			JSONObject globals = json.optJSONObject("globals");
			if (globals != null) {
				for (String key : globals.keySet()) {
					JSONObject global = globals.getJSONObject(key);
					switch (global.getString("type")) {
						case "Definition":
							result.globals.put(key, new TypeSymbol(result.definitions.getDefinition(global.getString("definition"))));
							break;
						default:
							throw new ConstructorException("Invalid global type: " + global.getString("type"));
					}
				}
			}
			
			return result;
		} catch (IOException ex) {
			throw new ConstructorException("Loading module files failed: " + ex.getMessage(), ex);
		}
	}
	
	@Override
	public SourcePackage getRootPackage() {
		if (rootPackage == null)
			rootPackage = loadPackage("", new File(directory, "src"));
		
		return rootPackage;
	}
	
	private SourcePackage loadPackage(String name, File directory) {
		if (!directory.exists())
			throw new IllegalArgumentException("Directory does not exist: " + directory.getAbsolutePath());
		
		SourcePackage pkg = new SourcePackage(directory, name);
		
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				pkg.addPackage(loadPackage(file.getName(), file));
			} else if (file.isFile() && file.getName().toLowerCase().endsWith(".zs")) {
				pkg.addFile(new FileSourceFile(file.getName(), file));
			}
		}
		
		return pkg;
	}
}

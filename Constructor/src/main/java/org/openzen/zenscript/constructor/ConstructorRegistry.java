/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.compiler.Target;
import org.openzen.zenscript.compiler.TargetType;
import org.openzen.zenscript.compiler.ZenCodeCompiler;
import org.openzen.zenscript.javabytecode.JavaBytecodeModule;
import org.openzen.zenscript.javabytecode.JavaBytecodeRunUnit;
import org.openzen.zenscript.javabytecode.JavaCompiler;
import org.openzen.zenscript.javashared.JavaCompileSpace;
import org.openzen.zenscript.javashared.SimpleJavaCompileSpace;
import org.openzen.zenscript.javasource.JavaDirectoryOutput;
import org.openzen.zenscript.javasource.JavaSourceCompiler;
import org.openzen.zenscript.javasource.JavaSourceModule;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstructorRegistry {
	private static final Map<String, TargetType> targetTypes = new HashMap<>();
	
	static {
		registerTargetType("javaSource", new JavaSourceTargetType());
		registerTargetType("javaBytecodeJar", new JavaBytecodeTargetType());
		registerTargetType("javaBytecodeRun", new JavaBytecodeTargetType());
	}
	
	public static void registerTargetType(String name, TargetType type) {
		targetTypes.put(name, type);
	}
	
	public static TargetType getTargetType(String name) {
		return targetTypes.get(name);
	}

	private ConstructorRegistry() {}
	
	private static class JavaSourceTargetType implements TargetType {

		@Override
		public Target create(File projectDir, JSONObject definition) {
			return new JavaSourceTarget(projectDir, definition);
		}
	}
	
	private static class JavaSourceTarget implements Target {
		private final String module;
		private final String name;
		private final File output;

		public JavaSourceTarget(File projectDir, JSONObject definition) {
			module = definition.getString("module");
			name = definition.optString("name", "Java Source: " + module);
			output = new File(projectDir, definition.getString("output"));
		}

		@Override
		public ZenCodeCompiler createCompiler(SemanticModule module) {
			return new JavaSourceZenCompiler(output, module.registry);
		}

		@Override
		public String getModule() {
			return module;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean canRun() {
			return false;
		}

		@Override
		public boolean canBuild() {
			return true;
		}
	}
	
	private static class JavaSourceZenCompiler implements ZenCodeCompiler {
		private final File output;
		private final JavaSourceCompiler compiler;
		private final List<JavaSourceModule> modules = new ArrayList<>();
		
		public final GlobalTypeRegistry registry;
		private final SimpleJavaCompileSpace space;
		
		public JavaSourceZenCompiler(File output, GlobalTypeRegistry registry) {
			this.output = output;
			compiler = new JavaSourceCompiler(registry);
			this.registry = registry;
			space = new SimpleJavaCompileSpace(registry);
		}

		@Override
		public void addModule(SemanticModule module) {
			JavaSourceModule result = compiler.compile(module, space, module.modulePackage.fullName);
			modules.add(result);
			space.register(result);
		}

		@Override
		public void finish() {
			JavaDirectoryOutput output = new JavaDirectoryOutput(this.output);
			for (JavaSourceModule module : modules)
				output.add(module);
			output.add(compiler.helpers);
		}

		@Override
		public void run() {
			throw new UnsupportedOperationException();
		}
	}
	
	private static class JavaBytecodeTargetType implements TargetType {

		@Override
		public Target create(File projectDir, JSONObject definition) {
			return new JavaBytecodeJarTarget(definition);
		}
	}
	
	private static class JavaBytecodeJarTarget implements Target {
		private final String module;
		private final String name;
		private final File file;
		private final boolean debugCompiler;

		public JavaBytecodeJarTarget(JSONObject definition) {
			module = definition.getString("module");
			name = definition.getString("name");
			file = new File(definition.getString("output"));
			debugCompiler = definition.optBoolean("debugCompiler", false);
		}

		@Override
		public ZenCodeCompiler createCompiler(SemanticModule module) {
			return new JavaBytecodeJarCompiler();
		}

		@Override
		public String getModule() {
			return module;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean canRun() {
			return true;
		}

		@Override
		public boolean canBuild() {
			return true;
		}
	}
	
	private static class JavaBytecodeJarCompiler implements ZenCodeCompiler {
		private final ZSPackage root = ZSPackage.createRoot();
		private final ZSPackage stdlib = new ZSPackage(root, "stdlib");
		public final GlobalTypeRegistry registry = new GlobalTypeRegistry(stdlib);
		
		private final JavaCompiler compiler = new JavaCompiler();
		private final List<JavaBytecodeModule> modules = new ArrayList<>();
		private final JavaCompileSpace space = new SimpleJavaCompileSpace(registry);

		@Override
		public void addModule(SemanticModule module) {
			JavaBytecodeModule result = compiler.compile(module.modulePackage.fullName, module, space);
			modules.add(result);
		}

		@Override
		public void finish() {
			
		}

		@Override
		public void run() {
			JavaBytecodeRunUnit unit = new JavaBytecodeRunUnit();
			for (JavaBytecodeModule module : modules)
				unit.add(module);
			//unit.add(compiler.helpers);
			unit.run();
		}
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.compiler.ZenCodeCompiler;
import org.openzen.zenscript.compiler.ZenCodeCompilingModule;
import org.openzen.zenscript.javabytecode.compiler.JavaClassWriter;
import org.openzen.zenscript.javabytecode.compiler.JavaScriptFile;
import org.openzen.zenscript.javabytecode.compiler.JavaStatementVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javabytecode.compiler.definitions.JavaDefinitionVisitor;
import org.openzen.zenscript.javashared.JavaBaseCompiler;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaCompileSpace;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaContext;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.prepare.JavaPrepareDefinitionMemberVisitor;
import org.openzen.zenscript.javashared.prepare.JavaPrepareDefinitionVisitor;

/**
 * @author Hoofdgebruiker
 */
public class JavaCompiler extends JavaBaseCompiler implements ZenCodeCompiler {
	private final JavaModule target;
	private final Map<String, JavaScriptFile> scriptBlocks = new HashMap<>();
	private final JavaClassWriter scriptsClassWriter;
	private int generatedScriptBlockCounter = 0;
	private boolean finished = false;
	private final File jarFile;
	private final JavaBytecodeContext context;
	
	private final List<HighLevelDefinition> definitions = new ArrayList<>();
	private final List<ScriptBlock> scripts = new ArrayList<>();
	
	public JavaCompiler(GlobalTypeRegistry registry, File jarFile) {
		this(registry, false, jarFile);
	}

	public JavaCompiler(GlobalTypeRegistry registry, boolean debug, File jarFile) {
		target = new JavaModule(new File("classes"));
		this.jarFile = jarFile;
		this.context = new JavaBytecodeContext(registry, target);
		
		scriptsClassWriter = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
		scriptsClassWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Scripts", null, "java/lang/Object", null);
	}
	
	public JavaContext getContext() {
		return context;
	}
	
	//@Override
	public JavaBytecodeModule compile(SemanticModule module, JavaCompileSpace space) {
		context.addModule(module.module);
		
		JavaBytecodeModule target = new JavaBytecodeModule(module.module);
		for (HighLevelDefinition definition : module.definitions.getAll()) {
			JavaPrepareDefinitionMemberVisitor memberPreparer = new JavaPrepareDefinitionMemberVisitor(context, context.getJavaModule(definition.module));
			definition.accept(memberPreparer);
		}
		
		for (HighLevelDefinition definition : module.definitions.getAll()) {
			String className = getClassName(definition.position.getFilename());
			JavaScriptFile scriptFile = getScriptFile(className);
			
			target.addClass(definition.name, definition.accept(new JavaDefinitionVisitor(context, scriptFile.classWriter)));
		}
		
		for (ScriptBlock script : module.scripts) {
			final SourceFile sourceFile = script.getTag(SourceFile.class);
			final String className = getClassName(sourceFile == null ? null : sourceFile.getFilename());
			JavaScriptFile scriptFile = getScriptFile(script.pkg.fullName + "/" + className);

			String methodName = scriptFile.scriptMethods.isEmpty() ? "run" : "run" + scriptFile.scriptMethods.size();

			// convert scripts into methods (add them to a Scripts class?)
			// (TODO: can we break very long scripts into smaller methods? for the extreme scripts)
			final JavaClassWriter visitor = scriptFile.classWriter;
			JavaMethod method = JavaMethod.getStatic(new JavaClass(script.pkg.fullName, className, JavaClass.Kind.CLASS), methodName, "()V", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
			scriptFile.scriptMethods.add(method);

			final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, context.getJavaModule(script.module), new JavaWriter(visitor, method, null, null, null));
			statementVisitor.start();
			for (Statement statement : script.statements) {
				statement.accept(statementVisitor);
			}
			statementVisitor.end();
		}
		
		for (Map.Entry<String, JavaScriptFile> entry : scriptBlocks.entrySet()) {
			for (JavaMethod method : entry.getValue().scriptMethods)
				target.addScript(method);

			entry.getValue().classWriter.visitEnd();
			target.addClass(entry.getKey(), entry.getValue().classWriter.toByteArray());
		}

		return target;
	}
	
	@Override
	public ZenCodeCompilingModule addModule(SemanticModule module) {
		context.addModule(module.module);
		
		return new ZenCodeCompilingModule() {
			@Override
			public void addDefinition(HighLevelDefinition definition) {
				JavaPrepareDefinitionVisitor preparer = new JavaPrepareDefinitionVisitor(context, context.getJavaModule(module.module), definition.position.getFilename(), null);
				definition.accept(preparer);

				definitions.add(definition);
			}

			@Override
			public void addScriptBlock(ScriptBlock script) {
				scripts.add(script);
			}

			@Override
			public void finish() {
				
			}
		};
	}

	private String getClassName(String filename) {
		if (filename == null) {
			return "generatedBlock" + (generatedScriptBlockCounter++);
		} else {
			// TODO: remove special characters
			System.out.println("Writing script: " + filename);
			return filename.substring(0, filename.lastIndexOf('.')).replace("/", "_");
		}
	}

	private JavaScriptFile getScriptFile(String className) {
		if (!scriptBlocks.containsKey(className)) {
			JavaClassWriter scriptFileWriter = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
			scriptFileWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);
			scriptBlocks.put(className, new JavaScriptFile(scriptFileWriter));
		}

		return scriptBlocks.get(className);
	}

	@Override
	public void finish() {
		JavaModule module = finishAndGetModule();

		if (jarFile != null) {
			// TODO: write module to a Jar file
		}
	}

	@Override
	public void run() {
		if (!finished)
			throw new IllegalStateException("Not yet built!");
		
		// TODO: execute this
	}

	public JavaModule finishAndGetModule() {
		if (finished)
			throw new IllegalStateException("Already finished!");
		
		finished = true;
		
		for (HighLevelDefinition definition : definitions) {
			JavaPrepareDefinitionMemberVisitor memberPreparer = new JavaPrepareDefinitionMemberVisitor(context, context.getJavaModule(definition.module));
			definition.accept(memberPreparer);
		}
		
		for (HighLevelDefinition definition : definitions) {
			String className = getClassName(definition.position.getFilename());
			JavaScriptFile scriptFile = getScriptFile(className);

			target.register(definition.name, definition.accept(new JavaDefinitionVisitor(context, scriptFile.classWriter)));
		}
 
		for (ScriptBlock script : scripts) {
			final SourceFile sourceFile = script.getTag(SourceFile.class);
			final String className = getClassName(sourceFile == null ? null : sourceFile.getFilename());
			JavaScriptFile scriptFile = getScriptFile(className);

			String methodName = scriptFile.scriptMethods.isEmpty() ? "run" : "run" + scriptFile.scriptMethods.size();

			// convert scripts into methods (add them to a Scripts class?)
			// (TODO: can we break very long scripts into smaller methods? for the extreme scripts)
			final JavaClassWriter visitor = scriptFile.classWriter;
			JavaMethod method = JavaMethod.getStatic(new JavaClass(script.pkg.fullName, className, JavaClass.Kind.CLASS), methodName, "()V", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
			scriptFile.scriptMethods.add(method);

			final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, context.getJavaModule(script.module), new JavaWriter(visitor, method, null, null, null));
			statementVisitor.start();
			for (Statement statement : script.statements) {
				statement.accept(statementVisitor);
			}
			statementVisitor.end();
		}
		
		JavaMethod runMethod = JavaMethod.getStatic(new JavaClass("script", "Scripts", JavaClass.Kind.CLASS), "run", "()V", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
		final JavaWriter runWriter = new JavaWriter(scriptsClassWriter, runMethod, null, null, null);
		runWriter.start();
		for (Map.Entry<String, JavaScriptFile> entry : scriptBlocks.entrySet()) {
			for (JavaMethod method : entry.getValue().scriptMethods)
				runWriter.invokeStatic(method);

			entry.getValue().classWriter.visitEnd();
			target.register(entry.getKey(), entry.getValue().classWriter.toByteArray());
		}
		runWriter.ret();
		runWriter.end();

		target.register("Scripts", scriptsClassWriter.toByteArray());
		return target;
	}
}

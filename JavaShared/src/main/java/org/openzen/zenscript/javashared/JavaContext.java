/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;
import org.openzen.zenscript.codemodel.type.storage.UniqueStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class JavaContext {
	private final GlobalTypeRegistry registry;
	private final Map<String, JavaSynthesizedFunction> functions = new HashMap<>();
	private final Map<String, JavaSynthesizedRange> ranges = new HashMap<>();
	
	public JavaContext(GlobalTypeRegistry registry) {
		this.registry = registry;
		
		{
			TypeParameter t = new TypeParameter(CodePosition.BUILTIN, "T");
			TypeParameter u = new TypeParameter(CodePosition.BUILTIN, "U");
			TypeParameter v = new TypeParameter(CodePosition.BUILTIN, "V");
			
			functions.put("TToU", new JavaSynthesizedFunction(
					new JavaClass("java.util.function", "Function", JavaClass.Kind.INTERFACE),
					new TypeParameter[] { t, u },
					new FunctionHeader(registry.getGeneric(u).stored(BorrowStorageTag.INVOCATION), registry.getGeneric(t).stored(BorrowStorageTag.INVOCATION)),
					"apply"));
			
			functions.put("TUToV", new JavaSynthesizedFunction(
					new JavaClass("java.util.function", "BiFunction", JavaClass.Kind.INTERFACE),
					new TypeParameter[] { t, u, v },
					new FunctionHeader(registry.getGeneric(v).stored(BorrowStorageTag.INVOCATION), registry.getGeneric(t).stored(BorrowStorageTag.INVOCATION), registry.getGeneric(u).stored(BorrowStorageTag.INVOCATION)),
					"apply"));
			
			functions.put("TToVoid", new JavaSynthesizedFunction(
					new JavaClass("java.util.function", "Consumer", JavaClass.Kind.INTERFACE),
					new TypeParameter[] { t },
					new FunctionHeader(BasicTypeID.VOID.stored, registry.getGeneric(t).stored(BorrowStorageTag.INVOCATION)),
					"accept"));
			
			functions.put("TUToVoid", new JavaSynthesizedFunction(
					new JavaClass("java.util.function", "BiConsumer", JavaClass.Kind.INTERFACE),
					new TypeParameter[] { t, u },
					new FunctionHeader(BasicTypeID.VOID.stored, registry.getGeneric(t).stored(BorrowStorageTag.INVOCATION), registry.getGeneric(u).stored(BorrowStorageTag.INVOCATION)),
					"accept"));
			
			functions.put("TToBool", new JavaSynthesizedFunction(
					new JavaClass("java.util.function", "Predicate", JavaClass.Kind.INTERFACE),
					new TypeParameter[] { t },
					new FunctionHeader(BasicTypeID.BOOL.stored, registry.getGeneric(t).stored(BorrowStorageTag.INVOCATION)),
					"test"));
		}
	}
	
	protected abstract JavaSyntheticClassGenerator getTypeGenerator();
			
	public abstract String getDescriptor(TypeID type);
	
	public abstract String getDescriptor(StoredType type);
	
	public String getMethodDescriptor(FunctionHeader header) {
		return getMethodDescriptor(header, false);
	}
	
    public String getMethodSignature(FunctionHeader header) {
        return new JavaTypeGenericVisitor(this).getGenericMethodSignature(header);
    }
	
	public String getEnumConstructorDescriptor(FunctionHeader header) {
		return getMethodDescriptor(header, true);
	}
	
	public JavaSynthesizedFunctionInstance getFunction(FunctionTypeID type) {
		String id = getFunctionId(type.header);
		JavaSynthesizedFunction function;
		if (!functions.containsKey(id)) {
			JavaClass cls = new JavaClass("zsynthetic", "Function" + id, JavaClass.Kind.INTERFACE);
			List<TypeParameter> typeParameters = new ArrayList<>();
			List<FunctionParameter> parameters = new ArrayList<>();
			for (FunctionParameter parameter : type.header.parameters) {
				JavaTypeInfo typeInfo = JavaTypeInfo.get(parameter.type);
				if (typeInfo.primitive) {
					parameters.add(new FunctionParameter(parameter.type, Character.toString((char)('a' + parameters.size()))));
				} else {
					TypeParameter typeParameter = new TypeParameter(CodePosition.BUILTIN, getTypeParameter(typeParameters.size()));
					typeParameters.add(typeParameter);
					parameters.add(new FunctionParameter(registry.getGeneric(typeParameter).stored(BorrowStorageTag.INVOCATION), Character.toString((char)('a' + parameters.size()))));
				}
			}
			StoredType returnType;
			{
				JavaTypeInfo typeInfo = JavaTypeInfo.get(type.header.getReturnType());
				if (typeInfo.primitive) {
					returnType = type.header.getReturnType();
				} else {
					TypeParameter typeParameter = new TypeParameter(CodePosition.BUILTIN, getTypeParameter(typeParameters.size()));
					typeParameters.add(typeParameter);
					returnType = registry.getGeneric(typeParameter).stored(UniqueStorageTag.INSTANCE);
				}
			}
			function = new JavaSynthesizedFunction(
					cls,
					typeParameters.toArray(new TypeParameter[typeParameters.size()]),
					new FunctionHeader(returnType, parameters.toArray(new FunctionParameter[parameters.size()])),
					"invoke");
			
			functions.put(id, function);
			getTypeGenerator().synthesizeFunction(function);
		} else {
			function = functions.get(id);
		}
		
		List<TypeID> typeArguments = new ArrayList<>();
		for (FunctionParameter parameter : type.header.parameters) {
			JavaTypeInfo typeInfo = JavaTypeInfo.get(parameter.type);
			if (!typeInfo.primitive) {
				typeArguments.add(parameter.type.type);
			}
		}
		if (!JavaTypeInfo.isPrimitive(type.header.getReturnType()))
			typeArguments.add(type.header.getReturnType().type);
		
		return new JavaSynthesizedFunctionInstance(function, typeArguments.toArray(new TypeID[typeArguments.size()]));
	}
	
	private String getFunctionId(FunctionHeader header) {
		StringBuilder signature = new StringBuilder();
		int typeParameterIndex = 0;
		for (FunctionParameter parameter : header.parameters) {
			JavaTypeInfo typeInfo = JavaTypeInfo.get(parameter.type);
			String id = typeInfo.primitive ? parameter.type.type.accept(parameter.type, new JavaSyntheticTypeSignatureConverter()) : getTypeParameter(typeParameterIndex++);
			signature.append(id);
		}
		signature.append("To");
		{
			JavaTypeInfo typeInfo = JavaTypeInfo.get(header.getReturnType());
			String id = typeInfo.primitive ? header.getReturnType().type.accept(header.getReturnType(), new JavaSyntheticTypeSignatureConverter()) : getTypeParameter(typeParameterIndex++);
			signature.append(id);
		}
		return signature.toString();
	}
	
	private String getTypeParameter(int index) {
	switch (index) {
			case 0: return "T";
			case 1: return "U";
			case 2: return "V";
			case 3: return "W";
			case 4: return "X";
			case 5: return "Y";
			case 6: return "Z";
			default: return "T" + index;
		}
	}
	
	public JavaSynthesizedClass getRange(RangeTypeID type) {
		JavaTypeInfo typeInfo = JavaTypeInfo.get(type.baseType);
		String id = typeInfo.primitive ? type.accept(null, new JavaSyntheticTypeSignatureConverter()) : "T";
		JavaSynthesizedRange range;
		if (!ranges.containsKey(id)) {
			JavaClass cls = new JavaClass("zsynthetic", id + "Range", JavaClass.Kind.CLASS);
			if (typeInfo.primitive) {
				range = new JavaSynthesizedRange(cls, TypeParameter.NONE, type.baseType);
			} else {
				TypeParameter typeParameter = new TypeParameter(CodePosition.BUILTIN, "T");
				range = new JavaSynthesizedRange(cls, new TypeParameter[] { typeParameter }, registry.getGeneric(typeParameter).stored(BorrowStorageTag.INVOCATION));
			}
			ranges.put(id, range);
			getTypeGenerator().synthesizeRange(range);
		} else {
			range = ranges.get(id);
		}
		
		if (typeInfo.primitive) {
			return new JavaSynthesizedClass(range.cls, TypeID.NONE);
		} else {
			return new JavaSynthesizedClass(range.cls, new TypeID[] { type.baseType.type });
		}
	}
	
	private String getMethodDescriptor(FunctionHeader header, boolean isEnumConstructor) {
        StringBuilder descBuilder = new StringBuilder("(");
        if (isEnumConstructor)
            descBuilder.append("Ljava/lang/String;I");
		
        for (FunctionParameter parameter : header.parameters) {
			descBuilder.append(getDescriptor(parameter.type));
        }
        descBuilder.append(")");
        descBuilder.append(getDescriptor(header.getReturnType()));
        return descBuilder.toString();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ConstTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.ITypeVisitor;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaOptionalTypeClassVisitor implements ITypeVisitor<Class> {
	private final JavaTypeClassVisitor base;
	
	public JavaOptionalTypeClassVisitor(JavaTypeClassVisitor base) {
		this.base = base;
	}

	@Override
	public Class visitBasic(BasicTypeID basic) {
		switch (basic) {
			case VOID:
				return void.class;
			case NULL:
				return Object.class;
			case ANY:
				return Object.class; // TODO
			case BOOL:
				return Boolean.class;
			case BYTE:
			case SBYTE:
				return Byte.class;
			case SHORT:
			case USHORT:
				return Short.class;
			case INT:
			case UINT:
				return Integer.class;
			case LONG:
			case ULONG:
				return Long.class;
			case FLOAT:
				return Float.class;
			case DOUBLE:
				return Double.class;
			case CHAR:
				return Character.class;
			case STRING:
				return String.class;
			default:
				throw new IllegalArgumentException("Invalid type: " + basic);
		}
	}

	@Override
	public Class visitArray(ArrayTypeID array) {
		return base.visitArray(array);
	}

	@Override
	public Class visitAssoc(AssocTypeID assoc) {
		return base.visitAssoc(assoc);
	}

	@Override
	public Class visitIterator(IteratorTypeID iterator) {
		return base.visitIterator(iterator);
	}

	@Override
	public Class visitFunction(FunctionTypeID function) {
		return base.visitFunction(function);
	}

	@Override
	public Class visitDefinition(DefinitionTypeID definition) {
		return base.visitDefinition(definition);
	}

	@Override
	public Class visitGeneric(GenericTypeID generic) {
		return base.visitGeneric(generic);
	}

	@Override
	public Class visitRange(RangeTypeID range) {
		return base.visitRange(range);
	}

	@Override
	public Class visitConst(ConstTypeID type) {
		return base.visitConst(type);
	}

	@Override
	public Class visitOptional(OptionalTypeID optional) {
		return base.visitOptional(optional);
	}

	@Override
	public Class visitGenericMap(GenericMapTypeID map) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
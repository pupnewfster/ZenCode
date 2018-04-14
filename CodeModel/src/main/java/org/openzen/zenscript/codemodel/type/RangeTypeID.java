/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Map;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class RangeTypeID implements ITypeID {
	public final ITypeID from;
	public final ITypeID to;
	
	public RangeTypeID(ITypeID from, ITypeID to) {
		this.from = from;
		this.to = to;
	}
	
	@Override
	public ITypeID withGenericArguments(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> arguments) {
		return registry.getRange(
				from.withGenericArguments(registry, arguments),
				to.withGenericArguments(registry, arguments));
	}

	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitRange(this);
	}
	
	@Override
	public RangeTypeID getUnmodified() {
		return this;
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public boolean isConst() {
		return false;
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return from.hasInferenceBlockingTypeParameters(parameters) || to.hasInferenceBlockingTypeParameters(parameters);
	}

	@Override
	public String toCamelCaseName() {
		if (from == BasicTypeID.INT && to == BasicTypeID.INT) {
			return "Range";
		} else if (from == to) {
			return from.toCamelCaseName() + "Range";
		} else {
			return from.toCamelCaseName() + to.toCamelCaseName() + "Range";
		}
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 89 * hash + from.hashCode();
		hash = 89 * hash + to.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final RangeTypeID other = (RangeTypeID) obj;
		return this.from == other.from && this.to == other.to;
	}
	
	@Override
	public String toString() {
		return from.toString() + " .. " + to.toString();
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class AliasDefinition extends HighLevelDefinition {
	public ITypeID type;
	
	public AliasDefinition(CodePosition position, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, pkg, name, modifiers, outerDefinition);
	}
	
	public void setType(ITypeID type) {
		this.type = type;
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitAlias(this);
	}
}

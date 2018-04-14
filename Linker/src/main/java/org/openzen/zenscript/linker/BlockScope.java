/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.linker;

import java.util.List;
import java.util.function.Function;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class BlockScope extends StatementScope {
	private final StatementScope parent;
	
	public BlockScope(StatementScope parent) {
		this.parent = parent;
	}
	
	@Override
	public LocalMemberCache getMemberCache() {
		return parent.getMemberCache();
	}
	
	@Override
	public IPartialExpression get(CodePosition position, GenericName name) {
		IPartialExpression fromSuper = super.get(position, name);
		if (fromSuper != null)
			return fromSuper;
		
		return parent.get(position, name);
	}

	@Override
	public Statement getLoop(String name) {
		return parent.getLoop(name);
	}

	@Override
	public FunctionHeader getFunctionHeader() {
		return parent.getFunctionHeader();
	}

	@Override
	public ITypeID getType(CodePosition position, List<GenericName> name) {
		return parent.getType(position, name);
	}

	@Override
	public ITypeID getThisType() {
		return parent.getThisType();
	}

	@Override
	public Function<CodePosition, Expression> getDollar() {
		return parent.getDollar();
	}
	
	@Override
	public IPartialExpression getOuterInstance(CodePosition position) {
		return parent.getOuterInstance(position);
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.OrOrExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.PrecompilationState;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionOrOr extends ParsedExpression {
	private final ParsedExpression left;
	private final ParsedExpression right;

	public ParsedExpressionOrOr(CodePosition position, ParsedExpression left, ParsedExpression right) {
		super(position);

		this.left = left;
		this.right = right;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		Expression cLeft = left.compile(scope.withHints(BasicTypeID.HINT_BOOL)).eval().castImplicit(position, scope, BasicTypeID.BOOL);
		Expression cRight = right.compile(scope.withHints(BasicTypeID.HINT_BOOL)).eval().castImplicit(position, scope, BasicTypeID.BOOL);
		return new OrOrExpression(position, cLeft, cRight);
	}

	@Override
	public boolean hasStrongType() {
		return left.hasStrongType() && right.hasStrongType();
	}

	@Override
	public ITypeID precompileForType(ExpressionScope scope, PrecompilationState state) {
		ITypeID leftType = left.precompileForType(scope, state);
		ITypeID rightType = right.precompileForType(scope, state);
		if (leftType == null || rightType == null)
			return null;
		
		return scope.getTypeMembers(leftType).union(rightType);
	}
}

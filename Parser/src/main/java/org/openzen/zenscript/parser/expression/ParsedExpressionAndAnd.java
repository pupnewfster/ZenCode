/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.expression.AndAndExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionAndAnd extends ParsedExpression {
	private final ParsedExpression left;
	private final ParsedExpression right;

	public ParsedExpressionAndAnd(
			CodePosition position,
			ParsedExpression left,
			ParsedExpression right) {
		super(position);

		this.left = left;
		this.right = right;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		Expression left = this.left.compile(scope).eval();
		Expression right = this.right.compile(scope).eval();
		
		ITypeID resultType = scope.getTypeMembers(left.getType()).union(right.getType());
		if (resultType == null)
			throw new CompileException(position, CompileExceptionCode.TYPE_CANNOT_UNITE, "These types could not be unified: " + left.getType() + " and " + right.getType());
		
		left = left.castImplicit(position, scope, resultType);
		right = right.castImplicit(position, scope, resultType);
		
		return new AndAndExpression(position, left, right);
	}

	@Override
	public boolean hasStrongType() {
		return left.hasStrongType() && right.hasStrongType();
	}
}

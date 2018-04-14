/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class CoalesceExpression extends Expression {
	public final Expression left;
	public final Expression right;
	
	public CoalesceExpression(CodePosition position, Expression left, Expression right) {
		super(position, right.type);
		
		this.left = left;
		this.right = right;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCoalesce(this);
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 * Checks if two values are identical (===).
 * 
 * @author Hoofdgebruiker
 */
public class EqualsExpression extends Expression {
	public final Expression left;
	public final Expression right;
	
	public EqualsExpression(CodePosition position, Expression left, Expression right) {
		super(position, BasicTypeID.BOOL);
		
		this.left = left;
		this.right = right;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitEquals(this);
	}
}

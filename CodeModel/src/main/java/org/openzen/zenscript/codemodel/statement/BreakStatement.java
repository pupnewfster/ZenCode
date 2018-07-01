/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import java.util.function.Consumer;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.ConcatMap;

/**
 *
 * @author Hoofdgebruiker
 */
public class BreakStatement extends Statement {
	public final LoopStatement target;
	
	public BreakStatement(CodePosition position, LoopStatement target) {
		super(position, null);
		
		this.target = target;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitBreak(this);
	}
	
	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		return modified.containsKey(target) ? new BreakStatement(position, modified.get(target)) : this;
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		return modified.containsKey(target) ? new BreakStatement(position, modified.get(target)) :  this;
	}
}

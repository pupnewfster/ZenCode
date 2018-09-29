/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionMember extends ParsedExpression {
	private final ParsedExpression value;
	private final String member;
	private final List<IParsedType> genericParameters;

	public ParsedExpressionMember(CodePosition position, ParsedExpression value, String member, List<IParsedType> genericParameters) {
		super(position);

		this.value = value;
		this.member = member;
		this.genericParameters = genericParameters;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		IPartialExpression cValue = value.compile(scope.withoutHints());
		TypeID[] typeParameters = IParsedType.compileList(genericParameters, scope);
		IPartialExpression member = cValue.getMember(
				position,
				scope,
				scope.hints,
				new GenericName(this.member, typeParameters));
		if (member == null)
			return new InvalidExpression(position, CompileExceptionCode.NO_SUCH_MEMBER, "Member not found: " + this.member);
		
		return member;
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}

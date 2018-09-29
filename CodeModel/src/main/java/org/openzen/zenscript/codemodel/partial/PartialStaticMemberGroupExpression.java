/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.partial;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class PartialStaticMemberGroupExpression implements IPartialExpression {
	public static PartialStaticMemberGroupExpression forMethod(CodePosition position, TypeScope scope, String name, TypeID target, FunctionalMemberRef method, TypeID[] typeArguments) {
		TypeMemberGroup group = new TypeMemberGroup(true, name);
		group.addMethod(method, TypeMemberPriority.SPECIFIED);
		return new PartialStaticMemberGroupExpression(position, scope, target, group, typeArguments);
	}
	
	private final CodePosition position;
	private final TypeScope scope;
	private final TypeID target;
	private final TypeMemberGroup group;
	private final TypeID[] typeArguments;
	
	public PartialStaticMemberGroupExpression(CodePosition position, TypeScope scope, TypeID target, TypeMemberGroup group, TypeID[] typeArguments) {
		this.position = position;
		this.scope = scope;
		this.group = group;
		this.target = target;
		this.typeArguments = typeArguments;
	}
	
	@Override
	public Expression eval() {
		return group.staticGetter(position, scope);
	}

	@Override
	public List<StoredType>[] predictCallTypes(TypeScope scope, List<StoredType> hints, int arguments) {
		return group.predictCallTypes(scope, hints, arguments);
	}
	
	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<StoredType> hints, int arguments) {
		return group.getMethodMembers().stream()
				.filter(method -> method.member.getHeader().parameters.length == arguments && method.member.isStatic())
				.map(method -> method.member.getHeader())
				.collect(Collectors.toList());
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<StoredType> hints, GenericName name) {
		return eval().getMember(position, scope, hints, name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<StoredType> hints, CallArguments arguments) {
		return group.callStatic(position, target, scope, arguments);
	}
	
	@Override
	public Expression assign(CodePosition position, TypeScope scope, Expression value) {
		return group.staticSetter(position, scope, value);
	}

	@Override
	public TypeID[] getGenericCallTypes() {
		return typeArguments;
	}
	
	@Override
	public List<StoredType> getAssignHints() {
		if (group.getSetter() != null)
			return Collections.singletonList(group.getSetter().getType());
		if (group.getField() != null)
			return Collections.singletonList(group.getField().getType());
		
		return Collections.emptyList();
	}
}

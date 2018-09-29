/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedCallArguments {
	public static final ParsedCallArguments NONE = new ParsedCallArguments(null, Collections.emptyList());
	
	public static ParsedCallArguments parse(ZSTokenParser tokens) throws ParseException {
		List<IParsedType> typeArguments = IParsedType.parseTypeArgumentsForCall(tokens);
		
		tokens.required(ZSTokenType.T_BROPEN, "( expected");
		
		List<ParsedExpression> arguments = new ArrayList<>();
		try {
			if (tokens.optional(ZSTokenType.T_BRCLOSE) == null) {
				do {
					arguments.add(ParsedExpression.parse(tokens));
				} while (tokens.optional(ZSTokenType.T_COMMA) != null);
				tokens.required(ZSTokenType.T_BRCLOSE, ") expected");
			}
		} catch (ParseException ex) {
			tokens.logError(ex);
			tokens.recoverUntilToken(ZSTokenType.T_BRCLOSE);
		}
		
		return new ParsedCallArguments(typeArguments, arguments);
	}
	
	public static ParsedCallArguments parseForAnnotation(ZSTokenParser tokens) throws ParseException {
		List<IParsedType> typeArguments = IParsedType.parseTypeArgumentsForCall(tokens);
		
		List<ParsedExpression> arguments = new ArrayList<>();
		if (tokens.isNext(ZSTokenType.T_BROPEN)) {
			tokens.required(ZSTokenType.T_BROPEN, "( expected");
			try {
				if (tokens.optional(ZSTokenType.T_BRCLOSE) == null) {
					do {
						arguments.add(ParsedExpression.parse(tokens));
					} while (tokens.optional(ZSTokenType.T_COMMA) != null);
					tokens.required(ZSTokenType.T_BRCLOSE, ") expected");
				}
			} catch (ParseException ex) {
				tokens.logError(ex);
				tokens.recoverUntilToken(ZSTokenType.T_BRCLOSE);
			}
		}
		
		return new ParsedCallArguments(typeArguments, arguments);
	}
	
	private final List<IParsedType> typeArguments;
	public final List<ParsedExpression> arguments;
	
	public ParsedCallArguments(List<IParsedType> typeArguments, List<ParsedExpression> arguments) {
		this.typeArguments = typeArguments;
		this.arguments = arguments;
	}
	
	public CallArguments compileCall(
			CodePosition position, 
			ExpressionScope scope,
			TypeID[] genericParameters,
			TypeMemberGroup member) throws CompileException
	{
		List<FunctionHeader> possibleHeaders = member.getMethodMembers().stream()
				.map(method -> method.member.getHeader())
				.collect(Collectors.toList());
		return compileCall(position, scope, genericParameters, possibleHeaders);
	}
	
	public CallArguments compileCall(
			CodePosition position,
			ExpressionScope scope,
			TypeID[] typeArguments,
			List<FunctionHeader> candidateFunctions) throws CompileException
	{
		if (this.typeArguments != null) {
			typeArguments = new TypeID[this.typeArguments.size()];
			for (int i = 0; i < this.typeArguments.size(); i++)
				typeArguments[i] = this.typeArguments.get(i).compileUnstored(scope);
		}
		
		List<FunctionHeader> candidates = new ArrayList<>();
		for (FunctionHeader header : candidateFunctions) {
			if (isCompatibleWith(scope, header, typeArguments))
				candidates.add(header);
		}
		
		if (candidates.isEmpty()) {
			StringBuilder explanation = new StringBuilder();
			CallArguments arguments = compileCallNaive(position, scope);
			for (FunctionHeader candidate : candidateFunctions)
				explanation.append(candidate.explainWhyIncompatible(scope, arguments)).append("\n");
			throw new CompileException(position, CompileExceptionCode.CALL_NO_VALID_METHOD, "No compatible methods found: \n" + explanation.toString());
		}

		ExpressionScope innerScope = scope;
		if (candidates.size() == 1) {
			innerScope = scope.forCall(candidates.get(0));
		} else {
			candidates = candidates.stream()
					.filter(candidate -> candidate.getNumberOfTypeParameters() == 0)
					.collect(Collectors.toList());
			
			if (candidates.isEmpty()) {
				throw new CompileException(position, CompileExceptionCode.CALL_NO_VALID_METHOD, "Could not determine call type parameters");
			}
		}
		
		List<StoredType>[] predictedTypes = new List[arguments.size()];
		for (int i = 0; i < predictedTypes.length; i++)
			predictedTypes[i] = new ArrayList<>();
		
		for (FunctionHeader header : candidates) {
			for (int i = 0; i < arguments.size(); i++) {
				if (!predictedTypes[i].contains(header.parameters[i].type))
					predictedTypes[i].add(header.parameters[i].type);
			}
		}
		
		Expression[] cArguments = new Expression[arguments.size()];
		for (int i = 0; i < cArguments.length; i++) {
			IPartialExpression cArgument = arguments.get(i).compile(innerScope.withHints(predictedTypes[i]));
			cArguments[i] = cArgument.eval();
		}
		
		TypeID[] typeArguments2 = typeArguments;
		if (typeArguments2 == null || typeArguments2.length == 0) {
			for (FunctionHeader candidate : candidates) {
				if (candidate.typeParameters != null) {
					typeArguments2 = new TypeID[candidate.typeParameters.length];
					for (int i = 0; i < typeArguments2.length; i++) {
						if (innerScope.genericInferenceMap.get(candidate.typeParameters[i]) == null)
							typeArguments2[i] = new InvalidTypeID(position, CompileExceptionCode.TYPE_ARGUMENTS_NOT_INFERRABLE, "Could not infer type parameter " + candidate.typeParameters[i].name);
						else
							typeArguments2[i] = innerScope.genericInferenceMap.get(candidate.typeParameters[i]);
					}

					break;
				}
			}
		}
		
		return new CallArguments(typeArguments2, cArguments);
	}
	
	
	public CallArguments compileCall(
			CodePosition position,
			ExpressionScope scope,
			TypeID[] typeArguments,
			FunctionHeader function) throws CompileException
	{
		ExpressionScope innerScope = scope.forCall(function);
		
		List<StoredType>[] predictedTypes = new List[arguments.size()];
		for (int i = 0; i < predictedTypes.length; i++) {
			predictedTypes[i] = new ArrayList<>();
			predictedTypes[i].add(function.parameters[i].type);
		}
		
		Expression[] cArguments = new Expression[arguments.size()];
		for (int i = 0; i < cArguments.length; i++) {
			IPartialExpression cArgument = arguments.get(i).compile(innerScope.withHints(predictedTypes[i]));
			cArguments[i] = cArgument.eval();
		}
		
		TypeID[] typeArguments2 = typeArguments;
		if (typeArguments2 == null) {
			if (function.typeParameters != null) {
				typeArguments2 = new TypeID[function.typeParameters.length];
				for (int i = 0; i < typeArguments2.length; i++) {
					if (innerScope.genericInferenceMap.get(function.typeParameters[i]) == null)
						throw new CompileException(position, CompileExceptionCode.TYPE_ARGUMENTS_NOT_INFERRABLE, "Could not infer type parameter " + function.typeParameters[i].name);
					else
						typeArguments2[i] = innerScope.genericInferenceMap.get(function.typeParameters[i]);
				}
			}
		}
		
		return new CallArguments(typeArguments2, cArguments);
	}
	
	private CallArguments compileCallNaive(CodePosition position, ExpressionScope scope) {
		Expression[] cArguments = new Expression[arguments.size()];
		for (int i = 0; i < cArguments.length; i++) {
			IPartialExpression cArgument = arguments.get(i).compile(scope);
			cArguments[i] = cArgument.eval();
		}
		return new CallArguments(TypeID.NONE, cArguments);
	}
	
	private boolean isCompatibleWith(BaseScope scope, FunctionHeader header, TypeID[] typeParameters) {
		if (arguments.size() != header.parameters.length)
			return false;
		
		for (int i = 0; i < arguments.size(); i++) {
			if (typeParameters == null && header.typeParameters != null && header.parameters[i].type.hasInferenceBlockingTypeParameters(header.typeParameters))
				return false;
			
			if (!arguments.get(i).isCompatibleWith(scope, header.parameters[i].type.getNormalized()))
				return false;
		}
		
		return true;
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.parser.expression;

import org.openzen.zencode.IZenCompileEnvironment;
import org.openzen.zencode.symbolic.scope.IMethodScope;
import org.openzen.zencode.symbolic.expression.IPartialExpression;
import org.openzen.zencode.runtime.IAny;
import org.openzen.zencode.symbolic.type.TypeInstance;
import org.openzen.zencode.util.CodePosition;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionVariable extends ParsedExpression
{
	private final String name;

	public ParsedExpressionVariable(CodePosition position, String name)
	{
		super(position);

		this.name = name;
	}

	@Override
	public String asIdentifier()
	{
		return name;
	}

	@Override
	public <E extends IPartialExpression<E>>
		 IPartialExpression<E> compilePartial(IMethodScope<E> scope, TypeInstance<E> predictedType)
	{
		IPartialExpression<E> result = scope.getValue(name, getPosition(), scope);
		if (result == null) {
			if (predictedType == null) {
				scope.getErrorLogger().errorCouldNotResolveSymbol(getPosition(), name);
				return scope.getExpressionCompiler().invalid(getPosition(), scope);
			}

			// enable usage of static members of the same type as the predicted type (eg. enum values)
			IPartialExpression<E> member = predictedType.getStaticMember(getPosition(), scope, name);
			if (member == null || member.getType().getCastingRule(predictedType) == null) {
				scope.getErrorLogger().errorCouldNotResolveSymbol(getPosition(), name);
				return scope.getExpressionCompiler().invalid(getPosition(), scope, predictedType);
			} else
				return member;
		} else
			return result;
	}

	@Override
	public <E extends IPartialExpression<E>>
		 E compileKey(IMethodScope<E> scope, TypeInstance<E> predictedType)
	{
		return scope.getExpressionCompiler().constantString(getPosition(), scope, name);
	}

	@Override
	public IAny eval(IZenCompileEnvironment<?> environment)
	{
		return environment.evalGlobal(name);
	}
}
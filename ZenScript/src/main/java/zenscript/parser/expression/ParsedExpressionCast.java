/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package zenscript.parser.expression;

import stanhebben.zenscript.IZenCompileEnvironment;
import stanhebben.zenscript.compiler.IScopeMethod;
import stanhebben.zenscript.expression.partial.IPartialExpression;
import stanhebben.zenscript.type.ZenType;
import zenscript.parser.type.IParsedType;
import zenscript.runtime.IAny;
import zenscript.util.ZenPosition;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionCast extends ParsedExpression {
	private final ParsedExpression value;
	private final IParsedType type;
	
	public ParsedExpressionCast(ZenPosition position, ParsedExpression value, IParsedType type) {
		super(position);
		
		this.value = value;
		this.type = type;
	}

	@Override
	public IPartialExpression compilePartial(IScopeMethod environment, ZenType predictedType) {
		ZenType compiledType = type.compile(environment);
		
		return value.compile(environment, compiledType).cast(getPosition(), compiledType);
	}

	@Override
	public IAny eval(IZenCompileEnvironment environment) {
		// not (yet?) possible
		return null;
	}
}

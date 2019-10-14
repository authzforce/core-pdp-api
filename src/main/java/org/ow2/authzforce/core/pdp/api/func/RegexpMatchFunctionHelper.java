/**
 * Copyright 2012-2019 THALES.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.api.func;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.XmlUtils;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.Expressions;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import net.sf.saxon.Version;
import net.sf.saxon.regex.RegularExpression;
import net.sf.saxon.trans.XPathException;

/**
 * *-regexp-match function helper
 * <p>
 * WARNING: the regular expression syntax required by XACML refers to the <code>xf:matches</code> function from [XF] (see the XACML core spec for this reference). This function and associated syntax
 * differ from {@link Pattern} (Java 7) in several ways. Therefore, we cannot use {@link Pattern} directly. Find examples of differences below:
 * <ul>
 * <li>{@link Pattern} matches the entire string against the pattern always, whereas <code>xf:matches</code> considers the string to match the pattern if any substring matches the pattern.</li>
 * <li><code>xf:matches</code> regular expression syntax is based on XML schema which defines character class substraction using '-' character, whereas {@link Pattern} does not support this syntax but
 * <code>&&[^</code> instead.</li>
 * <li>Category escape: can be done in XML SCHEMA with: <code>[\P{X}]</code>. {@link Pattern} only supports this form: <code>[^\p{X}]</code>.</li>
 * <li>Character classes: XML schema define categories <code>\c</code> and <code>\C</code>. {@link Pattern} does not support them.</li>
 * </ul>
 * EXAMPLE: this regex from XML schema spec uses character class substraction. It is valid for <code>xf:matches</code> but does not compile with {@link Pattern}:
 * 
 * <pre>
 * [\i-[:]][\c-[:]]*
 * </pre>
 * 
 */
public final class RegexpMatchFunctionHelper
{
	private static final class CompiledRegexMatchFunctionCall extends BaseFirstOrderFunctionCall<BooleanValue>
	{
		private final RegularExpression compiledRegex;
		private final List<Expression<?>> argExpressionsAfterRegex;
		private final Datatype<? extends SimpleValue<String>> matchedValType;
		private final String invalidRemainingArg1TypeMsg;
		private final String funcId;

		private CompiledRegexMatchFunctionCall(final FirstOrderFunctionSignature<BooleanValue> functionSig, final List<Expression<?>> argExpressions, final Datatype<?>[] remainingArgTypes,
		        final RegularExpression compiledRegex, final Datatype<? extends SimpleValue<String>> matchedValueType, final String invalidRemainingArg1TypeMsg) throws IllegalArgumentException
		{
			super(functionSig, argExpressions, remainingArgTypes);
			this.funcId = functionSig.getName();
			this.compiledRegex = compiledRegex;
			/*
			 * We can remove the first arg from argExpressions since it is already the compiledRegex.
			 */
			this.argExpressionsAfterRegex = argExpressions.subList(1, argExpressions.size());
			this.matchedValType = matchedValueType;
			this.invalidRemainingArg1TypeMsg = invalidRemainingArg1TypeMsg;
		}

		@Override
		public BooleanValue evaluate(final EvaluationContext context, final AttributeValue... remainingArgs) throws IndeterminateEvaluationException
		{
			final SimpleValue<String> arg1;
			if (argExpressionsAfterRegex.isEmpty())
			{
				// no more arg in argExpressions, so next arg is in remainingArgs
				try
				{
					arg1 = matchedValType.cast(remainingArgs[0]);
				}
				catch (final ClassCastException e)
				{
					throw new IndeterminateEvaluationException(invalidRemainingArg1TypeMsg, XacmlStatusCode.PROCESSING_ERROR.value(), e);
				}
			}
			else
			{
				try
				{
					arg1 = Expressions.eval(argExpressionsAfterRegex.get(0), context, matchedValType);
				}
				catch (final IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException("Function " + this.funcId + ": Indeterminate arg #1", XacmlStatusCode.PROCESSING_ERROR.value(), e);
				}
			}

			return BooleanValue.valueOf(compiledRegex.containsMatch(arg1.getUnderlyingValue()));

		}
	}

	/**
	 * Match a string against a regular expression
	 * 
	 * @param regex
	 *            regular expression
	 * @param arg1
	 *            string value
	 * @return true iff {@code arg1} matches {@code regex}
	 * @throws IllegalArgumentException
	 *             {@code regex} is not a valid regular expression
	 */
	public static boolean match(final StringValue regex, final SimpleValue<String> arg1) throws IllegalArgumentException
	{
		/*
		 * From Saxon xf:matches() implementation: Matches#evaluateItem() / evalMatches()
		 */
		final RegularExpression compiledRegex;
		try
		{
			compiledRegex = Version.platform.compileRegularExpression(XmlUtils.SAXON_PROCESSOR.getUnderlyingConfiguration(), regex.getUnderlyingValue(), "", "XP20", null);
		}
		catch (final XPathException e)
		{
			throw new PatternSyntaxException("Invalid regular expression arg", regex.getUnderlyingValue(), -1);
		}

		return compiledRegex.containsMatch(arg1.getUnderlyingValue());
	}

	private final String indeterminateArg1TypeMessage;
	private final FirstOrderFunctionSignature<BooleanValue> funcSig;
	private final Datatype<? extends SimpleValue<String>> matchedValueType;
	private final String invalidRegexMsg;

	/**
	 * Creates a "regex-match" (matching regular expressions) function helper
	 * 
	 * @param matchFunctionSignature
	 *            signature of function that {@link #getCompiledRegexMatchCall(List, Datatype...)} must create
	 * @param matchedDatatype
	 *            datatype of value to be matched against the regular expression
	 */
	public RegexpMatchFunctionHelper(final FirstOrderFunctionSignature<BooleanValue> matchFunctionSignature, final Datatype<? extends SimpleValue<String>> matchedDatatype)
	{
		this.funcSig = matchFunctionSignature;
		this.matchedValueType = matchedDatatype;
		this.indeterminateArg1TypeMessage = "Function " + funcSig.getName() + ": Invalid type (expected = " + matchedDatatype + ") of arg #1: ";
		this.invalidRegexMsg = "Function " + funcSig.getName() + ": Invalid regular expression in arg #0 (evaluated as static expression): '";
	}

	/**
	 * Creates regex-match function call using pre-compiled regex
	 * 
	 * @param argExpressions
	 *            input expressions
	 * @param remainingArgTypes
	 *            types of remaining arguments (after input expressions)
	 * @return function call using compiled regex from first argument if constant value; or null if first argument is not constant
	 */
	public FirstOrderFunctionCall<BooleanValue> getCompiledRegexMatchCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
	{
		// check if first arg = regex is constant value, in which case pre-compile the regex
		final RegularExpression compiledRegex;
		if (argExpressions.isEmpty())
		{
			compiledRegex = null;
		}
		else
		{
			final Expression<?> input0 = argExpressions.get(0);
			/*
			 * if first arg is constant, pre-compile the regex
			 */
			final Optional<? extends Value> constant = input0.getValue();
			if (constant.isPresent())
			{
				// actual constant
				final Value constantValue = constant.get();
				if (!(constantValue instanceof StringValue))
				{
					throw new IllegalArgumentException(invalidRegexMsg + constant + "' (invalid datatype: " + input0.getReturnType() + "; expected: " + StandardDatatypes.STRING + ")");
				}

				final String regex = ((StringValue) constantValue).getUnderlyingValue();
				try
				{
					/*
					 * From Saxon xf:matches() implementation: Matches#evaluateItem() / evalMatches()
					 */
					compiledRegex = Version.platform.compileRegularExpression(XmlUtils.SAXON_PROCESSOR.getUnderlyingConfiguration(), regex, "", "XP20", null);
				}
				catch (final XPathException e)
				{
					throw new IllegalArgumentException(invalidRegexMsg + regex + "'", e);
				}
			}
			else
			{
				compiledRegex = null;
			}
		}

		if (compiledRegex == null)
		{
			return null;
		}

		/*
		 * Else compiledRegex != null, so we can optimize: make a new FunctionCall that reuses the compiled regex Although we could remove the first arg from argExpressions since it is already the
		 * compiledRegex, we still need to pass original argExpressions to any subclass of FirstOrderFunctionCall (like below) because it checks all arguments datatypes and so on first.
		 */
		return new CompiledRegexMatchFunctionCall(funcSig, argExpressions, remainingArgTypes, compiledRegex, matchedValueType, indeterminateArg1TypeMessage);
	}
}

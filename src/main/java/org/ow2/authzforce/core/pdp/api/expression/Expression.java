/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api.expression;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * Super interface of any kinds of expression in a policy that the PDP evaluation engine may evaluate in a given authorization request context:
 * <ul>
 * <li>AttributeValue</li>
 * <li>Apply</li>
 * <li>AttributeSelector</li>
 * <li>VariableReference</li>
 * <li>AttributeDesignator</li>
 * <li>Function</li>
 * </ul>
 * 
 * @param <V>
 *            type of result from evaluating the expression
 */
public interface Expression<V extends Value>
{
	/**
	 * Gets the expected return type of the expression if evaluated.
	 * 
	 * @return expression evaluation's return type
	 */
	Datatype<V> getReturnType();

	/**
	 * Evaluates the expression using the given context.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of evaluation that may be a single value T (e.g. function result, AttributeValue, Condition, Match...) or bag of values (e.g. AttributeDesignator, AttributeSelector)
	 * @throws IndeterminateEvaluationException
	 *             if evaluation "Indeterminate" (see XACML core specification)
	 */
	V evaluate(EvaluationContext context) throws IndeterminateEvaluationException;

	/**
	 * Get the constant value of this expression if it has a constant value, i.e. independent from the evaluation context (e.g. AttributeValue, VariableReference to AttributeValue...). This is
	 * equivalent to call {@link #evaluate(EvaluationContext)} with {@code context == null}. This enables expression consumers to do optimizations, e.g. functions may pre-compile/pre-evaluate parts of
	 * their inputs knowing some are constant values.
	 * 
	 * @return the constant value iff the expression has a static/fixed/constant value; else null (if the expression is not constant). NB: Null is not considered/possible as a constant value for
	 *         expressions.
	 */
	V getValue();

	/**
	 * Gets the instance of the Java representation of the XACML-schema-derived JAXB-annotated Expression bound/equivalent to this expression
	 * 
	 * This may be removed in the future as it is no longer considered useful. Do not rely on it. It is the responsibility of the caller that instantiates this Expression to keep a reference to the
	 * original XACML/JAXB class from which it is instantiated, if the caller ever needs it.
	 * 
	 * @return JAXB element equivalent
	 */
	@Deprecated
	JAXBElement<? extends ExpressionType> getJAXBElement();

}

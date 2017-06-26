/**
 * Copyright 2012-2017 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.api.combining;

import java.util.Objects;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;

import org.ow2.authzforce.core.pdp.api.expression.ConstantExpression;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;

/**
 * Evaluates a XACML CombinerParameter.
 */
public final class CombinerParameterEvaluator extends CombinerParameter
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final UnsupportedOperationException UNSUPPORTED_SET_ATTRIBUTE_VALUE_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"CombinerParameterEvaluator.setAttributeValue() not allowed");

	private final AttributeValue attrValue;

	/*
	 * Forced to be non-transient (although derived from other fields) to comply with Serializable contract while staying final
	 */
	private final int hashCode;

	/*
	 * (non-Javadoc)
	 * 
	 * @see oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter# setAttributeValue(oasis.names .tc.xacml._3_0.core.schema.wd_17.AttributeValueType)
	 */
	@Override
	public void setAttributeValue(final AttributeValueType value)
	{
		// Cannot allow this because we have to make sure value is always
		// instance of our internal
		// AttributeValue class
		throw UNSUPPORTED_SET_ATTRIBUTE_VALUE_OPERATION_EXCEPTION;
	}

	@Override
	public AttributeValueType getAttributeValue()
	{
		return attrValue;
	}

	/**
	 * Creates a new CombinerParameter handler.
	 * 
	 * @param param
	 *            CombinerParameter as defined by OASIS XACML model
	 * @param xPathCompiler
	 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default XPath version specified in {@link DefaultsType} element; null if none specified
	 * @param expFactory
	 *            attribute value factory
	 * @throws IllegalArgumentException
	 *             if {@code param} value is not valid
	 */
	public CombinerParameterEvaluator(final CombinerParameter param, final ExpressionFactory expFactory, final XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		// set JAXB AttributeValueType.attributeValue = null, and overridde
		// getAttributeValue() to return an instance of AttributeValue instead
		super(null, param.getParameterName());
		final ConstantExpression<? extends AttributeValue> valExpr = expFactory.getInstance(param.getAttributeValue(), xPathCompiler);
		this.attrValue = valExpr.getValue().get();
		this.hashCode = Objects.hash(this.parameterName, this.attrValue);
	}

	/**
	 * Returns the value provided by this parameter.
	 * 
	 * @return the value provided by this parameter
	 */
	public AttributeValue getValue()
	{
		/*
		 * In the constructor, we make sure input is an AttributeValue, and we override setAttributeValue() to make it unsupported. So this cast should be safe
		 */
		return attrValue;
	}

	@Override
	public int hashCode()
	{
		return this.hashCode;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof CombinerParameterEvaluator))
		{
			return false;
		}

		final CombinerParameterEvaluator other = (CombinerParameterEvaluator) obj;
		return this.parameterName.equals(other.parameterName) && this.attrValue.equals(other.attrValue);
	}

}

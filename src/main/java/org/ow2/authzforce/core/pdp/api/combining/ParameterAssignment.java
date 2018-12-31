/**
 * Copyright 2012-2018 THALES.
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

import org.ow2.authzforce.core.pdp.api.expression.ConstantExpression;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Value;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.CombinerParameter;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;

/**
 * Parameter assignment, may be used to represent an XACML CombinerParameter for example.
 */
public final class ParameterAssignment
{

	private final String paramName;
	private final Value paramValue;

	/*
	 * Forced to be non-transient (although derived from other fields) to comply with Serializable contract while staying final
	 */
	private transient final int hashCode;

	private transient final String toString;

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
	public ParameterAssignment(final CombinerParameter param, final ExpressionFactory expFactory, final XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		// set JAXB AttributeValueType.attributeValue = null, and overridde
		// getAttributeValue() to return an instance of AttributeValue instead
		this.paramName = param.getParameterName();
		final ConstantExpression<? extends AttributeValue> valExpr = expFactory.getInstance(param.getAttributeValue(), xPathCompiler);
		this.paramValue = valExpr.getValue().get();
		this.hashCode = Objects.hash(this.paramName, this.paramValue);
		this.toString = "ParameterAssignment [paramName=" + paramName + ", paramValue=" + paramValue + "]";
	}

	/**
	 * Returns the name of the assigned parameter.
	 * 
	 * @return the name of the assigned parameter.
	 */
	public String getParameterName()
	{
		return this.paramName;
	}

	/**
	 * Returns the value assigned to the parameter.
	 * 
	 * @return the value assigned to the parameter.
	 */
	public Value getValue()
	{
		return paramValue;
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

		if (!(obj instanceof ParameterAssignment))
		{
			return false;
		}

		final ParameterAssignment other = (ParameterAssignment) obj;
		return this.paramName.equals(other.paramName) && this.paramValue.equals(other.paramValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return toString;
	}

}

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
package org.ow2.authzforce.core.pdp.api.func;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * Base class for XACML functions.
 * 
 * @param <RETURN_T>
 *            return type of this function
 */
public abstract class BaseFunction<RETURN_T extends Value> implements Function<RETURN_T>
{
	private final String functionId;
	private final String indeterminateArgMessagePrefix;

	// cached hashcode result
	private transient volatile int hashCode = 0; // Effective Java - Item 9

	@Override
	public final String getId()
	{
		return this.functionId;
	}

	protected BaseFunction(String functionId)
	{
		this.functionId = functionId;
		this.indeterminateArgMessagePrefix = "Function " + functionId + ": Indeterminate arg #";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.eval.Expression#isStatic()
	 */
	@Override
	public final boolean isStatic()
	{
		// the function itself is static: constant identified by its ID
		return true;
	}

	@Override
	public final RETURN_T evaluate(EvaluationContext context) throws IndeterminateEvaluationException
	{
		// Expression#evaluate()
		/*
		 * The static function instance itself (as an expression, without any parameter) evaluates to nothing, it is just a function ID
		 */
		return null;
	}

	@Override
	public final String toString()
	{
		return this.functionId;
	}

	@Override
	public final int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = this.functionId.hashCode();
		}
		return hashCode;
	}

	@Override
	public final boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof BaseFunction))
		{
			return false;
		}

		final BaseFunction<?> other = (BaseFunction<?>) obj;
		// functionId never null
		return functionId.equals(other.functionId);
	}

	/**
	 * Get Indeterminate arg message
	 * 
	 * @param argIndex
	 *            function argument index (#x) that could not be determined
	 * @return "Indeterminate arg#x" exception
	 */
	public final String getIndeterminateArgMessage(int argIndex)
	{
		return indeterminateArgMessagePrefix + argIndex;
	}

	/**
	 * Get Indeterminate arg exception
	 * 
	 * @param argIndex
	 *            function argument index (#x) that could not be determined
	 * @return "Indeterminate arg#x" exception
	 */
	public final IndeterminateEvaluationException getIndeterminateArgException(int argIndex)
	{
		return new IndeterminateEvaluationException(getIndeterminateArgMessage(argIndex), StatusHelper.STATUS_PROCESSING_ERROR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.eval.Expression#createJAXBElement()
	 */
	@Override
	public final JAXBElement<FunctionType> getJAXBElement()
	{
		return JaxbXACMLUtils.XACML_3_0_OBJECT_FACTORY.createFunction(new FunctionType(this.functionId));
	}

}

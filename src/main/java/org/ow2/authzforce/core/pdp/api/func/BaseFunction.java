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

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
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

	protected BaseFunction(final String functionId)
	{
		this.functionId = functionId;
		this.indeterminateArgMessagePrefix = "Function " + functionId + ": Indeterminate arg #";
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
	public final boolean equals(final Object obj)
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
	public final String getIndeterminateArgMessage(final int argIndex)
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
	public final IndeterminateEvaluationException getIndeterminateArgException(final int argIndex)
	{
		return new IndeterminateEvaluationException(getIndeterminateArgMessage(argIndex), StatusHelper.STATUS_PROCESSING_ERROR);
	}

}

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

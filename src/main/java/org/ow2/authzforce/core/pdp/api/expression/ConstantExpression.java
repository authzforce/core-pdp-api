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
package org.ow2.authzforce.core.pdp.api.expression;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * 
 * Expression wrapper for constant values - that do not depend on the evaluation context - to be used as Expressions, e.g. to be used as function argument. This is an alternative to {@link Value}
 * extending {@link Expression} directly, which would break the Acyclic Dependency principle since {@link Expression} already has a reference to {@link Value}.
 * 
 * @param <V>
 *            concrete value type
 *
 */
public abstract class ConstantExpression<V extends Value> implements Expression<V>
{
	private static final IllegalArgumentException UNDEF_DATATYPE_EXCEPTION = new IllegalArgumentException("Undefined expression return type");
	private static final IllegalArgumentException UNDEF_VALUE_EXCEPTION = new IllegalArgumentException("Undefined value");
	private final Datatype<V> datatype;
	protected final V value;

	/**
	 * Creates instance of constant value expression
	 * 
	 * @param datatype
	 *            value datatype
	 * @param v
	 *            constant value
	 * @throws IllegalArgumentException
	 *             if {@code datatype == null || v == null}
	 * 
	 */
	protected ConstantExpression(final Datatype<V> datatype, final V v) throws IllegalArgumentException
	{
		if (datatype == null)
		{
			throw UNDEF_DATATYPE_EXCEPTION;
		}

		if (v == null)
		{
			throw UNDEF_VALUE_EXCEPTION;
		}

		this.datatype = datatype;
		this.value = v;
	}

	@Override
	public final Datatype<V> getReturnType()
	{
		return this.datatype;
	}

	/**
	 * Returns the value itself
	 */
	@Override
	public final V evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
	{
		return this.value;
	}

	@Override
	public final V getValue()
	{
		return this.value;
	}

	@Override
	public String toString()
	{
		return value.toString();
	}

}

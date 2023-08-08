/*
 * Copyright 2012-2023 THALES.
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
package org.ow2.authzforce.core.pdp.api.expression;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

import java.util.Optional;

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
	protected final Optional<V> alwaysPresentValue;

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
		this.alwaysPresentValue = Optional.of(v);
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
	public final V evaluate(final EvaluationContext individualDecisionContext, Optional<EvaluationContext> mdpContext)
	{
		assert alwaysPresentValue.isPresent();
		return this.alwaysPresentValue.get();
	}

	/**
	 * Returns the constant underlying value (always present)
	 */
	@Override
	public final Optional<V> getValue()
	{
		return this.alwaysPresentValue;
	}

	@Override
	public String toString()
	{
		return alwaysPresentValue.toString();
	}

}

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
package org.ow2.authzforce.core.pdp.api.value;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.Optional;

import com.google.common.reflect.TypeToken;

/**
 * Primitive datatype
 *
 * @param <AV>
 *            value type
 */
public final class PrimitiveDatatype<AV extends AtomicValue> extends Datatype<AV>
{

	private static final ClassCastException DEFAULT_CLASS_CAST_EXCEPTION = new ClassCastException("Input is not a primitive value");
	private final Class<AV> valueClass;

	/**
	 * Default constructor
	 * 
	 * @throws NullPointerException
	 *             if {@code valueClass == null || id == null || functionIdPrefix == null}.
	 */
	PrimitiveDatatype(final Class<AV> valueClass, final String id, final String functionIdPrefix) throws NullPointerException
	{
		super(TypeToken.of(Objects.requireNonNull(valueClass, "Undefined valueClass arg")), Optional.empty(), id, functionIdPrefix);
		this.valueClass = valueClass;
	}

	@Override
	public boolean isInstance(final Value val)
	{
		return this.valueClass.isInstance(val);
	}

	@Override
	public AV cast(final Value val) throws ClassCastException
	{
		if (val instanceof AtomicValue)
		{
			return this.valueClass.cast(val);
		}

		throw DEFAULT_CLASS_CAST_EXCEPTION;
	}

	@Override
	public AV[] newArray(final int length)
	{
		return (AV[]) Array.newInstance(this.valueClass, length);
	}

	@Override
	public Optional<Datatype<?>> getTypeParameter()
	{
		return Optional.empty();
	}

}
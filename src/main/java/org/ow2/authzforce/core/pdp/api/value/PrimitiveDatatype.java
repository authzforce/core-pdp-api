/*
 * Copyright 2012-2021 THALES.
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
public class PrimitiveDatatype<AV extends PrimitiveValue> extends Datatype<AV>
{

	private static final ClassCastException DEFAULT_CLASS_CAST_EXCEPTION = new ClassCastException("Input is not a primitive value");
	private final Class<AV> valueClass;
	private final Class<AV[]> arrayClass;

	/**
	 * Datatype constructor
	 * 
	 * @param instanceClass
	 *            (non-null) Java class used as implementation for this datatype, i.e. all values of this datatype are instances of {@code valueClass}.
	 * @param id
	 *            (non-null) datatype ID
	 * @param functionIdPrefix
	 *            (non-null) prefix of ID of any standard generic (e.g. bag/set) function built on this datatype, e.g. 'urn:oasis:names:tc:xacml:1.0:function:string' for string datatype
	 * @throws NullPointerException
	 *             if {@code instanceClass == null || id == null || functionIdPrefix == null}.
	 */
	public PrimitiveDatatype(final Class<AV> instanceClass, final String id, final String functionIdPrefix) throws NullPointerException
	{
		super(TypeToken.of(Objects.requireNonNull(instanceClass, "Undefined valueClass arg")), Optional.empty(), id, functionIdPrefix);
		this.valueClass = instanceClass;
		this.arrayClass = (Class<AV[]>) Array.newInstance(this.valueClass, 0).getClass();
	}

	@Override
	public final boolean isInstance(final Value val)
	{
		return this.valueClass.isInstance(val);
	}

	@Override
	public final AV cast(final Value val) throws ClassCastException
	{
		if (val instanceof PrimitiveValue)
		{
			return this.valueClass.cast(val);
		}

		throw DEFAULT_CLASS_CAST_EXCEPTION;
	}

	@Override
	public final AV[] newArray(final int length)
	{
		return (AV[]) Array.newInstance(this.valueClass, length);
	}

	@Override
	public final Optional<Datatype<?>> getTypeParameter()
	{
		return Optional.empty();
	}

	/**
	 * Get class of instances of this datatype
	 * 
	 * @return class of instances of this datatype
	 */
	public final Class<AV> getInstanceClass()
	{
		return valueClass;
	}

	/**
	 * Get class of array of instances of this datatype
	 * 
	 * @return class of array where the component type is this datatype
	 */
	public final Class<AV[]> getArrayClass()
	{
		return this.arrayClass;
	}

}
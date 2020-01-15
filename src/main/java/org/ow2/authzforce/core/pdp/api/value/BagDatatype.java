/**
 * Copyright 2012-2020 THALES.
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
 * Bag datatype for bags of primitive datatypes
 * 
 * @param <AV>
 *            Java type implementing this datatype (instance type)
 */
public class BagDatatype<AV extends AttributeValue> extends Datatype<Bag<AV>>
{

	private static final ClassCastException DEFAULT_CLASS_CAST_EXCEPTION = new ClassCastException("Input is not a Bag");

	/**
	 * Bag datatype ID prefix, for internal identification purposes. This is an invalid URI on purpose, to avoid conflict with any custom XACML datatype URI (datatype extension).
	 */
	private static final String ID_PREFIX = "bag";

	private final Optional<Datatype<AV>> alwaysPresentElementDatatype;

	/**
	 * Default constructor
	 * 
	 * @throws NullPointerException
	 *             if {@code genericBagType == null || elementDatatype == null}.
	 */
	BagDatatype(final TypeToken<Bag<AV>> genericBagType, final Datatype<AV> elementDatatype) throws NullPointerException
	{
		super(genericBagType, Optional.of(Objects.requireNonNull(elementDatatype)), ID_PREFIX + "<" + elementDatatype + ">", elementDatatype.getFunctionIdPrefix() + "-" + ID_PREFIX);
		this.alwaysPresentElementDatatype = Optional.of(Objects.requireNonNull(elementDatatype, "Undefined typeParam"));
	}

	@Override
	public boolean isInstance(final Value val)
	{
		return val instanceof Bag && alwaysPresentElementDatatype.get().equals(((Bag<?>) val).getElementDatatype());
	}

	@Override
	public Bag<AV> cast(final Value val) throws ClassCastException
	{
		if (val instanceof Bag)
		{
			return (Bag<AV>) val;
		}

		throw DEFAULT_CLASS_CAST_EXCEPTION;
	}

	@Override
	public Optional<? extends Datatype<?>> getTypeParameter()
	{
		return this.alwaysPresentElementDatatype;
	}

	@Override
	public Bag<AV>[] newArray(final int length)
	{
		return (Bag<AV>[]) Array.newInstance(Bag.class, length);
	}

	/**
	 * Returns the bag element datatype (datatype of every element in a bag of this datatype). Same as {@link #getTypeParameter()}.
	 * 
	 * @return bag element datatype
	 */
	public Datatype<AV> getElementType()
	{
		return this.alwaysPresentElementDatatype.get();
	}

}
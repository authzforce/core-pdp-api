/**
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

/**
 * Base class for datatype-specific Attribute Value Factory/Parser.
 * 
 * @param <AV>
 *            type of instance (attribute values) created by this factory
 */
public abstract class BaseAttributeValueFactory<AV extends AttributeValue> implements AttributeValueFactory<AV>
{
	protected final AttributeDatatype<AV> instanceDatatype;
	private final transient String toString;
	private final transient int hashCode;

	/**
	 * Base attribute value factory constructor
	 */
	protected BaseAttributeValueFactory(final AttributeDatatype<AV> instanceDatatype)
	{
		assert instanceDatatype != null;
		this.instanceDatatype = instanceDatatype;
		this.toString = getClass().getName() + "[datatype=" + instanceDatatype + "]";
		this.hashCode = this.instanceDatatype.hashCode();
	}

	/**
	 * Returns the ID of the Datatype of values created by this factory
	 */
	@Override
	public final String getId()
	{
		return this.instanceDatatype.getId();
	}

	@Override
	public final AttributeDatatype<AV> getDatatype()
	{
		return instanceDatatype;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString()
	{
		return toString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode()
	{
		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(final Object other)
	{
		if (this == other)
		{
			return true;
		}

		if (!(other instanceof AttributeValueFactory))
		{
			return false;
		}

		final AttributeValueFactory<?> otherAttValFactory = (AttributeValueFactory<?>) other;
		return this.instanceDatatype.equals(otherAttValFactory.getDatatype());
	}
}
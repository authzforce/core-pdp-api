/**
 * Copyright 2012-2019 THALES.
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
package org.ow2.authzforce.core.pdp.api;

import java.util.Objects;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

/**
 * (XACML-like) AttributeAssignment to be passed to a PEP action (obligation, advice).
 * 
 * @param <AV>
 *            type assigned attribute value
 */
public final class PepActionAttributeAssignment<AV extends AttributeValue>
{

	private final String attId;
	private final Optional<String> category;
	private final Optional<String> issuer;
	private final Datatype<AV> datatype;
	private final AV value;

	private transient volatile int hashCode = 0;
	private transient volatile String toString = null;

	/**
	 * Default constructor
	 * 
	 * @param attributeId
	 *            attribute ID
	 * @param category
	 *            attribute category
	 * @param issuer
	 *            attribute Issuer
	 * @param datatype
	 *            attribute datatype
	 * @param value
	 *            attribute value
	 */
	public PepActionAttributeAssignment(String attributeId, Optional<String> category, Optional<String> issuer, Datatype<AV> datatype, AV value)
	{
		this.attId = attributeId;
		this.datatype = datatype;
		this.value = value;

		this.category = category;
		this.issuer = issuer;
	}

	/**
	 * @return the attribute Id
	 */
	public String getAttributeId()
	{
		return attId;
	}

	/**
	 * @return the category
	 */
	public Optional<String> getCategory()
	{
		return category;
	}

	/**
	 * @return the issuer
	 */
	public Optional<String> getIssuer()
	{
		return issuer;
	}

	/**
	 * @return the attribute datatype
	 */
	public Datatype<AV> getDatatype()
	{
		return datatype;
	}

	/**
	 * @return the attribute value
	 */
	public AV getValue()
	{
		return value;
	}

	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(this.attId, this.category, this.issuer, this.datatype, this.value);
		}

		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof PepActionAttributeAssignment))
		{
			return false;
		}

		final PepActionAttributeAssignment<?> other = (PepActionAttributeAssignment<?>) obj;
		return this.attId.equals(other.attId) && this.category.equals(other.category) && this.issuer.equals(other.issuer) && this.datatype.equals(other.datatype) && this.value.equals(other.value);
	}

	@Override
	public String toString()
	{
		if (toString == null)
		{
			toString = "PepActionAttributeAssignment [attId=" + attId + ", category=" + category.orElse(null) + ", issuer=" + issuer.orElse(null) + ", datatype=" + datatype + ", value=" + value + "]";
		}

		return toString;
	}

}

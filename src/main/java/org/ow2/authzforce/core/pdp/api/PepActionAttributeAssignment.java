/**
 * Copyright 2012-2018 Thales Services SAS.
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

}

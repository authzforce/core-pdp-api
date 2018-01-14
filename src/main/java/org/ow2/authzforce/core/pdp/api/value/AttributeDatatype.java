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
package org.ow2.authzforce.core.pdp.api.value;

import com.google.common.reflect.TypeToken;

/**
 * AttributeValue datatype, i.e. in the XACML context, a datatype of any value that may be used for a request attribute, AttributeValue in a policy, or a response's attribute assignment
 *
 * @param <AV>
 *            value type
 */
public final class AttributeDatatype<AV extends AttributeValue> extends PrimitiveDatatype<AV>
{
	private final Bag<AV> emptyBag;
	private final BagDatatype<AV> bagDatatype;

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
	public AttributeDatatype(final Class<AV> instanceClass, final String id, final String functionIdPrefix) throws NullPointerException
	{
		super(instanceClass, id, functionIdPrefix);
		this.emptyBag = Bags.empty(this, null);
		this.bagDatatype = new BagDatatype<>(new TypeToken<Bag<AV>>()
		{
			private static final long serialVersionUID = 1L;
		}, this);
	}

	/**
	 * Gets empty bag
	 * 
	 * @return empty bag
	 */
	public Bag<AV> getEmptyBag()
	{
		return emptyBag;
	}

	/**
	 * Gets corresponding bag datatype (that a bag of this primitive datatype would have)
	 * 
	 * @return corresponding bag datatype
	 */
	public BagDatatype<AV> getBagDatatype()
	{
		return bagDatatype;
	}

}
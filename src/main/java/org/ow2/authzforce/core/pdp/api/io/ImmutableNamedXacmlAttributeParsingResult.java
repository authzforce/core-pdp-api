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
package org.ow2.authzforce.core.pdp.api.io;

import java.util.Collection;

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

import com.google.common.collect.ImmutableCollection;

/**
 * Immutable implementation of {@link NamedXacmlAttributeParsingResult}
 * 
 * @param <AV>
 *            type of resulting attribute value
 */
public final class ImmutableNamedXacmlAttributeParsingResult<AV extends AttributeValue> implements NamedXacmlAttributeParsingResult<AV>
{

	private final AttributeFqn attName;
	private final Datatype<AV> attDatatype;
	private final ImmutableCollection<AV> attValues;

	/**
	 * Main constructor
	 * 
	 * @param attributeName
	 *            attribute's (fully qualified) name
	 * @param attributeDatatype
	 *            attribute datatype
	 * @param attributeValues
	 *            attribute values
	 */
	public ImmutableNamedXacmlAttributeParsingResult(final AttributeFqn attributeName, final Datatype<AV> attributeDatatype, final ImmutableCollection<AV> attributeValues)
	{
		assert attributeName != null && attributeDatatype != null && attributeValues != null && !attributeValues.isEmpty();
		this.attName = attributeName;
		this.attDatatype = attributeDatatype;
		this.attValues = attributeValues;
	}

	@Override
	public AttributeFqn getAttributeName()
	{
		return this.attName;
	}

	@Override
	public Datatype<AV> getAttributeDatatype()
	{
		return this.attDatatype;
	}

	@Override
	public Collection<AV> getAttributeValues()
	{
		return this.attValues;
	}

}
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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api.value;

import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.AttributeSource;

import com.google.common.collect.ImmutableMultiset;

/**
 * Attribute bag (section 7.3.2 of XACML specification), i.e. {@link Bag} with extra metadata such as the identifier of the attribute value provider (where does the value come from, e.g. Request, or
 * PDP, or custom attribute provider module...)
 * 
 * @param <AV>
 *            type of every element in the bag
 */
public class AttributeBag<AV extends AttributeValue> extends Bag<AV>
{

	private final Optional<AttributeSource> source;

	/**
	 * Constructor
	 * 
	 * @param elementDatatype
	 *            bag element datatype
	 * @param elements
	 *            bag elements.
	 * @param attributeValueSource
	 *            source of the attribute value (where does it come from? Request, PDP, custom attribute provider module...); not present iff {@code elements.isEmpty()} (no value found anywhere)
	 */
	protected AttributeBag(final Datatype<AV> elementDatatype, final ImmutableMultiset<AV> elements, final Optional<AttributeSource> attributeValueSource)
	{
		super(elementDatatype, elements);
		assert elements.isEmpty() || attributeValueSource.isPresent();
		this.source = attributeValueSource;
	}

	/**
	 * Get the source of this attribute bag
	 * 
	 * @return source
	 */
	public final Optional<AttributeSource> getSource()
	{
		return source;
	}

}

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
package org.ow2.authzforce.core.pdp.api.io;

import org.ow2.authzforce.core.pdp.api.AttributeFqn;

/**
 * On the contrary to {@link IssuedToNonIssuedCopyingLaxXacmlAttributeParser}, this XACML Attribute parser does not copy the values of Attributes having an Issuer to the corresponding Attributes
 * without Issuer (same Category, AttributeId...) in the resulting attribute map. Therefore, it does not comply with what XACML 3.0, §5.29 says on &lt;AttributeDesignator&gt; evaluation. However,
 * it is more performant. In this implementation, an Attribute with no Issuer is handled like an attribute with an Issuer, except the Issuer has the special value "null". Therefore, an
 * AttributeDesignator with "null" Issuer (undefined) will still match any attribute in the request with "null" Issuer (but not any other Attribute with same AttributeId but a defined/non-null
 * Issuer, for which a different AttributeDesignator with a defined Issuer must be used).
 * 
 * @param <INPUT_ATTRIBUTE>
 *            type of raw input attribute object (not yet parsed into AuthzForce internal model), typically from original XACML Request, e.g. JAXB-annotated Attribute for XACML/XML request, or
 *            JSON object for XACML/JSON request
 *
 */
public final class NonIssuedLikeIssuedLaxXacmlAttributeParser<INPUT_ATTRIBUTE> extends LaxXacmlAttributeParser<INPUT_ATTRIBUTE>
{
	/**
	 * Creates instance of XACML Attribute Parser
	 * 
	 * @param namedAttributeParser
	 *            low-level parser for named attributes of type {@code INPUT_ATTRIBUTE}
	 * @throws IllegalArgumentException
	 *             iff {@code namedAttributeParser == null}
	 */
	public NonIssuedLikeIssuedLaxXacmlAttributeParser(final NamedXacmlAttributeParser<INPUT_ATTRIBUTE> namedAttributeParser) throws IllegalArgumentException
	{
		super(namedAttributeParser);
	}

	@Override
	protected boolean copyIssuedAttributeValuesToNonIssued(final AttributeFqn attributeFQN)
	{
		return false;
	}

}
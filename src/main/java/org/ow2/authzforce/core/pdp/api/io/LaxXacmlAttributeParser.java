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
package org.ow2.authzforce.core.pdp.api.io;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import net.sf.saxon.s9api.XPathCompiler;

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.AttributeSources;
import org.ow2.authzforce.core.pdp.api.MutableAttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

/**
 * 
 * "Lax" XACML Attribute parser. "Lax" means it allows defining multi-valued attributes by repeating the same XACML Attribute (same AttributeId) within a XACML Attributes element (same Category)
 * but with possibly different AttributeValues. As discussed on the xacml-dev mailing list (see {@linkplain "https://lists.oasis-open.org/archives/xacml-dev/201507/msg00001.html"}), the XACML 3.0
 * core spec, §7.3.3, indicates that multiple occurrences of the same &lt;Attribute&gt; with same meta-data but different values should be considered equivalent to a single &lt;Attribute&gt;
 * element with same meta-data and merged values (multi-valued Attribute). Moreover, the XACML 3.0 conformance test 'IIIA024' expects this behavior: the multiple subject-id Attributes are expected
 * to result in a multi-value bag during evaluation of the &lt;AttributeDesignator&gt;.
 * <p>
 * It is strongly recommended for better performance to avoid such Attribute repetitions and group all the values of the same Attribute in the same Attribute element with multiple AttributeValues,
 * using the "strict" parser ({@link NonIssuedLikeIssuedStrictXacmlAttributeParser} instead of the "lax" variant.
 * 
 * @param <INPUT_ATTRIBUTE>
 *            type of raw input attribute object (not yet parsed into AuthzForce internal model), typically from original XACML Request, e.g. JAXB-annotated Attribute for XACML/XML request, or
 *            JSON object for XACML/JSON request
 *
 */
abstract class LaxXacmlAttributeParser<INPUT_ATTRIBUTE> extends XacmlRequestAttributeParser<INPUT_ATTRIBUTE, MutableAttributeBag<?>>
{

	/**
	 * Creates instance of XACML Attribute Parser
	 * 
	 * @param namedAttributeParser
	 *            low-level parser for named attributes of type {@code INPUT_ATTRIBUTE}
	 * @throws IllegalArgumentException
	 *             iff {@code namedAttributeParser == null}
	 */
	protected LaxXacmlAttributeParser(final NamedXacmlAttributeParser<INPUT_ATTRIBUTE> namedAttributeParser) throws IllegalArgumentException
	{
		super(namedAttributeParser);
	}

	/**
	 * Decide whether to copy values of attributes with Issuer to attributes with same category and ID but null Issuer
	 * 
	 * @return true iff the caller is required to make the copy
	 */
	protected abstract boolean copyIssuedAttributeValuesToNonIssued(AttributeFqn attributeFQN);

	private <AV extends AttributeValue> void updateAttributeMap(final Map<AttributeFqn, MutableAttributeBag<?>> attributeMap, final NamedXacmlAttributeParsingResult<AV> attributeParsingResult)
	{
		assert attributeMap != null && attributeParsingResult != null;
		/*
		 * Input AttributeValues have now been validated. Let's check any existing values for the same attrGUID (<Attribute> with same meta-data) in the map. As discussed on the xacml-dev mailing
		 * list (see https://lists.oasis-open.org/archives/xacml-dev/201507/msg00001.html), the XACML 3.0 core spec, §7.3.3, indicates that multiple occurrences of the same <Attribute> with same
		 * meta-data but different values should be considered equivalent to a single <Attribute> element with same meta-data and merged values (multi-valued Attribute). Moreover, the conformance
		 * test 'IIIA024' expects this behavior: the multiple subject-id Attributes are expected to result in a multi-value bag during evaluation of the AttributeDesignator.
		 * 
		 * Therefore, we choose to merge the attribute values here if this is a new occurrence of the same Attribute, i.e. attrMap.get(attrGUID) != null. In this case, we can reuse the list
		 * already created for the previous occurrence to store the new values resulting from parsing.
		 */
		final AttributeFqn attributeName = attributeParsingResult.getAttributeName();
		final Datatype<AV> attributeDatatype = attributeParsingResult.getAttributeDatatype();
		final MutableAttributeBag<?> previousAttrVals = attributeMap.get(attributeName);
		final MutableAttributeBag<AV> newAttrVals;
		if (previousAttrVals == null)
		{
			/*
			 * First occurrence of this attribute ID (attrGUID). Check whether this is not an unsupported resource-scope attribute.
			 */
			newAttrVals = new MutableAttributeBag<>(attributeDatatype, AttributeSources.REQUEST);
			validateResourceScope(attributeName, newAttrVals);
			attributeMap.put(attributeName, newAttrVals);
		}
		else
		{
			/*
			 * Collection of values already in the map for this Attribute id, reuse/update it directly
			 */
			if (!(previousAttrVals.getElementType().equals(attributeDatatype)))
			{
				throw new IllegalArgumentException("Invalid Attribute '" + attributeName + "': Values with different datatypes (" + previousAttrVals.getElementType() + ", " + attributeDatatype
						+ ")");
			}

			newAttrVals = (MutableAttributeBag<AV>) previousAttrVals;
		}

		/*
		 * XACML 3.0, §5.29 says on <AttributeDesignator>: "If the Issuer is not present in the attribute designator, then the matching of the attribute to the named attribute SHALL be governed by
		 * AttributeId and DataType attributes alone." Therefore, if this attribute has an Issuer, we copy its values to the "Issuer- less" version for evaluating later any matching "Issuer-less"
		 * Attribute Designator.
		 */
		final Collection<AV> attParsingResultValues = attributeParsingResult.getAttributeValues();
		newAttrVals.addAll(attParsingResultValues);

		if (copyIssuedAttributeValuesToNonIssued(attributeName))
		{
			final MutableAttributeBag<AV> newIssuerLessAttrVals;
			// attribute has an Issuer -> prepare to update the matching Issuer-less attribute values
			final AttributeFqn issuerLessId = AttributeFqns.newInstance(attributeName.getCategory(), Optional.empty(), attributeName.getId());
			final MutableAttributeBag<?> oldIssuerLessAttrVals = attributeMap.get(issuerLessId);
			if (oldIssuerLessAttrVals == null)
			{
				newIssuerLessAttrVals = new MutableAttributeBag<>(attributeDatatype, AttributeSources.REQUEST);
				attributeMap.put(issuerLessId, newIssuerLessAttrVals);
			}
			else
			{
				if (!(oldIssuerLessAttrVals.getElementType().equals(attributeDatatype)))
				{
					throw new IllegalArgumentException("Invalid Attribute '" + issuerLessId + "': Values with different datatypes (" + oldIssuerLessAttrVals.getElementType() + ", "
							+ attributeDatatype + ")");
				}

				newIssuerLessAttrVals = (MutableAttributeBag<AV>) oldIssuerLessAttrVals;
			}

			newIssuerLessAttrVals.addAll(attParsingResultValues);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.pdp.api.io.XacmlParsingUtils.XacmlAttributeParser#parseNamedAttribute(java.lang.String, java.lang.Object, net.sf.saxon.s9api.XPathCompiler, java.util.Map)
	 */
	@Override
	public final void parseNamedAttribute(final String attributeCategoryId, final INPUT_ATTRIBUTE inputXacmlAttribute, final XPathCompiler xPathCompiler,
			final Map<AttributeFqn, MutableAttributeBag<?>> attributeMap) throws IllegalArgumentException
	{
		final NamedXacmlAttributeParsingResult<?> attributeParsingResult = parseNamedAttribute(attributeCategoryId, inputXacmlAttribute, xPathCompiler);
		updateAttributeMap(attributeMap, attributeParsingResult);
	}
}
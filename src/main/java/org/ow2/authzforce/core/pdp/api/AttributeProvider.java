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
package org.ow2.authzforce.core.pdp.api;

import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

/**
 * Attribute provider used to resolve {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType}s.
 * 
 */
public interface AttributeProvider
{
	/**
	 * Provides values of the attribute matching the given designator data. If no value found, but no other error occurred, an empty bag is returned.
	 * <p>
	 * WARNING: java.net.URI cannot be used here for XACML datatype/category/id, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI. [1]
	 * http://www.w3.org/TR/xmlschema-2/#anyURI
	 * </p>
	 * 
	 * @param attributeGUID
	 *            the global identifier (Category,Issuer,AttributeId) of the attribute to find
	 * @param context
	 *            the representation of the request data
	 * @param attributeDatatype
	 *            attribute datatype (expected datatype of every element in the result bag} )
	 * @return the result of retrieving the attribute, which will be a bag of values of type defined by {@code attributeDatatype}
	 * @throws IndeterminateEvaluationException
	 *             if any error finding the attribute value(s)
	 */
	<AV extends AttributeValue> Bag<AV> get(AttributeGUID attributeGUID, Datatype<AV> attributeDatatype, EvaluationContext context) throws IndeterminateEvaluationException;

}

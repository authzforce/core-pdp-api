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

import java.util.Optional;

/**
 * Attribute's Fully Qualified Name, including the attribute's Category, the AttributeId, and optional Issuer. So this is more than the AttributeId which is only local to a specific category and/or
 * issuer. This is used for example as key in a map to retrieve corresponding AttributeValue in a request/evaluation context or AttributeProvider module responsible to fetch such attribute.
 * <p>
 * Why not use AttributeDesignator? Because in our internal model, we don't care about MustBePresent or Datatype for looking up an attribute in a request context or other similar lookups.
 * <p>
 * WARNING: java.net.URI cannot be used here for XACML category and ID, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
 * </p>
 * <p>
 * [1] http://www.w3.org/TR/xmlschema-2/#anyURI That's why we use String instead.
 * </p>
 * 
 */
public interface AttributeFqn extends Comparable<AttributeFqn>
{

	/**
	 * @return the category
	 */
	String getCategory();

	/**
	 * @return the id
	 */
	String getId();

	/**
	 * @return the issuer
	 */
	Optional<String> getIssuer();

}
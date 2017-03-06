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
package org.ow2.authzforce.core.pdp.api.combining;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.PdpExtensionRegistry;

/**
 * Provides a registry mechanism for adding and retrieving combining algorithms.
 */
public interface CombiningAlgRegistry extends PdpExtensionRegistry<CombiningAlg<?>>
{

	/**
	 * Tries to return the correct combinging algorithm based on the given algorithm ID.
	 * 
	 * @param algId
	 *            the identifier by which the algorithm is known
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here for XACML category and ID, because not equivalent to XML schema anyURI type. Spaces are allowed in
	 *            XSD anyURI [1], not in java.net.URI for example. That's why we use String instead.
	 *            </p>
	 *            <p>
	 *            [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 *            </p>
	 * @param combinedElementType
	 *            type of combined element
	 * 
	 * @return a combining algorithm
	 * 
	 * @throws IllegalArgumentException
	 *             algId is invalid (not registered in this registry)
	 */
	<T extends Decidable> CombiningAlg<T> getAlgorithm(String algId, Class<T> combinedElementType) throws IllegalArgumentException;

}

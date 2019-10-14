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

/**
 * Marker Interface for all kinds of PDP extension (Attribute datatypes, functions, combining algorithms, AttributeProviderModule, RootPolicyProviderModule...)
 * 
 * 
 */
public interface PdpExtension
{
	/**
	 * Prefix used by AuthZForce PDP extensions. Third-party (outside AuthZForce project) contributions must use a different prefix to avoid conflicts.
	 */
	String AUTHZFORCE_EXTENSION_PREFIX = "urn:ow2:authzforce:feature:pdp:";

	/**
	 * Get globally unique ID (e.g. URI) of the extension
	 * 
	 * @return extension ID
	 */
	String getId();

}

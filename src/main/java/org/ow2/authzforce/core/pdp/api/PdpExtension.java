/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
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

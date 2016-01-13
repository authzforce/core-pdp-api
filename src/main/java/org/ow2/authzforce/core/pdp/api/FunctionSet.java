/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.api;

import java.util.Set;

/**
 * Function set. Allows to group functions of the same category, e.g. all standard numeric comparison functions, all standard numeric arithmetic functions, etc.
 * This is particularly important for simplifying the run-time configuration system, which uses this interface to load a group of functions based only on a
 * function group ID specified in the configuration; therefore, it makes configuration files much smaller, easier to read and maintain.
 */
public interface FunctionSet extends PdpExtension
{
	/**
	 * Namespace to be used as default prefix for internal function set IDs
	 */
	String DEFAULT_ID_NAMESPACE = "urn:thalesgroup:xacml:function-set:";

	/**
	 * Returns a single instance of each of the functions supported by some class. The <code>Set</code> must contain instances of <code>Function</code>, and it
	 * must be both non-null and non-empty. It may contain only a single <code>Function</code>.
	 * 
	 * @return the functions members of this group
	 */
	Set<Function<?>> getSupportedFunctions();

}

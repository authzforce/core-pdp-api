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
 * Enumeration of property names (or so-called global variables) usable in PDP configuration strings in form of ${PROPERTY_NAME}, set by the PDP configuration
 * parser. PDP modules can then replace them during instantiation using {@link EnvironmentProperties#replacePlaceholders(String)} when a EnvironmentProperties
 * object is provided.
 *
 */
public enum EnvironmentPropertyName
{
	/**
	 * PDP configuration file's parent directory if the PDP configuration is loaded from a file. This property can be used in file paths to resolve paths
	 * relative to this parent directory.
	 */
	PARENT_DIR
}
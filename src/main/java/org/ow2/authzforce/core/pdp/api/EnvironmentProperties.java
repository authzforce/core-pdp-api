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
 * Environment properties set by PDP configuration parsers (before or during PDP instantiation) for later use by PDP extensions.
 *
 */
public interface EnvironmentProperties
{

	/**
	 * Replaces placeholders in the form of ${PROPERTY_NAME} with the corresponding property value. Implementations must support the enum constant of
	 * {@link EnvironmentPropertyName} for which {@link EnvironmentPropertyName#name()} is used as PROPERTY_NAME for replacement.
	 * 
	 * @param input
	 *            string possibly containing property placeholders ${...}
	 * @return string with all properties replaced
	 */
	String replacePlaceholders(String input);

}
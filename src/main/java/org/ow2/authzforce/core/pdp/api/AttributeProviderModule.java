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

import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

/**
 * Attribute provider module, in charge of getting attribute values in a specific way (e.g. from a specific attribute source).
 */
public interface AttributeProviderModule extends AttributeProvider
{

	/**
	 * Returns a non-null non-empty <code>Set</code> of <code>AttributeDesignator</code>s provided/supported by this module.
	 * 
	 * @return a non-null non-empty <code>Set</code> of supported <code>AttributeDesignatorType</code>s
	 */
	Set<AttributeDesignatorType> getProvidedAttributes();

}

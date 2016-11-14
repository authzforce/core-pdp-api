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
package org.ow2.authzforce.core.pdp.api.value;

import javax.xml.datatype.DatatypeFactory;

/**
 * The base type for all atomic/non-bag values , this abstract class represents a value for a given datatype. All the standard primitive datatypes defined in the XACML specification extend this, as
 * well as Functions ("special" datatype because they can be used as parameter of higher-order functions). If you want to provide a new datatype, extend {@link DatatypeFactory} to provide a factory
 * for it.
 * 
 */
public interface AtomicValue extends Value
{
	// marker interface
}

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

/**
 * Representation of an xs:dayTimeDuration value. This class supports parsing xs:dayTimeDuration values. All objects of this class are immutable and thread-safe.
 *
 * 
 * @version $Id: $
 */
public final class DayTimeDurationValue extends DurationValue<DayTimeDurationValue>
{
	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#dayTimeDuration";

	/**
	 * Creates instance from string representation
	 *
	 * @param val
	 *            string representation of xs:dayTimeDuration
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code val} is not a valid string representation for xs:dayTimeDuration
	 */
	public DayTimeDurationValue(final String val) throws IllegalArgumentException
	{
		super(TYPE_URI, XML_TEMPORAL_DATATYPE_FACTORY.newDurationDayTime(val));
	}
}

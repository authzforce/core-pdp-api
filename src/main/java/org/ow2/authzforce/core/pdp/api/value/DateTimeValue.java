/**
 * Copyright 2012-2018 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.api.value;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ow2.authzforce.core.pdp.api.XmlUtils;

/**
 * Representation of an xs:dateTime value. This class supports parsing xs:dateTime values. All objects of this class are immutable and thread-safe.
 *
 * 
 * @version $Id: $
 */
public final class DateTimeValue extends BaseTimeValue<DateTimeValue>
{
	/**
	 * Creates a new <code>DateTimeAttributeValue</code> from a string representation of date/time
	 *
	 * @param dateTime
	 *            string representation of date/time
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code dateTime} is not a valid string representation for this value datatype
	 */
	public DateTimeValue(final String dateTime) throws IllegalArgumentException
	{
		this(XmlUtils.XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendar(dateTime));
	}

	/**
	 * Creates a new <code>DateTimeAttributeValue</code> that represents the supplied date
	 *
	 * @param dateTime
	 *            a <code>XMLGregorianCalendar</code> object representing the specified date and time
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code dateTime} does not correspond to a valid xs:dateTime
	 */
	public DateTimeValue(final XMLGregorianCalendar dateTime) throws IllegalArgumentException
	{
		super(dateTime, DatatypeConstants.DATETIME);
	}

	/**
	 * Creates a new <code>DateTimeAttributeValue</code> that represents the supplied date
	 *
	 * @param dateTime
	 *            a <code>GregorianCalendar</code> object representing the specified date and time
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code dateTime} does not correspond to a valid xs:dateTime
	 */
	public DateTimeValue(final GregorianCalendar dateTime)
	{
		this(XmlUtils.XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendar(dateTime));
	}

	/** {@inheritDoc} */
	@Override
	public DateTimeValue add(final DurationValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.getUnderlyingValue());
		return new DateTimeValue(cal);
	}

	/** {@inheritDoc} */
	@Override
	public DateTimeValue subtract(final DurationValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.getUnderlyingValue().negate());
		return new DateTimeValue(cal);
	}
}

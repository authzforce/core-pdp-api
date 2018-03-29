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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ow2.authzforce.core.pdp.api.XmlUtils;

/**
 * Representation of an xs:date value. This class supports parsing xs:date values. All objects of this class are immutable and thread-safe.
 *
 * 
 * @version $Id: $
 */
public final class DateValue extends BaseTimeValue<DateValue>
{
	/**
	 * Creates a new <code>DateAttributeValue</code> from a string representation of date
	 *
	 * @param date
	 *            string representation of date
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code date} is not a valid string representation of xs:date
	 */
	public DateValue(final String date) throws IllegalArgumentException
	{
		this(XmlUtils.XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendar(date));
	}

	/**
	 * Creates instance from Calendar
	 * 
	 * @param date
	 *            date (all time fields assumed unset)
	 * @throws IllegalArgumentException
	 *             if {@code date == null}
	 */
	private DateValue(final XMLGregorianCalendar date) throws IllegalArgumentException
	{
		super(date, DatatypeConstants.DATE);
	}

	/**
	 * Creates a new <code>DateAttributeValue</code> from a Calendar
	 *
	 * @param calendar
	 *            a <code>XMLGregorianCalendar</code> object representing the specified date; beware that this method modifies {@code calendar} by unsetting all time fields:
	 *            {@code calendar.setTime(DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED)}
	 * @return new instance
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code calendar == null}
	 */
	public static DateValue getInstance(final XMLGregorianCalendar calendar) throws IllegalArgumentException
	{
		// we only want the date, so unset time fields
		calendar.setTime(DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED);
		return new DateValue(calendar);
	}

	/** {@inheritDoc} */
	@Override
	public DateValue add(final DurationValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.getUnderlyingValue());
		return new DateValue(cal);
	}

	/** {@inheritDoc} */
	@Override
	public DateValue subtract(final DurationValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.getUnderlyingValue().negate());
		return new DateValue(cal);
	}
}

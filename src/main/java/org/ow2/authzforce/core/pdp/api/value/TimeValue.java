/*
 * Copyright 2012-2023 THALES.
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import org.ow2.authzforce.core.pdp.api.XmlUtils;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Representation of a xs:time value. This class supports parsing xs:time values. All objects of this class are immutable and thread-safe.
 * <p>
 * The {@link XMLGregorianCalendar} returned by {@link #getUnderlyingValue()} have the following characteristics:
 * <ul>
 * <li>{@link XMLGregorianCalendar#getYear()} always returns {@link DatatypeConstants#FIELD_UNDEFINED}</li>
 * <li>{@link XMLGregorianCalendar#getEon()} and {@link XMLGregorianCalendar#getEonAndYear()} always return null</li>
 * <li>{@link XMLGregorianCalendar#getMonth()} always returns {@link DatatypeConstants#FIELD_UNDEFINED}</li>
 * <li>{@link XMLGregorianCalendar#getDay()} always returns {@link DatatypeConstants#FIELD_UNDEFINED}</li>
 * <li>{@link XMLGregorianCalendar#getYear()} always returns {@link DatatypeConstants#FIELD_UNDEFINED}</li>
 * </ul>
 * </p>
 * 
 * @version $Id: $
 */
public final class TimeValue extends BaseTimeValue<TimeValue>
{
	private static final class XdmTimeValue extends XdmAtomicValue {
		private XdmTimeValue(final XMLGregorianCalendar calendar) {
			super(new net.sf.saxon.value.TimeValue(calendar.toGregorianCalendar(), calendar.getTimezone()));
		}
	}

	private transient volatile XdmItem xdmItem = null;

	/**
	 * Creates a new <code>TimeAttributeValue</code> from a string representation of time
	 *
	 * @param time
	 *            string representation of time
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code time} is not a valid string representation of xs:time
	 */
	public TimeValue(final String time) throws IllegalArgumentException
	{
		this(XmlUtils.XML_TEMPORAL_DATATYPE_FACTORY.newXMLGregorianCalendar(time));
	}

	/**
	 * Creates a new <code>TimeAttributeValue</code> that represents the supplied time but uses default timezone and offset values.
	 * 
	 * @param time
	 *            a <code>XMLGregorianCalendar</code> object representing the specified time; all date fields assumed unset
	 * @throws IllegalArgumentException
	 *             if {@code time == null}
	 */
	private TimeValue(final XMLGregorianCalendar time) throws IllegalArgumentException
	{
		super(time, DatatypeConstants.TIME);
	}

	@SuppressFBWarnings(value="EI_EXPOSE_REP", justification="According to Saxon documentation, an XdmValue is immutable.")
	@Override
	public XdmItem getXdmItem()
	{
		if(xdmItem == null) {
			xdmItem = new XdmTimeValue(value);
		}

		return xdmItem;
	}

	/**
	 * Creates a new instance from a Calendar
	 *
	 * @param timeCalendar
	 *            a <code>XMLGregorianCalendar</code> object representing the specified time; beware that this method creates an internal copy of {@code timeCalendar} (to prevent modification of {@code timeCalendar} and any external modification of the created instance's internal copy) before unsetting all date fields (year, month, day): e.g.
	 *            for the year, {@code calendarCopy.setYear(DatatypeConstants.FIELD_UNDEFINED)}
	 * @return new instance
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code calendar == null}
	 */
	public static TimeValue getInstance(final XMLGregorianCalendar timeCalendar)
	{
		// we only want the time, so unset all non-time fields
		// Make a defensive copy first to avoid modifying the original argument and being modified externally
		final XMLGregorianCalendar copy = (XMLGregorianCalendar) timeCalendar.clone();
		copy.setYear(DatatypeConstants.FIELD_UNDEFINED);
		copy.setMonth(DatatypeConstants.FIELD_UNDEFINED);
		copy.setDay(DatatypeConstants.FIELD_UNDEFINED);
		return new TimeValue(copy);
	}

	/** {@inheritDoc} */
	@Override
	public TimeValue add(final DurationValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.getUnderlyingValue());
		return new TimeValue(cal);
	}

	/** {@inheritDoc} */
	@Override
	public TimeValue subtract(final DurationValue<?> durationVal)
	{
		final XMLGregorianCalendar cal = (XMLGregorianCalendar) value.clone();
		cal.add(durationVal.getUnderlyingValue().negate());
		return new TimeValue(cal);
	}

}

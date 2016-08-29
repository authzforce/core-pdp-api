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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/**
 * Superclass of date/time attribute values, i.e. XML schema date/time values. The choice of the Java type
 * <code>XMLGregorianCalendar</code> is based on JAXB schema-to-Java mapping spec:
 * https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 *
 * @param <TAV>
 *            type of result returned by arithmetic functions with this type of arguments: {@link #add(DurationValue)},
 *            {@link #subtract(DurationValue)}, etc. Basically, we expect that arithmetic functions applied to this type
 *            T will return a result of the same type T.
 * 
 * @version $Id: $
 */
/*
 * Do not replace "Time" with "Temporal" in the class name because it is NOT used for Durations (dayTimeDuration,
 * yearMonthDuration...)
 */
public abstract class BaseTimeValue<TAV extends BaseTimeValue<TAV>> extends SimpleValue<XMLGregorianCalendar>
		implements Comparable<TAV>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final XMLGregorianCalendar validate(final XMLGregorianCalendar time, final QName xmlSchemaDatatype)
	{
		if (!time.getXMLSchemaType().equals(xmlSchemaDatatype))
		{
			throw new IllegalArgumentException(
					"Invalid Attribute Value '" + time + "' (actual XML schema type: " + time.getXMLSchemaType()
							+ ")  for specified/expected Attribute DataType '" + xmlSchemaDatatype + '"');
		}

		return time;
	}

	/**
	 * Instantiate date/time attribute value
	 *
	 * @param datatypeId
	 *            datatype URI
	 * @param val
	 *            string representation of instance of this datatype
	 * @param xsdDatatypeQName
	 *            Fully qualified name for the date/time W3C XML Schema 1.0 datatype.
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code datatype == null || val == null}
	 */
	public BaseTimeValue(final String datatypeId, final XMLGregorianCalendar val, final QName xsdDatatypeQName)
			throws IllegalArgumentException
	{
		super(datatypeId, validate(val, xsdDatatypeQName));
	}

	/**
	 * Add duration to this time
	 *
	 * @param durationVal
	 *            duration value
	 * @return this + durationVal
	 */
	abstract public TAV add(DurationValue<?> durationVal);

	/**
	 * Subtract duration to this time
	 *
	 * @param durationVal
	 *            duration value
	 * @return this - durationVal
	 */
	abstract public TAV subtract(DurationValue<?> durationVal);

	/**
	 * {@inheritDoc}
	 *
	 * Compares internal date/time value ({@link XMLGregorianCalendar}) to another, using
	 * {@link XMLGregorianCalendar#compare(XMLGregorianCalendar)}
	 */
	@Override
	public final int compareTo(final TAV o) throws IllegalArgumentException
	{
		final int result = this.value.compare(o.value);
		if (result == DatatypeConstants.INDETERMINATE)
		{
			throw new IllegalArgumentException(
					"Comparison of XML schema date/time '" + this.value + "' to '" + o.value + "' is indeterminate");
		}

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public final String printXML()
	{
		return this.value.toXMLFormat();
	}

}

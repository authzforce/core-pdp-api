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

import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

/**
 * Representation of an xs:hexBinary value. This class supports parsing xs:hexBinary values. All objects of this class are immutable and all methods of the class are thread-safe. The choice of the
 * Java type byte[] is based on JAXB schema-to-Java mapping spec: https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 *
 * 
 * @version $Id: $
 */
public final class HexBinaryValue extends SimpleValue<byte[]>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#hexBinary";

	private transient volatile int hashCode = 0; // Effective Java - Item 9

	/**
	 * Creates a new <code>HexBinaryAttributeValue</code> that represents the byte [] value supplied.
	 *
	 * @param value
	 *            the <code>byte []</code> value to be represented
	 */
	public HexBinaryValue(final byte[] value)
	{
		super(TYPE_URI, value);
	}

	/**
	 * Returns a new <code>HexBinaryAttributeValue</code> that represents the xsi:hexBinary value indicated by the string provided.
	 *
	 * @param val
	 *            a string representing the desired value
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code val} is not a valid string representation of xs:hexBinary
	 */
	public HexBinaryValue(final String val) throws IllegalArgumentException
	{
		this(DatatypeConverter.parseHexBinary(val));
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Arrays.hashCode(value);
		}

		return hashCode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof HexBinaryValue))
		{
			return false;
		}

		final HexBinaryValue other = (HexBinaryValue) obj;

		/*
		 * if (value == null) { if (other.value != null) { return false; } } else
		 */
		return Arrays.equals(value, other.value);
	}

	/** {@inheritDoc} */
	@Override
	public String printXML()
	{
		return DatatypeConverter.printHexBinary(this.value);
	}

}

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

import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

/**
 * Representation of an xs:hexBinary value. This class supports parsing xs:hexBinary values. All objects of this class are immutable and all methods of the class are thread-safe. The choice of the
 * Java type byte[] is based on JAXB schema-to-Java mapping spec: https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 *
 * 
 * @version $Id: $
 */
public final class HexBinaryValue extends StringParseableValue<byte[]>
{
	private transient volatile int hashCode = 0; // Effective Java - Item 9

	/**
	 * Creates a new <code>HexBinaryAttributeValue</code> that represents the byte [] value supplied.
	 *
	 * @param value
	 *            the <code>byte []</code> value to be represented
	 */
	public HexBinaryValue(final byte[] value)
	{
		super(value);
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

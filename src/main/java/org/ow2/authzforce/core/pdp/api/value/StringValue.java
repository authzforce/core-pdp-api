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

import java.util.Locale;

import javax.xml.bind.DatatypeConverter;

/**
 * Representation of an xs:string value. This class supports parsing xs:string values. All objects of this class are immutable and all methods of the class are thread-safe.
 *
 * 
 * @version $Id: $
 */
public final class StringValue extends StringParseableValue<String> implements Comparable<StringValue>
{
	private static final StringValue TRUE = new StringValue("true");
	private static final StringValue FALSE = new StringValue("false");

	/**
	 * Empty StringValue
	 */
	public static final StringValue EMPTY = new StringValue("");

	/**
	 * Convert the lexical XSD string argument into a String value, using {@link javax.xml.bind.DatatypeConverter#parseString(String)}.
	 *
	 * @param val
	 *            A string containing a lexical representation of xsd:string
	 * @return instance
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code value} is not a valid string representation of xsd:string
	 */
	public static StringValue parse(final String val) throws IllegalArgumentException
	{
		return new StringValue(DatatypeConverter.parseString(val));
	}

	/**
	 * Convert string argument - assumed a valid xsd:string into a String value. Use with caution as no xsd:string format validation is done here. For internal purposes only. If you need proper input
	 * validation, use {@link #parse(String)} instead.
	 *
	 * @param validXsdString
	 *            A string containing a valid lexical representation of xsd:string
	 */
	public StringValue(final String validXsdString)
	{
		super(validXsdString);
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(final StringValue o)
	{
		return this.value.compareTo(o.value);
	}

	/**
	 * Same as {@link String#equalsIgnoreCase(String)} on string attribute values
	 *
	 * @param other
	 *            other value to be compared against
	 * @return true if the other attribute value is not null and it represents an equivalent String ignoring case; false otherwise
	 */
	public boolean equalsIgnoreCase(final StringValue other)
	{
		return this.value.equalsIgnoreCase(other.value);
	}

	/**
	 * <p>
	 * trim
	 * </p>
	 *
	 * @see String#trim()
	 * @return StringAttributeValue with value resulting from <code>value.trim()</code>
	 */
	public StringValue trim()
	{
		final String result = value.trim();
		// if the value is same as result, return itself, else return new value from result
		return result.equals(value) ? this : new StringValue(result);
	}

	/**
	 * <p>
	 * toLowerCase
	 * </p>
	 *
	 * @see String#toLowerCase(Locale)
	 * @param locale
	 *            Locale
	 * @return StringAttributeValue with value resulting from <code>value.toLowerCase(L)</code>
	 */
	public StringValue toLowerCase(final Locale locale)
	{
		final String result = value.toLowerCase(locale);
		// if the value is same as result, return itself, else return new value from result
		return result.equals(value) ? this : new StringValue(result);
	}

	// /**
	// * Get string representation of boolean
	// *
	// * @param value
	// * boolean
	// * @return string equivalent ("true" or "false")
	// */
	// public static StringValue getInstance(final Boolean value)
	// {
	// return value.booleanValue() ? TRUE : FALSE;
	// }

	/**
	 * Converts BooleanAttributeValue to String
	 *
	 * @param value
	 *            boolean
	 * @return string equivalent ("true" or "false")
	 */
	public static StringValue getInstance(final BooleanValue value)
	{
		return value == BooleanValue.TRUE ? TRUE : FALSE;
	}

	/** {@inheritDoc} */
	@Override
	public String printXML()
	{
		return this.value;
	}

}

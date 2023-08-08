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

import javax.xml.bind.DatatypeConverter;

/**
 * Representation of a xs:boolean value. This class supports parsing xs:boolean values. All objects of this class are immutable and all methods of the class are thread-safe. The choice of the Java
 * type boolean is based on JAXB schema-to-Java mapping spec: https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 *
 * 
 * @version $Id: $
 */
public final class BooleanValue extends StringParseableValue<Boolean>
{
	private final int hashCode;

	/**
	 * Single instance of BooleanAttributeValue that represents true. Initialized by the static initializer below.
	 */
	public static final BooleanValue TRUE = new BooleanValue(Boolean.TRUE);

	/**
	 * Single instance of BooleanAttributeValue that represents false. Initialized by the static initializer below.
	 */
	public static final BooleanValue FALSE = new BooleanValue(Boolean.FALSE);

	/**
	 * Convert a boolean value from string, according to the XML Schema definition. Adapted from {@link net.sf.saxon.value.BooleanValue#fromString(CharSequence)}, but without whitespace trimming. This
	 * is meant to replace {@link DatatypeConverter#parseBoolean(String)} which is flawed and does not comply with XSD definition of boolean type as of now (JDK7/8). See
	 * https://java.net/jira/browse/JAXB-901, and https://java.net/jira/browse/JAXB-902. E.g. DatatypeConverter.parseBoolean("not") throws NullPointerException instead of IllegalArgumentException as
	 * expected according to javadoc.
	 *
	 * @param s
	 *            XSD-compliant string representation of boolean
	 * @return boolean value corresponding to {@code s}
	 * @throws java.lang.IllegalArgumentException
	 *             if string parameter does not conform to lexical value space defined in XML Schema Part 2: Datatypes for xsd:boolean.
	 */
	public static BooleanValue getInstance(final String s) throws IllegalArgumentException
	{
		/* implementation designed to avoid creating new objects
		 contrary to Saxon's original code, we don't allow whitespaces to apply the XML schema
		 spec strictly
		s = Whitespace.trimWhitespace(s);
		 */
		switch (s.length())
		{
			case 1:
				final char c = s.charAt(0);
				if (c == '1')
				{
					return TRUE;
				}

				if (c == '0')
				{
					return FALSE;
				}
				break;

			case 4:
				if (s.charAt(0) == 't' && s.charAt(1) == 'r' && s.charAt(2) == 'u' && s.charAt(3) == 'e')
				{
					return TRUE;
				}
				break;

			case 5:
				if (s.charAt(0) == 'f' && s.charAt(1) == 'a' && s.charAt(2) == 'l' && s.charAt(3) == 's' && s.charAt(4) == 'e')
				{
					return FALSE;
				}
				break;

			default:
		}

		throw new IllegalArgumentException("The string '" + (s.length() > 5 ? s.substring(0, 5) + "... (content omitted)" : s) + "' is not a valid xs:boolean value.");
	}


	/**
	 * Get {@link #TRUE} (resp. {@link #FALSE} ) instance if <code>b</code> (resp. if ! <code>b</code>)
	 *
	 * @param b
	 *            boolean input
	 * @return instance
	 */
	public static BooleanValue valueOf(final boolean b)
	{
		return b ? TRUE : FALSE;
	}

	private transient volatile XdmItem xdmItem = null;

	/**
	 * Creates a new <code>BooleanAttributeValue</code> that represents the boolean value supplied.
	 * <p>
	 * This constructor is private because it should not be used by anyone other than the static initializer in this class. Instead, please use one of the getInstance methods, which will ensure that
	 * only two BooleanAttributeValue objects are created, thus avoiding excess object creation.
	 * 
	 * @param value
	 *            boolean value
	 */
	public BooleanValue(final Boolean value)
	{
		super(value);
		hashCode = this.value.hashCode();
	}

	/**
	 * not(this)
	 *
	 * @return <code>!value</code>
	 */
	public BooleanValue not()
	{
		return value ? FALSE : TRUE;
	}

	// public static void main(String[] args)
	// {
	// System.out.println(fromString("not"));
	// }

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
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

		if (!(obj instanceof BooleanValue))
		{
			return false;
		}

		final BooleanValue other = (BooleanValue) obj;

		/*
		 * if (value == null) { if (other.value != null) { return false; } } else
		 */
		return value.booleanValue() == other.value.booleanValue();
	}

	/** {@inheritDoc} */
	@Override
	public String printXML()
	{
		return DatatypeConverter.printBoolean(this.value);
	}

	@SuppressFBWarnings(value="EI_EXPOSE_REP", justification="According to Saxon documentation, an XdmValue is immutable.")
	@Override
	public XdmItem getXdmItem()
	{
		if(xdmItem == null) {
			xdmItem = new XdmAtomicValue(value);
		}

		return xdmItem;
	}
}

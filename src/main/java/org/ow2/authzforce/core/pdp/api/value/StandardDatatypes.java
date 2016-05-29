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

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.sf.saxon.s9api.XPathCompiler;

import org.ow2.authzforce.core.pdp.api.func.Function;

/**
 * XACML standard datatypes
 *
 * 
 * @version $Id: $
 */
public final class StandardDatatypes
{
	private StandardDatatypes()
	{
		// private empty constructor
	}

	/**
	 * string
	 */
	public static final SimpleValue.Factory<StringValue> STRING_FACTORY = new SimpleValue.StringContentOnlyFactory<StringValue>(StringValue.class, StringValue.TYPE_URI,
			URI.create(Function.XACML_NS_1_0 + "string"))
	{

		@Override
		public StringValue getInstance(String val)
		{
			return StringValue.parse(val);
		}

	};

	/**
	 * boolean
	 */
	public static final SimpleValue.Factory<BooleanValue> BOOLEAN_FACTORY = new SimpleValue.StringContentOnlyFactory<BooleanValue>(BooleanValue.class, BooleanValue.TYPE_URI,
			URI.create(Function.XACML_NS_1_0 + "boolean"))
	{

		@Override
		public BooleanValue getInstance(String val)
		{
			return BooleanValue.getInstance(val);
		}

	};

	/**
	 * integer
	 */
	public static final SimpleValue.Factory<IntegerValue> INTEGER_FACTORY = new SimpleValue.StringContentOnlyFactory<IntegerValue>(IntegerValue.class, IntegerValue.TYPE_URI,
			URI.create(Function.XACML_NS_1_0 + "integer"))
	{

		@Override
		public IntegerValue getInstance(String val)
		{
			return new IntegerValue(val);
		}

	};

	/**
	 * double
	 */
	public static final SimpleValue.Factory<DoubleValue> DOUBLE_FACTORY = new SimpleValue.StringContentOnlyFactory<DoubleValue>(DoubleValue.class, DoubleValue.TYPE_URI,
			URI.create(Function.XACML_NS_1_0 + "double"))
	{

		@Override
		public DoubleValue getInstance(String val)
		{
			return new DoubleValue(val);
		}

	};

	/**
	 * time
	 */
	public static final SimpleValue.Factory<TimeValue> TIME_FACTORY = new SimpleValue.StringContentOnlyFactory<TimeValue>(TimeValue.class, TimeValue.TYPE_URI, URI.create(Function.XACML_NS_1_0
			+ "time"))
	{

		@Override
		public TimeValue getInstance(String val)
		{
			return new TimeValue(val);
		}

	};

	/**
	 * date
	 */
	public static final SimpleValue.Factory<DateValue> DATE_FACTORY = new SimpleValue.StringContentOnlyFactory<DateValue>(DateValue.class, DateValue.TYPE_URI, URI.create(Function.XACML_NS_1_0
			+ "date"))
	{

		@Override
		public DateValue getInstance(String val)
		{
			return new DateValue(val);
		}

	};

	/**
	 * dateTime
	 */
	public static final SimpleValue.Factory<DateTimeValue> DATETIME_FACTORY = new SimpleValue.StringContentOnlyFactory<DateTimeValue>(DateTimeValue.class, DateTimeValue.TYPE_URI,
			URI.create(Function.XACML_NS_1_0 + "dateTime"))
	{

		@Override
		public DateTimeValue getInstance(String val)
		{
			return new DateTimeValue(val);
		}

	};

	/**
	 * anyURI
	 */
	public static final SimpleValue.Factory<AnyURIValue> ANYURI_FACTORY = new SimpleValue.StringContentOnlyFactory<AnyURIValue>(AnyURIValue.class, AnyURIValue.TYPE_URI,
			URI.create(Function.XACML_NS_1_0 + "anyURI"))
	{

		@Override
		public AnyURIValue getInstance(String val)
		{
			return new AnyURIValue(val);
		}

	};

	/**
	 * hexBinary
	 */
	public static final SimpleValue.Factory<HexBinaryValue> HEXBINARY_FACTORY = new SimpleValue.StringContentOnlyFactory<HexBinaryValue>(HexBinaryValue.class, HexBinaryValue.TYPE_URI,
			URI.create(Function.XACML_NS_1_0 + "hexBinary"))
	{

		@Override
		public HexBinaryValue getInstance(String val)
		{
			return new HexBinaryValue(val);
		}

	};

	/**
	 * base64Binary
	 */
	public static final SimpleValue.Factory<Base64BinaryValue> BASE64BINARY_FACTORY = new SimpleValue.StringContentOnlyFactory<Base64BinaryValue>(Base64BinaryValue.class, Base64BinaryValue.TYPE_URI,
			URI.create(Function.XACML_NS_1_0 + "base64Binary"))
	{
		@Override
		public Base64BinaryValue getInstance(String val)
		{
			return new Base64BinaryValue(val);
		}

	};

	/**
	 * x500Name
	 */
	public static final SimpleValue.Factory<X500NameValue> X500NAME_FACTORY = new SimpleValue.StringContentOnlyFactory<X500NameValue>(X500NameValue.class, X500NameValue.TYPE_URI,
			URI.create(Function.XACML_NS_1_0 + "x500Name"))
	{

		@Override
		public X500NameValue getInstance(String val)
		{
			return new X500NameValue(val);
		}

	};

	/**
	 * rfc822Name
	 */
	public static final SimpleValue.Factory<RFC822NameValue> RFC822NAME_FACTORY = new SimpleValue.StringContentOnlyFactory<RFC822NameValue>(RFC822NameValue.class, RFC822NameValue.TYPE_URI,
			URI.create(Function.XACML_NS_1_0 + "rfc822Name"))
	{

		@Override
		public RFC822NameValue getInstance(String val)
		{
			return new RFC822NameValue(val);
		}
	};

	/**
	 * ipAddress
	 */
	public static final SimpleValue.Factory<IPAddressValue> IPADDRESS_FACTORY = new SimpleValue.StringContentOnlyFactory<IPAddressValue>(IPAddressValue.class, IPAddressValue.TYPE_URI,
			URI.create(Function.XACML_NS_2_0 + "ipAddress"))
	{
		@Override
		public IPAddressValue getInstance(String value)
		{
			return new IPAddressValue(value);
		}

	};

	/**
	 * dnsName
	 */
	public static final SimpleValue.Factory<DNSNameWithPortRangeValue> DNSNAME_FACTORY = new SimpleValue.StringContentOnlyFactory<DNSNameWithPortRangeValue>(DNSNameWithPortRangeValue.class,
			DNSNameWithPortRangeValue.TYPE_URI, URI.create(Function.XACML_NS_2_0 + "dnsName"))
	{

		@Override
		public DNSNameWithPortRangeValue getInstance(String value)
		{
			return new DNSNameWithPortRangeValue(value);
		}

	};

	/**
	 * dayTimeDuration
	 */
	public static final SimpleValue.Factory<DayTimeDurationValue> DAYTIMEDURATION_FACTORY = new SimpleValue.StringContentOnlyFactory<DayTimeDurationValue>(DayTimeDurationValue.class,
			DayTimeDurationValue.TYPE_URI, URI.create(Function.XACML_NS_3_0 + "dayTimeDuration"))
	{

		@Override
		public DayTimeDurationValue getInstance(String val)
		{
			return new DayTimeDurationValue(val);
		}

	};

	/**
	 * yearMonthDuration
	 */
	public static final SimpleValue.Factory<YearMonthDurationValue> YEARMONTHDURATION_FACTORY = new SimpleValue.StringContentOnlyFactory<YearMonthDurationValue>(YearMonthDurationValue.class,
			YearMonthDurationValue.TYPE_URI, URI.create(Function.XACML_NS_3_0 + "yearMonthDuration"))
	{

		@Override
		public YearMonthDurationValue getInstance(String val)
		{
			return new YearMonthDurationValue(val);
		}

	};

	/**
	 * xpathExpression
	 */
	public static final SimpleValue.Factory<XPathValue> XPATH_FACTORY = new SimpleValue.Factory<XPathValue>(XPathValue.class, XPathValue.TYPE_URI, URI.create(Function.XACML_NS_3_0 + "xpath"))
	{
		@Override
		public XPathValue getInstance(String value, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			return new XPathValue(value, otherXmlAttributes, xPathCompiler);
		}

		@Override
		public boolean isExpressionStatic()
		{
			// xpathExpression evaluation result depends on the context (request Content node)
			return false;
		}

	};

	/**
	 * Set of standard mandatory datatype factories (xpathExpression is optional, therefore excluded)
	 */
	public static final Set<SimpleValue.Factory<? extends SimpleValue<? extends Object>>> MANDATORY_DATATYPE_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(STRING_FACTORY,
			BOOLEAN_FACTORY, INTEGER_FACTORY, DOUBLE_FACTORY, TIME_FACTORY, DATE_FACTORY, DATETIME_FACTORY, ANYURI_FACTORY, HEXBINARY_FACTORY, BASE64BINARY_FACTORY, X500NAME_FACTORY,
			RFC822NAME_FACTORY, IPADDRESS_FACTORY, DNSNAME_FACTORY, DAYTIMEDURATION_FACTORY, YEARMONTHDURATION_FACTORY)));

}

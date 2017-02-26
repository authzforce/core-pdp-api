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
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.sf.saxon.s9api.XPathCompiler;

import org.ow2.authzforce.core.pdp.api.HashCollections;
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
	public static final SimpleValue.StringContentOnlyFactory<StringValue> STRING_FACTORY = new SimpleValue.StringContentOnlyFactory<StringValue>(StringValue.class, StringValue.TYPE_URI,
			Function.XACML_NS_1_0 + "string")
	{

		@Override
		public StringValue getInstance(final String val)
		{
			return StringValue.parse(val);
		}

	};

	/**
	 * boolean
	 */
	public static final SimpleValue.StringContentOnlyFactory<BooleanValue> BOOLEAN_FACTORY = new SimpleValue.StringContentOnlyFactory<BooleanValue>(BooleanValue.class, BooleanValue.TYPE_URI,
			Function.XACML_NS_1_0 + "boolean")
	{

		@Override
		public BooleanValue getInstance(final String val)
		{
			return BooleanValue.getInstance(val);
		}

	};

	/**
	 * integer
	 */
	public static final SimpleValue.StringContentOnlyFactory<IntegerValue> INTEGER_FACTORY = new SimpleValue.StringContentOnlyFactory<IntegerValue>(IntegerValue.class, IntegerValue.TYPE_URI,
			Function.XACML_NS_1_0 + "integer")
	{

		@Override
		public IntegerValue getInstance(final String val)
		{
			return new IntegerValue(val);
		}

	};

	/**
	 * double
	 */
	public static final SimpleValue.StringContentOnlyFactory<DoubleValue> DOUBLE_FACTORY = new SimpleValue.StringContentOnlyFactory<DoubleValue>(DoubleValue.class, DoubleValue.TYPE_URI,
			Function.XACML_NS_1_0 + "double")
	{

		@Override
		public DoubleValue getInstance(final String val)
		{
			return new DoubleValue(val);
		}

	};

	/**
	 * time
	 */
	public static final SimpleValue.StringContentOnlyFactory<TimeValue> TIME_FACTORY = new SimpleValue.StringContentOnlyFactory<TimeValue>(TimeValue.class, TimeValue.TYPE_URI, Function.XACML_NS_1_0
			+ "time")
	{

		@Override
		public TimeValue getInstance(final String val)
		{
			return new TimeValue(val);
		}

	};

	/**
	 * date
	 */
	public static final SimpleValue.StringContentOnlyFactory<DateValue> DATE_FACTORY = new SimpleValue.StringContentOnlyFactory<DateValue>(DateValue.class, DateValue.TYPE_URI, Function.XACML_NS_1_0
			+ "date")
	{

		@Override
		public DateValue getInstance(final String val)
		{
			return new DateValue(val);
		}

	};

	/**
	 * dateTime
	 */
	public static final SimpleValue.StringContentOnlyFactory<DateTimeValue> DATETIME_FACTORY = new SimpleValue.StringContentOnlyFactory<DateTimeValue>(DateTimeValue.class, DateTimeValue.TYPE_URI,
			Function.XACML_NS_1_0 + "dateTime")
	{

		@Override
		public DateTimeValue getInstance(final String val)
		{
			return new DateTimeValue(val);
		}

	};

	/**
	 * anyURI
	 */
	public static final SimpleValue.StringContentOnlyFactory<AnyURIValue> ANYURI_FACTORY = new SimpleValue.StringContentOnlyFactory<AnyURIValue>(AnyURIValue.class, AnyURIValue.TYPE_URI,
			Function.XACML_NS_1_0 + "anyURI")
	{

		@Override
		public AnyURIValue getInstance(final String val)
		{
			return new AnyURIValue(val);
		}

	};

	/**
	 * hexBinary
	 */
	public static final SimpleValue.StringContentOnlyFactory<HexBinaryValue> HEXBINARY_FACTORY = new SimpleValue.StringContentOnlyFactory<HexBinaryValue>(HexBinaryValue.class,
			HexBinaryValue.TYPE_URI, Function.XACML_NS_1_0 + "hexBinary")
	{

		@Override
		public HexBinaryValue getInstance(final String val)
		{
			return new HexBinaryValue(val);
		}

	};

	/**
	 * base64Binary
	 */
	public static final SimpleValue.StringContentOnlyFactory<Base64BinaryValue> BASE64BINARY_FACTORY = new SimpleValue.StringContentOnlyFactory<Base64BinaryValue>(Base64BinaryValue.class,
			Base64BinaryValue.TYPE_URI, Function.XACML_NS_1_0 + "base64Binary")
	{
		@Override
		public Base64BinaryValue getInstance(final String val)
		{
			return new Base64BinaryValue(val);
		}

	};

	/**
	 * x500Name
	 */
	public static final SimpleValue.StringContentOnlyFactory<X500NameValue> X500NAME_FACTORY = new SimpleValue.StringContentOnlyFactory<X500NameValue>(X500NameValue.class, X500NameValue.TYPE_URI,
			Function.XACML_NS_1_0 + "x500Name")
	{

		@Override
		public X500NameValue getInstance(final String val)
		{
			return new X500NameValue(val);
		}

	};

	/**
	 * rfc822Name
	 */
	public static final SimpleValue.StringContentOnlyFactory<RFC822NameValue> RFC822NAME_FACTORY = new SimpleValue.StringContentOnlyFactory<RFC822NameValue>(RFC822NameValue.class,
			RFC822NameValue.TYPE_URI, Function.XACML_NS_1_0 + "rfc822Name")
	{

		@Override
		public RFC822NameValue getInstance(final String val)
		{
			return new RFC822NameValue(val);
		}
	};

	/**
	 * ipAddress
	 */
	public static final SimpleValue.StringContentOnlyFactory<IPAddressValue> IPADDRESS_FACTORY = new SimpleValue.StringContentOnlyFactory<IPAddressValue>(IPAddressValue.class,
			IPAddressValue.TYPE_URI, Function.XACML_NS_2_0 + "ipAddress")
	{
		@Override
		public IPAddressValue getInstance(final String value)
		{
			return new IPAddressValue(value);
		}

	};

	/**
	 * dnsName
	 */
	public static final SimpleValue.StringContentOnlyFactory<DNSNameWithPortRangeValue> DNSNAME_FACTORY = new SimpleValue.StringContentOnlyFactory<DNSNameWithPortRangeValue>(
			DNSNameWithPortRangeValue.class, DNSNameWithPortRangeValue.TYPE_URI, Function.XACML_NS_2_0 + "dnsName")
	{

		@Override
		public DNSNameWithPortRangeValue getInstance(final String value)
		{
			return new DNSNameWithPortRangeValue(value);
		}

	};

	/**
	 * dayTimeDuration
	 */
	public static final SimpleValue.StringContentOnlyFactory<DayTimeDurationValue> DAYTIMEDURATION_FACTORY = new SimpleValue.StringContentOnlyFactory<DayTimeDurationValue>(DayTimeDurationValue.class,
			DayTimeDurationValue.TYPE_URI, Function.XACML_NS_3_0 + "dayTimeDuration")
	{

		@Override
		public DayTimeDurationValue getInstance(final String val)
		{
			return new DayTimeDurationValue(val);
		}

	};

	/**
	 * yearMonthDuration
	 */
	public static final SimpleValue.StringContentOnlyFactory<YearMonthDurationValue> YEARMONTHDURATION_FACTORY = new SimpleValue.StringContentOnlyFactory<YearMonthDurationValue>(
			YearMonthDurationValue.class, YearMonthDurationValue.TYPE_URI, Function.XACML_NS_3_0 + "yearMonthDuration")
	{

		@Override
		public YearMonthDurationValue getInstance(final String val)
		{
			return new YearMonthDurationValue(val);
		}

	};

	/**
	 * xpathExpression
	 */
	public static final SimpleValue.Factory<XPathValue> XPATH_FACTORY = new SimpleValue.Factory<XPathValue>(XPathValue.class, XPathValue.TYPE_URI, Function.XACML_NS_3_0 + "xpath")
	{
		@Override
		public XPathValue getInstance(final String value, final Map<QName, String> otherXmlAttributes, final XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			return new XPathValue(value, otherXmlAttributes, xPathCompiler);
		}

	};

	/**
	 * Set of standard mandatory datatype factories (xpathExpression is optional, therefore excluded)
	 */
	public static final Set<SimpleValue.Factory<? extends SimpleValue<? extends Object>>> MANDATORY_DATATYPE_SET = HashCollections.newImmutableSet(Arrays.asList(STRING_FACTORY, BOOLEAN_FACTORY,
			INTEGER_FACTORY, DOUBLE_FACTORY, TIME_FACTORY, DATE_FACTORY, DATETIME_FACTORY, ANYURI_FACTORY, HEXBINARY_FACTORY, BASE64BINARY_FACTORY, X500NAME_FACTORY, RFC822NAME_FACTORY,
			IPADDRESS_FACTORY, DNSNAME_FACTORY, DAYTIMEDURATION_FACTORY, YEARMONTHDURATION_FACTORY));

	/**
	 * Special datatype for function, since datatype are used to specify - among other things - function parameter types, and function may be used as parameters of high-order functions (e.g. any-of,
	 * all-of, etc.). Although this is not defined as XACML datatype per se in XACML specification, this datatype is more or less implicit.
	 */
	@SuppressWarnings("rawtypes")
	public static final Datatype<Function> FUNCTION_DATATYPE = new PrimitiveDatatype<>(Function.class, "function", "function");

}

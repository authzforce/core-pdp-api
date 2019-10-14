/**
 * Copyright 2012-2019 THALES.
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
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.xacml.identifiers.XacmlDatatypeId;

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
	public static final AttributeDatatype<StringValue> STRING = new AttributeDatatype<>(StringValue.class, XacmlDatatypeId.STRING.value(), Function.XACML_NS_1_0 + "string");

	/**
	 * boolean
	 */
	public static final AttributeDatatype<BooleanValue> BOOLEAN = new AttributeDatatype<>(BooleanValue.class, XacmlDatatypeId.BOOLEAN.value(), Function.XACML_NS_1_0 + "boolean");

	/**
	 * integer
	 */
	public static final AttributeDatatype<IntegerValue> INTEGER = new AttributeDatatype<>(IntegerValue.class, XacmlDatatypeId.INTEGER.value(), Function.XACML_NS_1_0 + "integer");

	/**
	 * double
	 */
	public static final AttributeDatatype<DoubleValue> DOUBLE = new AttributeDatatype<>(DoubleValue.class, XacmlDatatypeId.DOUBLE.value(), Function.XACML_NS_1_0 + "double");

	/**
	 * time
	 */
	public static final AttributeDatatype<TimeValue> TIME = new AttributeDatatype<>(TimeValue.class, XacmlDatatypeId.TIME.value(), Function.XACML_NS_1_0 + "time");

	/**
	 * date
	 */
	public static final AttributeDatatype<DateValue> DATE = new AttributeDatatype<>(DateValue.class, XacmlDatatypeId.DATE.value(), Function.XACML_NS_1_0 + "date");

	/**
	 * dateTime
	 */
	public static final AttributeDatatype<DateTimeValue> DATETIME = new AttributeDatatype<>(DateTimeValue.class, XacmlDatatypeId.DATETIME.value(), Function.XACML_NS_1_0 + "dateTime");

	/**
	 * anyURI
	 */
	public static final AttributeDatatype<AnyUriValue> ANYURI = new AttributeDatatype<>(AnyUriValue.class, XacmlDatatypeId.ANY_URI.value(), Function.XACML_NS_1_0 + "anyURI");

	/**
	 * hexBinary
	 */
	public static final AttributeDatatype<HexBinaryValue> HEXBINARY = new AttributeDatatype<>(HexBinaryValue.class, XacmlDatatypeId.HEX_BINARY.value(), Function.XACML_NS_1_0 + "hexBinary");

	/**
	 * base64Binary
	 */
	public static final AttributeDatatype<Base64BinaryValue> BASE64BINARY = new AttributeDatatype<>(Base64BinaryValue.class, XacmlDatatypeId.BASE64_BINARY.value(), Function.XACML_NS_1_0
			+ "base64Binary");

	/**
	 * x500Name
	 */
	public static final AttributeDatatype<X500NameValue> X500NAME = new AttributeDatatype<>(X500NameValue.class, XacmlDatatypeId.X500_NAME.value(), Function.XACML_NS_1_0 + "x500Name");

	/**
	 * rfc822Name
	 */
	public static final AttributeDatatype<Rfc822NameValue> RFC822NAME = new AttributeDatatype<>(Rfc822NameValue.class, XacmlDatatypeId.RFC822_NAME.value(), Function.XACML_NS_1_0 + "rfc822Name");

	/**
	 * ipAddress
	 */
	public static final AttributeDatatype<IpAddressValue> IPADDRESS = new AttributeDatatype<>(IpAddressValue.class, XacmlDatatypeId.IP_ADDRESS.value(), Function.XACML_NS_2_0 + "ipAddress");

	/**
	 * dnsName
	 */
	public static final AttributeDatatype<DnsNameWithPortRangeValue> DNSNAME = new AttributeDatatype<>(DnsNameWithPortRangeValue.class, XacmlDatatypeId.DNS_NAME.value(), Function.XACML_NS_2_0
			+ "dnsName");

	/**
	 * dayTimeDuration
	 */
	public static final AttributeDatatype<DayTimeDurationValue> DAYTIMEDURATION = new AttributeDatatype<>(DayTimeDurationValue.class, XacmlDatatypeId.DAYTIME_DURATION.value(), Function.XACML_NS_3_0
			+ "dayTimeDuration");

	/**
	 * yearMonthDuration
	 */
	public static final AttributeDatatype<YearMonthDurationValue> YEARMONTHDURATION = new AttributeDatatype<>(YearMonthDurationValue.class, XacmlDatatypeId.YEARMONTH_DURATION.value(),
			Function.XACML_NS_3_0 + "yearMonthDuration");

	/**
	 * xpathExpression
	 */
	public static final AttributeDatatype<XPathValue> XPATH = new AttributeDatatype<>(XPathValue.class, XacmlDatatypeId.XPATH_EXPRESSION.value(), Function.XACML_NS_3_0 + "xpath");

	/**
	 * Special datatype for function, since datatype are used to specify - among other things - function parameter types, and function may be used as parameters of high-order functions (e.g. any-of,
	 * all-of, etc.). Although this is not defined as XACML datatype per se in XACML specification, this datatype is more or less implicit.
	 */
	@SuppressWarnings("rawtypes")
	public static final PrimitiveDatatype<Function> FUNCTION = new PrimitiveDatatype<>(Function.class, "function", "function");

	/**
	 * Set of standard mandatory attribute datatypes (xpathExpression is optional, and function is not standard datatype, therefore excluded)
	 */
	public static final Set<AttributeDatatype<?>> MANDATORY_SET = HashCollections.newImmutableSet(Arrays.asList(STRING, BOOLEAN, INTEGER, DOUBLE, TIME, DATE, DATETIME, ANYURI, HEXBINARY,
			BASE64BINARY, X500NAME, RFC822NAME, IPADDRESS, DNSNAME, DAYTIMEDURATION, YEARMONTHDURATION));

}

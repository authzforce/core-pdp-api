/**
 * Copyright 2012-2017 Thales Services SAS.
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

import org.ow2.authzforce.xacml.identifiers.XacmlDatatypeId;

/**
 * Representation of an xs:yearMonthDuration value. This class supports parsing xs:yearMonthDuration values. All objects of this class are immutable and thread-safe. The choice of the Java type
 * Duration is based on JAXB schema-to-Java mapping spec: https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html and documentation of javax.xml.datatype package.
 *
 * 
 * @version $Id: $
 */
public final class YearMonthDurationValue extends DurationValue<YearMonthDurationValue>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates duration attribute value from string representation
	 * 
	 * @param value
	 *            string representation of the XML duration
	 * @throws IllegalArgumentException
	 *             if {@code val} is not a valid string representation for this datatype
	 */

	/**
	 * Instantiates year-month duration from string representation of xs:dayTimeDuration value.
	 *
	 * @param value
	 *            a string representing the desired duration
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code value} is not a valid string representation of xs:dayTimeDuration
	 */
	public YearMonthDurationValue(final String value) throws IllegalArgumentException
	{
		super(XacmlDatatypeId.YEARMONTH_DURATION.value(), XML_TEMPORAL_DATATYPE_FACTORY.newDurationYearMonth(value));
	}

}

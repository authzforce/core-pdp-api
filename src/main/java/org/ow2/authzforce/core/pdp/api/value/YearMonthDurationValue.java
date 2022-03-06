/*
 * Copyright 2012-2022 THALES.
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

import javax.xml.datatype.Duration;

/**
 * Representation of a xs:yearMonthDuration value. This class supports parsing xs:yearMonthDuration values. All objects of this class are immutable and thread-safe. The choice of the Java type
 * Duration is based on JAXB schema-to-Java mapping spec: https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html and documentation of javax.xml.datatype package.
 *
 * 
 * @version $Id: $
 */
public final class YearMonthDurationValue extends DurationValue<YearMonthDurationValue>
{
	private static final class XdmYearMonthDurationValue extends XdmAtomicValue
	{
		private XdmYearMonthDurationValue(final Duration duration) {
			super(net.sf.saxon.value.YearMonthDurationValue.fromMonths(duration.getYears() * 12 + duration.getMonths()), true);
		}
	}

	private transient volatile XdmItem xdmItem = null;

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
		super(XmlUtils.XML_TEMPORAL_DATATYPE_FACTORY.newDurationYearMonth(value));
	}

	@SuppressFBWarnings(value="EI_EXPOSE_REP", justification="According to Saxon documentation, an XdmValue is immutable.")
	@Override
	public XdmItem getXdmItem()
	{
		if(xdmItem == null)
		{
			xdmItem = new XdmYearMonthDurationValue(value);
		}

		return xdmItem;
	}

}

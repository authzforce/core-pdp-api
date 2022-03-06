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
 * Representation of a xs:dayTimeDuration value. This class supports parsing xs:dayTimeDuration values. All objects of this class are immutable and thread-safe.
 *
 * 
 * @version $Id: $
 */
public final class DayTimeDurationValue extends DurationValue<DayTimeDurationValue>
{
	private static final class XdmDayTimeDurationValue extends XdmAtomicValue
	{
		private XdmDayTimeDurationValue(final Duration duration) {
			super(net.sf.saxon.value.DayTimeDurationValue.fromJavaDuration(java.time.Duration.parse(duration.toString())), true);
		}
	}

	private transient volatile XdmItem xdmItem = null;

	/**
	 * Creates instance from string representation
	 *
	 * @param val
	 *            string representation of xs:dayTimeDuration
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code val} is not a valid string representation for xs:dayTimeDuration
	 */
	public DayTimeDurationValue(final String val) throws IllegalArgumentException
	{
		super(XmlUtils.XML_TEMPORAL_DATATYPE_FACTORY.newDurationDayTime(val));
	}

	@SuppressFBWarnings(value="EI_EXPOSE_REP", justification="According to Saxon documentation, an XdmValue is immutable.")
	@Override
	public XdmItem getXdmItem()
	{
		if(xdmItem == null)
		{
			xdmItem = new XdmDayTimeDurationValue(value);
		}

		return xdmItem;
	}
}

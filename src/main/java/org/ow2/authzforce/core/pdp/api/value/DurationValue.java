/**
 * Copyright 2012-2021 THALES.
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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

/**
 * Superclass of duration attribute values, i.e. XML schema dayTime/yearMonthDuration values. The choice of the Java type Duration is based on JAXB schema-to-Java mapping spec:
 * https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html and documentation of javax.xml.datatype package.
 *
 * @param <DAV>
 *            Concrete DurationAttributeValue type subclass
 * 
 * @version $Id: $
 */
public abstract class DurationValue<DAV extends DurationValue<DAV>> extends StringParseableValue<Duration>
{

	/**
	 * Instantiates duration attribute value from string representation
	 *
	 * @param duration
	 *            duration
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code val} is not a valid string representation for this datatype
	 */
	public DurationValue(final Duration duration) throws IllegalArgumentException
	{
		super(duration);
	}

	/**
	 * Compares internal duration value ({@link Duration}) to another, using {@link Duration#compare(Duration)}
	 *
	 * @param o
	 *            compared duration value
	 * @return result of {@link Duration#compare(Duration)}
	 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
	 *             if and only if result is {@link javax.xml.datatype.DatatypeConstants#INDETERMINATE}
	 */
	public final int compare(final DAV o) throws IndeterminateEvaluationException
	{
		final int result = this.value.compare(o.value);
		if (result == DatatypeConstants.INDETERMINATE)
		{
			throw new IndeterminateEvaluationException(XacmlStatusCode.PROCESSING_ERROR.value(), "Comparison of XML schema duration '" + this.value + "' to '" + o.value + "' is indeterminate");
		}

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public final String printXML()
	{
		return this.value.toString();
	}
}

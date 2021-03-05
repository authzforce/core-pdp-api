/*
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

import java.util.Deque;

/**
 * Superclass of all numeric Attribute Values (integer, double...)
 *
 * @param <N>
 *            actual Java type of the underlying numeric value (Integer, Double...)
 * @param <NAV>
 *            Concrete numeric AttributeValue type subclass
 * 
 * @version $Id: $
 */
public abstract class NumericValue<N extends Number, NAV extends NumericValue<N, NAV>> extends StringParseableValue<N>
{
	/**
	 * <p>
	 * Constructor for NumericValue.
	 * </p>
	 *
	 * @param val
	 *            a N object.
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code rawVal == null}
	 * @throws NullPointerException
	 *             if {@code datatypeId == null}
	 */
	protected NumericValue(final N val) throws IllegalArgumentException, NullPointerException
	{
		super(val);
	}

	/**
	 * Returns the absolute value of <code>this</code>. Used by the XACML "abs" functions.
	 *
	 * @return the absolute value
	 */
	public abstract NAV abs();

	/**
	 * Adds numbers to this. Used by the XACML numeric *-add functions.
	 *
	 * @param others
	 *            values to add to this value
	 * @return sum of this and the others.
	 * @throws ArithmeticException
	 *             if the result overflows the value space of {@code N}
	 */
	public abstract NAV add(Deque<? extends NAV> others) throws ArithmeticException;

	/**
	 * Subtract a number from this. Used by XACML numeric *-subtract functions.
	 *
	 * @param subtractedVal
	 *            value to be subtracted from <code>this</code>
	 * @return this - subtractedVal
	 * @throws ArithmeticException
	 *             if the result overflows the value space of {@code N}
	 */
	public abstract NAV subtract(final NAV subtractedVal) throws ArithmeticException;

	/**
	 * Multiply <code>this</code> by other numbers. Used by the XACML "multiply" functions.
	 *
	 * @param others
	 *            other values to multiply by
	 * @return product of this by the others
	 * @throws ArithmeticException
	 *             if the result overflows the value space of {@code N}
	 */
	public abstract NAV multiply(Deque<? extends NAV> others) throws ArithmeticException;

	/**
	 * Divide <code>this</code> by some other number. Used by XACML *-divide functions.
	 *
	 * @param divisor
	 *            number by which <code>this</code> is divided
	 * @return the result quotient
	 * @throws java.lang.ArithmeticException
	 *             if divisor is zero
	 */
	public abstract NAV divide(NAV divisor) throws ArithmeticException;

}

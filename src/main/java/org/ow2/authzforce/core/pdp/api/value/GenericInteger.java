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

import java.math.BigInteger;

/**
 * Generic Integer, to be used as underlying type for {@link IntegerValue} implementations, i.e. as a superclass for small (java short), medium (java int) or large integers (java long or BigInteger)
 *
 */
public abstract class GenericInteger extends Number implements Comparable<GenericInteger>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 * Converts this value to a primitive int safely, i.e. checking for lost information.
	 * 
	 * @see <a href="https://www.securecoding.cert.org/confluence/display/java/NUM00-J.+Detect+or+prevent+integer+overflow">The CERT Oracle Secure Coding Standard for Java - NUM00-J. Detect or prevent
	 *      integer overflow</a>
	 * @return this converted to an int
	 * @throws java.lang.ArithmeticException
	 *             if the value of this will not exactly fit into an int.
	 */
	public abstract int intValueExact() throws ArithmeticException;

	/**
	 *
	 * Converts this value to a primitive long safely, i.e. checking for lost information.
	 * 
	 * @see <a href="https://www.securecoding.cert.org/confluence/display/java/NUM00-J.+Detect+or+prevent+integer+overflow">The CERT Oracle Secure Coding Standard for Java - NUM00-J. Detect or prevent
	 *      integer overflow</a>
	 * @return this converted to an int
	 * @throws java.lang.ArithmeticException
	 *             if the value of this will not exactly fit into a long.
	 */
	public abstract long longValueExact();

	/**
	 * Convert to BigInteger
	 * 
	 * @return this as {@link BigInteger}
	 */
	public abstract BigInteger bigIntegerValue();

	/**
	 * Returns the absolute value of <code>this</code>. Used by the XACML "abs" functions.
	 *
	 * @return the absolute value
	 */
	public abstract GenericInteger abs();

	/**
	 * Add integer to this. Used by the XACML numeric *-add functions.
	 *
	 * @param other
	 *            value to add to this value
	 * @return this + other.
	 * @throws ArithmeticException
	 *             if the result overflows the value space of this type
	 */
	public abstract GenericInteger add(GenericInteger other);

	/**
	 * Subtract a number from this. Used by XACML numeric *-subtract functions.
	 *
	 * @param subtractedValue
	 *            value to be subtracted from <code>this</code>
	 * @return this - subtractedValue
	 * @throws ArithmeticException
	 *             if the result overflows the value space of this type
	 */
	public abstract GenericInteger subtract(GenericInteger subtractedValue);

	/**
	 * Multiply <code>this</code> by another integer. Used by the XACML "multiply" functions.
	 *
	 * @param factor
	 *            factor (other integer to multiply by)
	 * @return this * factor
	 * @throws ArithmeticException
	 *             if the result overflows the value space of this type
	 */
	public abstract GenericInteger multiply(GenericInteger factor);

	/**
	 * Divide <code>this</code> by some other number. Used by XACML *-divide functions.
	 *
	 * @param divisor
	 *            number by which <code>this</code> is divided
	 * @return the result quotient
	 * @throws java.lang.ArithmeticException
	 *             if divisor is zero
	 */
	public abstract GenericInteger divide(GenericInteger divisor);

	/**
	 * Returns this % <code>divisor</code>
	 *
	 * @param divisor
	 *            second argument
	 * @return this % divisor
	 * @throws java.lang.ArithmeticException
	 *             if divisor is zero
	 */
	public abstract GenericInteger remainder(GenericInteger divisor) throws ArithmeticException;
}

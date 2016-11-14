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

import java.math.BigInteger;
import java.util.Deque;

import javax.xml.bind.DatatypeConverter;

/**
 * Representation of an xs:integer value. This class supports parsing xs:integer values. All objects of this class are immutable and all methods of the class are thread-safe. The actual type of the
 * underlying value is BigInteger. See https://jaxb.java.net/tutorial/section_2_2_2-Numeric-Types.html
 *
 * 
 * @version $Id: $
 */
public final class IntegerValue extends NumericValue<BigInteger, IntegerValue> implements Comparable<IntegerValue>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final IllegalArgumentException TOO_BIGINTEGER_FOR_DOUBLE_ILLEGAL_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"BigInteger argument outside the range which can be represented by a double");

	/**
	 * Official name of this type
	 */
	public static final String TYPE_URI = "http://www.w3.org/2001/XMLSchema#integer";

	/**
	 * Value zero
	 */
	public static final IntegerValue ZERO = new IntegerValue(BigInteger.ZERO);

	/**
	 * Creates instance from integer argument
	 *
	 * @param val
	 *            Java equivalent of xsd:integer
	 */
	public IntegerValue(final BigInteger val)
	{
		super(TYPE_URI, val);
	}

	/**
	 * Creates instance from long argument, mostly for easy writing of tests
	 * <p>
	 * Be aware that type long is not equivalent to xsd:integer type, BigInteger is. See https://jaxb.java.net/tutorial/section_2_2_2-Numeric-Types.html
	 * </p>
	 *
	 * @param val
	 *            integer value as Java long
	 */
	public IntegerValue(final long val)
	{
		this(BigInteger.valueOf(val));
	}

	/**
	 * Creates instance from lexical representation of xsd:integer
	 *
	 * @param val
	 *            String representation of xsd:integer
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code val} is not a valid string representation of xs:integer
	 */
	public IntegerValue(final String val) throws IllegalArgumentException
	{
		this(DatatypeConverter.parseInteger(val));
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(final IntegerValue o)
	{
		return this.value.compareTo(o.value);
	}

	/** {@inheritDoc} */
	@Override
	public IntegerValue abs()
	{
		return new IntegerValue(this.value.abs());
	}

	/** {@inheritDoc} */
	@Override
	public IntegerValue add(final Deque<IntegerValue> others)
	{
		BigInteger sum = value;
		while (!others.isEmpty())
		{
			sum = sum.add(others.poll().value);
		}

		return new IntegerValue(sum);
	}

	/** {@inheritDoc} */
	@Override
	public IntegerValue multiply(final Deque<IntegerValue> others)
	{
		BigInteger product = value;
		while (!others.isEmpty())
		{
			product = product.multiply(others.poll().value);
		}

		return new IntegerValue(product);
	}

	/** {@inheritDoc} */
	@Override
	public IntegerValue divide(final IntegerValue divisor) throws ArithmeticException
	{
		return new IntegerValue(value.divide(divisor.value));
	}

	/** {@inheritDoc} */
	@Override
	public IntegerValue subtract(final IntegerValue subtractedVal)
	{
		return new IntegerValue(value.subtract(subtractedVal.value));
	}

	/**
	 * Returns this % <code>divisor</code>
	 *
	 * @param divisor
	 *            second argument
	 * @return this % divisor using {@link BigInteger#remainder(BigInteger)}
	 * @throws java.lang.ArithmeticException
	 *             if divisor is zero
	 */
	public IntegerValue remainder(final IntegerValue divisor) throws ArithmeticException
	{
		return new IntegerValue(value.remainder(divisor.value));
	}

	/**
	 * Converts this integer to a double as specified by {@link BigInteger#doubleValue()}
	 *
	 * @return <code>this</code> as a double
	 * @throws java.lang.IllegalArgumentException
	 *             if this integer is outside the range which can be represented by a double
	 */
	public double doubleValue() throws IllegalArgumentException
	{
		final double doubleVal = value.doubleValue();
		if (Double.isInfinite(doubleVal) || Double.isNaN(doubleVal))
		{
			// this BigInteger has too great a magnitude to represent as a double
			throw TOO_BIGINTEGER_FOR_DOUBLE_ILLEGAL_ARGUMENT_EXCEPTION;
		}

		return doubleVal;
	}

	/**
	 *
	 * Converts this value to an int, checking for lost information. If the value of this BigInteger is out of the range of the int type, then an ArithmeticException is thrown.
	 * 
	 * @see <a href="https://www.securecoding.cert.org/confluence/display/java/NUM00-J.+Detect+or+prevent+integer+overflow">The CERT Oracle Secure Coding Standard for Java - NUM00-J. Detect or prevent
	 *      integer overflow</a>
	 * @return this converted to an int
	 * @throws java.lang.ArithmeticException
	 *             if the value of this will not exactly fit in a int.
	 */
	public int intValueExact() throws ArithmeticException
	{
		return value.intValueExact();
	}

	/** {@inheritDoc} */
	@Override
	public String printXML()
	{
		return DatatypeConverter.printInteger(this.value);
	}

}

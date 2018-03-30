/**
 * Copyright 2012-2018 Thales Services SAS.
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
import java.util.Deque;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of an xs:integer value. This class supports parsing xs:integer values. All objects of this class are immutable and all methods of the class are thread-safe.
 *
 * 
 * @version $Id: $
 */
public final class IntegerValue extends NumericValue<GenericInteger, IntegerValue> implements Comparable<IntegerValue>
{
	private static final IllegalArgumentException TOO_BIGINTEGER_FOR_DOUBLE_ILLEGAL_ARGUMENT_EXCEPTION = new IllegalArgumentException(
	        "BigInteger argument outside the range which can be represented by a double");

	/**
	 * Creates instance from integer argument
	 *
	 * @param val
	 *            Java representation of xsd:integer
	 */
	public IntegerValue(final GenericInteger val)
	{
		super(val);
	}

	private static final IntBasedValueFactory.CachingHelper<IntegerValue> INSTANCE_FACTORY = new IntBasedValueFactory.CachingHelper<>(new IntBasedValueFactory<IntegerValue>()
	{

		@Override
		public Class<IntegerValue> getInstanceClass()
		{
			return IntegerValue.class;
		}

		@Override
		public IntegerValue newInstance(final int i)
		{
			return new IntegerValue(MediumInteger.valueOf(i));
		}

		@Override
		public IntegerValue newInstance(final long l) throws ArithmeticException
		{
			return new IntegerValue(LongInteger.valueOf(l));
		}

	});

	private static final Logger LOGGER = LoggerFactory.getLogger(IntegerValue.class);

	/**
	 * Returns an {@link IntegerValue} instance representing the specified int value
	 * 
	 * @param i
	 *            integer
	 * @return instance representing {@code i}
	 */
	public static IntegerValue valueOf(final int i)
	{
		return INSTANCE_FACTORY.getValue(i);
	}

	/**
	 * Returns an {@link IntegerValue} instance representing the specified long value
	 * 
	 * @param l
	 *            long integer
	 * @return instance representing {@code l}
	 */
	public static IntegerValue valueOf(final long l)
	{
		return INSTANCE_FACTORY.getValue(l);
	}

	private static IntegerValue valueOf(final GenericInteger i)
	{
		try
		{
			final int intVal = i.intValueExact();
			return IntegerValue.valueOf(intVal);
		} catch (final ArithmeticException e)
		{
			LOGGER.debug("Input integer value is too big to fit in an int: {}", i);
		}

		return new IntegerValue(i);
	}

	@Override
	public int compareTo(final IntegerValue o)
	{
		return value.compareTo(o.value);
	}

	@Override
	public IntegerValue abs()
	{
		// TODO: caching
		final GenericInteger result = value.abs();
		return result == value ? this : IntegerValue.valueOf(result);
	}

	@Override
	public IntegerValue add(final Deque<? extends IntegerValue> others) throws ArithmeticException
	{
		GenericInteger result = value;
		while (!others.isEmpty())
		{
			result = result.add(others.poll().value);
		}

		return result == value ? this : IntegerValue.valueOf(result);
	}

	@Override
	public IntegerValue multiply(final Deque<? extends IntegerValue> others) throws ArithmeticException
	{
		GenericInteger result = value;
		while (!others.isEmpty())
		{
			result = result.multiply(others.poll().value);
		}

		return result == value ? this : IntegerValue.valueOf(result);
	}

	@Override
	public IntegerValue divide(final IntegerValue divisor) throws ArithmeticException
	{
		final GenericInteger result = value.divide(divisor.value);
		return result == value ? this : IntegerValue.valueOf(result);
	}

	@Override
	public IntegerValue subtract(final IntegerValue subtractedVal) throws ArithmeticException
	{
		final GenericInteger result = value.subtract(subtractedVal.value);
		return result == value ? this : IntegerValue.valueOf(result);
	}

	@Override
	public String printXML()
	{
		return DatatypeConverter.printInteger(this.value.bigIntegerValue());
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
		final GenericInteger result = this.value.remainder(divisor.value);
		return result == value ? this : IntegerValue.valueOf(result);
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
		// TODO: caching
		final double doubleValue = this.value.doubleValue();
		if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue))
		{
			// this BigInteger has too great a magnitude to represent as a double
			throw TOO_BIGINTEGER_FOR_DOUBLE_ILLEGAL_ARGUMENT_EXCEPTION;
		}

		return doubleValue;
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
		return this.value.intValueExact();
	}
}

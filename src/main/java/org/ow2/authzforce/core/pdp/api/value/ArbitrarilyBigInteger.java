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

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Arbitrary-precision integer, that can represent xsd:integer values. The actual type of the underlying value is {@link BigInteger}. See
 * https://jaxb.java.net/tutorial/section_2_2_2-Numeric-Types.html
 *
 * 
 * @version $Id: $
 */
public final class ArbitrarilyBigInteger extends GenericInteger
{
	private static final long serialVersionUID = 1L;

	private static final IllegalArgumentException TOO_BIGINTEGER_FOR_DOUBLE_ILLEGAL_ARGUMENT_EXCEPTION = new IllegalArgumentException(
	        "BigInteger argument outside the range which can be represented by a double");

	private static final Logger LOGGER = LoggerFactory.getLogger(ArbitrarilyBigInteger.class);

	private final BigInteger value;

	/**
	 * Creates instance from integer argument
	 *
	 * @param value
	 *            Java equivalent of xsd:integer
	 */
	public ArbitrarilyBigInteger(final BigInteger value)
	{
		this.value = value;
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
	public ArbitrarilyBigInteger(final long val)
	{
		this(BigInteger.valueOf(val));
	}

	private static final IntBasedValueFactory.CachingHelper<ArbitrarilyBigInteger> INSTANCE_FACTORY = new IntBasedValueFactory.CachingHelper<>(new IntBasedValueFactory<>()
	{

		@Override
		public Class<ArbitrarilyBigInteger> getInstanceClass()
		{
			return ArbitrarilyBigInteger.class;
		}

		@Override
		public ArbitrarilyBigInteger newInstance(final int i)
		{
			return new ArbitrarilyBigInteger(i);
		}

		@Override
		public ArbitrarilyBigInteger newInstance(final long l) throws ArithmeticException
		{
			return new ArbitrarilyBigInteger(l);
		}

	});

	/**
	 * Returns an {@link ArbitrarilyBigInteger} instance representing the specified big integer value
	 * 
	 * @param big
	 *            integer
	 * @return instance representing {@code big}
	 */
	public static ArbitrarilyBigInteger valueOf(final BigInteger big)
	{
		final long l;
		try
		{
			l = big.longValueExact();
			return INSTANCE_FACTORY.getValue(l);
		} catch (final ArithmeticException e)
		{
			LOGGER.debug("Input integer value is too big to fit in a long: {}", big);
		}

		return new ArbitrarilyBigInteger(big);
	}

	@Override
	public int intValue()
	{
		return value.intValue();
	}

	@Override
	public long longValue()
	{
		return value.longValue();
	}

	@Override
	public float floatValue()
	{
		return value.floatValue();
	}

	@Override
	public BigInteger bigIntegerValue()
	{
		return value;
	}

	@Override
	public int intValueExact() throws ArithmeticException
	{
		return value.intValueExact();
	}

	@Override
	public long longValueExact()
	{
		return value.longValueExact();
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(final GenericInteger o)
	{
		return this.value.compareTo(o.bigIntegerValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return this.value.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof GenericInteger))
		{
			return false;
		}

		final GenericInteger other = (GenericInteger) obj;
		return value.equals(other.bigIntegerValue());
	}

	/** {@inheritDoc} */
	@Override
	public ArbitrarilyBigInteger abs()
	{
		// TODO: caching
		return new ArbitrarilyBigInteger(this.value.abs());
	}

	/** {@inheritDoc} */
	@Override
	public ArbitrarilyBigInteger add(final GenericInteger other)
	{
		final BigInteger result = value.add(other.bigIntegerValue());
		return result.equals(value) ? this : ArbitrarilyBigInteger.valueOf(result);
	}

	/** {@inheritDoc} */
	@Override
	public ArbitrarilyBigInteger multiply(final GenericInteger other) throws ArithmeticException
	{
		final BigInteger result = value.multiply(other.bigIntegerValue());
		return result.equals(value) ? this : ArbitrarilyBigInteger.valueOf(result);
	}

	/** {@inheritDoc} */
	@Override
	public ArbitrarilyBigInteger divide(final GenericInteger divisor) throws ArithmeticException
	{
		final BigInteger result = value.divide(divisor.bigIntegerValue());
		return result.equals(value) ? this : ArbitrarilyBigInteger.valueOf(result);
	}

	/** {@inheritDoc} */
	@Override
	public ArbitrarilyBigInteger subtract(final GenericInteger subtractedVal)
	{
		final BigInteger result = value.subtract(subtractedVal.bigIntegerValue());
		return result.equals(value) ? this : ArbitrarilyBigInteger.valueOf(result);
	}

	@Override
	public ArbitrarilyBigInteger remainder(final GenericInteger divisor) throws ArithmeticException
	{
		final BigInteger result = value.remainder(divisor.bigIntegerValue());
		return result.equals(value) ? this : ArbitrarilyBigInteger.valueOf(result);
	}

	@Override
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

}

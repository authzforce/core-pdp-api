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

/**
 * Long integer, equivalent of xsd:long. The actual Java type of the underlying value is Long. For bigger values, see {@link ArbitrarilyBigInteger}. More info:
 * https://jaxb.java.net/tutorial/section_2_2_2-Numeric-Types.html
 *
 * 
 * @version $Id: $
 */
public final class LongInteger extends GenericInteger
{
	private static final long serialVersionUID = 1L;

	private final long value;

	private transient volatile BigInteger bigInt = null;

	/**
	 * Creates instance from long argument
	 *
	 * @param value
	 *            Java equivalent of xsd:long
	 */
	public LongInteger(final long value)
	{
		this.value = value;
	}

	private static final IntBasedValueFactory.CachingHelper<LongInteger> INSTANCE_FACTORY = new IntBasedValueFactory.CachingHelper<>(new IntBasedValueFactory<LongInteger>()
	{

		@Override
		public Class<LongInteger> getInstanceClass()
		{
			return LongInteger.class;
		}

		@Override
		public LongInteger newInstance(final int i)
		{
			return new LongInteger(i);
		}

		@Override
		public LongInteger newInstance(final long l) throws ArithmeticException
		{
			return new LongInteger(l);
		}

	});

	/**
	 * Returns an {@link LongInteger} instance representing the specified long value
	 * 
	 * @param l
	 *            integer
	 * @return instance representing {@code l}
	 */
	public static LongInteger valueOf(final long l)
	{
		return INSTANCE_FACTORY.getValue(l);
	}

	@Override
	public int intValue()
	{
		return (int) value;
	}

	@Override
	public long longValue()
	{
		return value;
	}

	@Override
	public float floatValue()
	{
		return value;
	}

	@Override
	public double doubleValue() throws IllegalArgumentException
	{
		return value;
	}

	@Override
	public int intValueExact() throws ArithmeticException
	{
		return Math.toIntExact(value);
	}

	@Override
	public long longValueExact()
	{
		return value;
	}

	@Override
	public BigInteger bigIntegerValue()
	{
		if (bigInt == null)
		{
			bigInt = BigInteger.valueOf(value);
		}
		return bigInt;
	}

	@Override
	public int compareTo(final GenericInteger o)
	{
		return Long.compare(value, o.longValueExact());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return Long.hashCode(value);
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
		try
		{
			return value == other.longValueExact();
		} catch (final ArithmeticException e)
		{
			return false;
		}
	}

	@Override
	public LongInteger abs()
	{
		// TODO: caching
		return new LongInteger(Math.abs(this.value));
	}

	@Override
	public LongInteger add(final GenericInteger other) throws ArithmeticException
	{
		final long result = value + other.longValueExact();
		return result == value ? this : LongInteger.valueOf(result);
	}

	@Override
	public LongInteger multiply(final GenericInteger other) throws ArithmeticException
	{
		final long result = value * other.longValueExact();
		return result == value ? this : LongInteger.valueOf(result);
	}

	@Override
	public LongInteger divide(final GenericInteger divisor) throws ArithmeticException
	{
		final long result = value / divisor.longValueExact();
		return result == value ? this : LongInteger.valueOf(result);
	}

	@Override
	public LongInteger subtract(final GenericInteger subtractedVal)
	{
		final long result = Math.subtractExact(value, subtractedVal.longValueExact());
		return result == value ? this : LongInteger.valueOf(result);
	}

	@Override
	public LongInteger remainder(final GenericInteger divisor) throws ArithmeticException
	{
		final long result = value % divisor.longValueExact();
		return result == value ? this : LongInteger.valueOf(result);
	}

}

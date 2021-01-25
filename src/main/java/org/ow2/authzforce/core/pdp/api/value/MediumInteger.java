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

import java.math.BigInteger;

/**
 * Medium-size integer, equivalent of xsd:int. The actual Java type of the underlying value is Integer. For bigger values, see {@link LongInteger}.
 *
 * 
 * @version $Id: $
 */
public final class MediumInteger extends GenericInteger
{
	private static final long serialVersionUID = 1L;

	private final int value;

	private transient volatile BigInteger bigInt = null;

	/**
	 * Creates instance from integer argument
	 *
	 * @param value
	 *            Java equivalent of xsd:int
	 */
	public MediumInteger(final int value)
	{
		this.value = value;
	}

	private static final IntBasedValueFactory.CachingHelper<MediumInteger> INSTANCE_FACTORY = new IntBasedValueFactory.CachingHelper<>(new IntBasedValueFactory<>()
	{

		@Override
		public Class<MediumInteger> getInstanceClass()
		{
			return MediumInteger.class;
		}

		@Override
		public MediumInteger newInstance(final int i)
		{
			return new MediumInteger(i);
		}

		@Override
		public MediumInteger newInstance(final long l) throws ArithmeticException
		{
			return new MediumInteger(Math.toIntExact(l));
		}

	});

	/**
	 * Returns an {@link MediumInteger} instance representing the specified int value
	 * 
	 * @param i
	 *            integer
	 * @return instance representing {@code i}
	 */
	public static MediumInteger valueOf(final int i)
	{
		return INSTANCE_FACTORY.getValue(i);
	}

	@Override
	public int intValue()
	{
		return value;
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
		return value;
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
		return Integer.compare(value, o.intValueExact());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return Integer.hashCode(value);
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
			return value == other.intValueExact();
		} catch (final ArithmeticException e)
		{
			return false;
		}
	}

	@Override
	public MediumInteger abs()
	{
		// TODO: caching
		return new MediumInteger(Math.abs(this.value));
	}

	@Override
	public MediumInteger add(final GenericInteger other) throws ArithmeticException
	{
		final int result = value + other.intValueExact();
		return result == value ? this : MediumInteger.valueOf(result);
	}

	@Override
	public MediumInteger multiply(final GenericInteger other) throws ArithmeticException
	{
		final int result = value * other.intValueExact();
		return result == value ? this : MediumInteger.valueOf(result);
	}

	@Override
	public MediumInteger divide(final GenericInteger divisor) throws ArithmeticException
	{
		final int result = value / divisor.intValueExact();
		return result == value ? this : MediumInteger.valueOf(result);
	}

	@Override
	public MediumInteger subtract(final GenericInteger subtractedVal)
	{
		final int result = Math.subtractExact(value, subtractedVal.intValueExact());
		return result == value ? this : MediumInteger.valueOf(result);
	}

	@Override
	public MediumInteger remainder(final GenericInteger divisor) throws ArithmeticException
	{
		final int result = value % divisor.intValueExact();
		return result == value ? this : MediumInteger.valueOf(result);
	}

}

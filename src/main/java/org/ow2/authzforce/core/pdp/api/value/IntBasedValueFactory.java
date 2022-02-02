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

import java.lang.reflect.Array;

/**
 * Integer-based/derived value factory
 *
 */
interface IntBasedValueFactory<V>
{
	/**
	 * Name of system property setting the lower bound (int) of cached integer-based values, e.g. {@link IntegerValue}s. Default: {@link Byte#MIN_VALUE}
	 */
	String INTEGER_VALUE_CACHE_MIN_SYS_PROPERTY_NAME = "org.ow2.authzforce.core.pdp.api.value.IntBasedValueFactory.cacheMin";

	/**
	 * Name of system property setting the upper bound (int) of cached integer-based values, e.g. {@link IntegerValue}s. Default: {@link Byte#MAX_VALUE}
	 */
	String INTEGER_VALUE_CACHE_MAX_SYS_PROPERTY_NAME = "org.ow2.authzforce.core.pdp.api.value.IntBasedValueFactory.cacheMax";

	Class<V> getInstanceClass();

	V newInstance(int i);

	V newInstance(long l) throws ArithmeticException;

	final class CachingHelper<V>
	{
		private static final int CACHE_MIN;
		private static final int CACHE_MAX;
		static
		{
			final String cacheMinSysPropVal = System.getProperty(INTEGER_VALUE_CACHE_MIN_SYS_PROPERTY_NAME);
			if (cacheMinSysPropVal == null)
			{
				CACHE_MIN = Byte.MIN_VALUE;
			}
			else
			{
				try
				{
					CACHE_MIN = Integer.parseInt(cacheMinSysPropVal);
				}
				catch (final NumberFormatException e)
				{
					throw new RuntimeException("Invalid value of system property '" + INTEGER_VALUE_CACHE_MIN_SYS_PROPERTY_NAME + "': " + cacheMinSysPropVal + ". Expected: int (Java)");
				}
			}

			final String cacheMaxSysPropVal = System.getProperty(INTEGER_VALUE_CACHE_MAX_SYS_PROPERTY_NAME);
			if (cacheMaxSysPropVal == null)
			{
				CACHE_MAX = Byte.MAX_VALUE;
			}
			else
			{
				try
				{
					CACHE_MAX = Integer.parseInt(cacheMaxSysPropVal);
				}
				catch (final NumberFormatException e)
				{
					throw new RuntimeException("Invalid value of system property '" + INTEGER_VALUE_CACHE_MAX_SYS_PROPERTY_NAME + "': " + cacheMaxSysPropVal + ". Expected: int (Java)");
				}

				if(CACHE_MAX <= CACHE_MIN) {
					throw new RuntimeException("Invalid value of system property '" + INTEGER_VALUE_CACHE_MAX_SYS_PROPERTY_NAME + "' (too small): " + CACHE_MAX + " <= " + CACHE_MIN + "(may be changed by system property '"+ INTEGER_VALUE_CACHE_MAX_SYS_PROPERTY_NAME + "')" );
				}
			}
		}

		private final IntBasedValueFactory<V> baseFactory;
		private final V[] cache;

		CachingHelper(final IntBasedValueFactory<V> baseFactory)
		{
			this.baseFactory = baseFactory;
			this.cache = (V[]) Array.newInstance(baseFactory.getInstanceClass(), CACHE_MAX - CACHE_MIN + 1);
			int cachedInt = CACHE_MIN;
			for (int i = 0; i < cache.length; i++)
			{
				cache[i] = baseFactory.newInstance(cachedInt);
				cachedInt += 1;
			}
		}

		V getValue(final int i)
		{
			if (i >= CACHE_MIN && i <= CACHE_MAX)
			{
				return cache[i - CACHE_MIN];
			}

			return baseFactory.newInstance(i);
		}

		V getValue(final long l)
		{
			if (l >= CACHE_MIN && l <= CACHE_MAX)
			{
				return cache[(int) l - CACHE_MIN];
			}

			return baseFactory.newInstance(l);
		}
	}
}

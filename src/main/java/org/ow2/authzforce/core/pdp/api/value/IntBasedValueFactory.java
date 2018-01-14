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
	String INTEGER_VALUE_CACHE_MIN = "org.ow2.authzforce.core.pdp.api.value.IntBasedValueFactory.cacheMin";

	/**
	 * Name of system property setting the upper bound (int) of cached integer-based values, e.g. {@link IntegerValue}s. Default: {@link Byte#MAX_VALUE}
	 */
	String INTEGER_VALUE_CACHE_MAX = "org.ow2.authzforce.core.pdp.api.value.IntBasedValueFactory.cacheMax";

	Class<V> getInstanceClass();

	V newInstance(int i);

	V newInstance(long l) throws ArithmeticException;

	final class CachingHelper<V>
	{
		private static int DEFAULT_CACHE_MIN;
		private static int DEFAULT_CACHE_MAX;
		static
		{
			final String cacheMinSysPropVal = System.getProperty(INTEGER_VALUE_CACHE_MIN);
			if (cacheMinSysPropVal == null)
			{
				DEFAULT_CACHE_MIN = Byte.MIN_VALUE;
			}
			else
			{
				try
				{
					DEFAULT_CACHE_MIN = Integer.parseInt(cacheMinSysPropVal);
				}
				catch (final NumberFormatException e)
				{
					throw new RuntimeException("Invalid value of system property '" + INTEGER_VALUE_CACHE_MIN + "': " + cacheMinSysPropVal + ". Expected: int (Java)");
				}
			}

			final String cacheMaxSysPropVal = System.getProperty(INTEGER_VALUE_CACHE_MAX);
			if (cacheMaxSysPropVal == null)
			{
				DEFAULT_CACHE_MAX = Byte.MAX_VALUE;
			}
			else
			{
				try
				{
					DEFAULT_CACHE_MAX = Integer.parseInt(cacheMaxSysPropVal);
				}
				catch (final NumberFormatException e)
				{
					throw new RuntimeException("Invalid value of system property '" + INTEGER_VALUE_CACHE_MAX + "': " + cacheMaxSysPropVal + ". Expected: int (Java)");
				}
			}
		}

		private final IntBasedValueFactory<V> baseFactory;
		private final int cacheMin;
		private final int cacheMax;
		private final V[] cache;

		private CachingHelper(final IntBasedValueFactory<V> baseFactory, final int cacheMin, final int cacheMax)
		{
			assert cacheMax > cacheMin;
			this.cacheMin = cacheMin;
			this.cacheMax = cacheMax;
			this.baseFactory = baseFactory;
			cache = (V[]) Array.newInstance(baseFactory.getInstanceClass(), cacheMax - cacheMin + 1);
			int cachedInt = cacheMin;
			for (int i = 0; i < cache.length; i++)
			{
				cache[i] = baseFactory.newInstance(cachedInt);
				cachedInt += 1;
			}
		}

		CachingHelper(final IntBasedValueFactory<V> baseFactory)
		{

			this(baseFactory, DEFAULT_CACHE_MIN, DEFAULT_CACHE_MAX);
		}

		V getValue(final int i)
		{
			if (i >= cacheMin && i <= cacheMax)
			{
				return cache[i - cacheMin];
			}

			return baseFactory.newInstance(i);
		}

		V getValue(final long l)
		{
			if (l >= cacheMin && l <= cacheMax)
			{
				return cache[(int) l - cacheMin];
			}

			return baseFactory.newInstance(l);
		}
	}
}

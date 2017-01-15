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
package org.ow2.authzforce.core.pdp.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Utilities for String validation, sanitizing, transformation, etc.
 * 
 */
public final class StringUtils
{
	private StringUtils()
	{
	}

	/**
	 * Replace CRLF characters with "&lt;NEWLINE&gt"; in {@code input.toString()} to prevent CRLF injection
	 * 
	 * @param input
	 *            input object
	 * @return encoded string
	 */
	public static String sanitizeForLogging(final Object input)
	{
		try
		{
			return URLEncoder.encode(input.toString(), "UTF-8");
		}
		catch (final UnsupportedEncodingException e)
		{
			// UTF-8 is supported
			return null;
		}
	}
}

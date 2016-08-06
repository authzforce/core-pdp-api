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

import java.io.Serializable;
import java.util.Objects;

/**
 * This class represents a port range as specified in the <code>dnsName</code> and <code>ipAddress</code> datatypes. The range may have upper and lower bounds, be specified by a single port number, or
 * may be unbound.
 * 
 * @version $Id: $
 */
public final class NetworkPortRange implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Minimum network socket port number (decimal).
	 */
	public static final int MIN_NET_PORT_NUMBER = 0;

	/**
	 * Maximum network socket port number (decimal).
	 */
	public static final int MAX_NET_PORT_NUMBER = 65535;

	// the port range bounds
	private final int lowerBound;
	private final int upperBound;

	private transient volatile String toString = null;
	private transient volatile int hashCode = 0; // Effective Java - Item 9

	/**
	 * Creates a <code>PortRange</code> with upper and lower bounds.
	 * 
	 * @param lowerBound
	 *            the lower-bound port number
	 * @param upperBound
	 *            the upper-bound port number
	 */
	private NetworkPortRange(final int lowerBound, final int upperBound)
	{
		if (lowerBound < MIN_NET_PORT_NUMBER)
		{
			throw new IllegalArgumentException("Invalid port number: " + lowerBound + " < " + MIN_NET_PORT_NUMBER);
		}

		// MIN <= lowerBound

		if (lowerBound > upperBound)
		{
			throw new IllegalArgumentException("Invalid port range: lower bound (" + lowerBound + ") > upper bound (" + upperBound + ")");
		}

		// MIN <= lowerbound <= upperBound

		if (upperBound > MAX_NET_PORT_NUMBER)
		{
			throw new IllegalArgumentException("Invalid port number: " + upperBound + " > " + MAX_NET_PORT_NUMBER);
		}

		// MIN <= lowerbound <= upperBound <= MAX

		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	/**
	 * Max port range (all possible port numbers): [{@value #MIN_NET_PORT_NUMBER}, {@value #MAX_NET_PORT_NUMBER}]
	 */
	public static final NetworkPortRange MAX = new NetworkPortRange(MIN_NET_PORT_NUMBER, MAX_NET_PORT_NUMBER);

	/**
	 * Creates an instance of <code>PortRange</code> based on the given value.
	 *
	 * @param value
	 *            a <code>String</code> representing the range, that may be null or empty for unlimited range, or a valid port range as defined by appendix A.2 for IP address and DNS name.
	 * @return a new <code>PortRange</code>
	 * @throws IllegalArgumentException
	 *             if the port range syntax is not: {@literal portnumber | "-"portnumber | portnumber"-"[portnumber]}; or one of the {@literal portnumber} values is not a valid decimal number in the
	 *             interval [{@value #MIN_NET_PORT_NUMBER}, {@value #MAX_NET_PORT_NUMBER}]
	 */
	public static NetworkPortRange getInstance(final String value) throws IllegalArgumentException
	{
		// first off, make sure there's actually a port range
		if (value == null || value.isEmpty())
		{
			return MAX;
		}

		// there's content, so figure where the '-' is, if at all
		final int dashPos = value.indexOf('-');
		final int lowerBound;
		final int upperBound;

		if (dashPos == -1)
		{
			// there's no dash, so it's just a single number
			lowerBound = upperBound = Integer.parseInt(value);
		} else if (dashPos == 0)
		{
			// it starts with a dash, so it's just upper-range bound
			lowerBound = MIN_NET_PORT_NUMBER;
			upperBound = Integer.parseInt(value.substring(1));
		} else
		{
			// it's a number followed by a dash, so get the lower-bound...
			lowerBound = Integer.parseInt(value.substring(0, dashPos));
			final int len = value.length();
			// ... and see if there is an upper-bound specified
			upperBound = dashPos == len - 1 ? MAX_NET_PORT_NUMBER : Integer.parseInt(value.substring(dashPos + 1, len));
		}

		return new NetworkPortRange(lowerBound, upperBound);
	}

	/**
	 * Returns the lower-bound port value. If the range is not lower-bound, then this returns <code>UNBOUND</code>. If the range is actually a single port number, then this returns the same value as
	 * <code>getUpperBound</code>.
	 *
	 * @return the upper-bound
	 */
	public int getLowerBound()
	{
		return lowerBound;
	}

	/**
	 * Returns the upper-bound port value. If the range is not upper-bound, then this returns <code>UNBOUND</code>. If the range is actually a single port number, then this returns the same value as
	 * <code>getLowerBound</code>.
	 *
	 * @return the upper-bound
	 */
	public int getUpperBound()
	{
		return upperBound;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(lowerBound, upperBound);
		}

		return hashCode;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Returns true if the input is an instance of this class and if its value equals the value contained in this class.
	 */
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}

		if (!(o instanceof NetworkPortRange))
		{
			return false;
		}

		final NetworkPortRange other = (NetworkPortRange) o;
		return lowerBound == other.lowerBound && upperBound == other.upperBound;
	}

	@Override
	public String toString()
	{
		if (toString != null)
		{
			return toString;
		}

		if (lowerBound == MIN_NET_PORT_NUMBER)
		{
			toString = upperBound == MAX_NET_PORT_NUMBER ? "" : "-" + upperBound;
		} else
		{
			toString = lowerBound + "-" + (upperBound == MAX_NET_PORT_NUMBER ? "" : upperBound);
		}

		return toString;
	}

}

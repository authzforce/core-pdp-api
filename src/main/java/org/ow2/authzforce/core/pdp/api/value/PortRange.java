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

import java.util.Objects;

/**
 * This class represents a port range as specified in the <code>dnsName</code> and <code>ipAddress</code> datatypes. The range may have upper and lower bounds, be specified by a single port number, or
 * may be unbound.
 * 
 * @version $Id: $
 */
public final class PortRange
{

	/**
	 * Constant used to specify that the range is unbound on one side.
	 */
	private static final int UNBOUND = -1;

	// the port bound values
	private final int lowerBound;
	private final int upperBound;

	/**
	 * Default constructor used to represent an unbound range. This is typically used when an address has no port information.
	 */
	public PortRange()
	{
		this(UNBOUND, UNBOUND);
	}

	/**
	 * Creates a <code>PortRange</code> with upper and lower bounds. Either of the parameters may have the value <code>UNBOUND</code> meaning that there is no bound at the respective end.
	 * 
	 * @param lowerBound
	 *            the lower-bound port number or <code>UNBOUND</code>
	 * @param upperBound
	 *            the upper-bound port number or <code>UNBOUND</code>
	 */
	private PortRange(int lowerBound, int upperBound)
	{
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	/**
	 * Creates an instance of <code>PortRange</code> based on the given value.
	 *
	 * @param value
	 *            a <code>String</code> representing the range
	 * @return a new <code>PortRange</code>
	 * @throws java.lang.NumberFormatException
	 *             if a port value isn't an integer
	 */
	public static PortRange getInstance(String value)
	{
		int lowerBound = UNBOUND;
		int upperBound = UNBOUND;

		// first off, make sure there's actually content here
		if (value.length() == 0 || value.equals("-"))
		{
			return new PortRange();
		}

		// there's content, so figure where the '-' is, if at all
		int dashPos = value.indexOf('-');

		if (dashPos == -1)
		{
			// there's no dash, so it's just a single number
			lowerBound = upperBound = Integer.parseInt(value);
		} else if (dashPos == 0)
		{
			// it starts with a dash, so it's just upper-range bound
			upperBound = Integer.parseInt(value.substring(1));
		} else
		{
			// it's a number followed by a dash, so get the lower-bound...
			lowerBound = Integer.parseInt(value.substring(0, dashPos));
			int len = value.length();

			// ... and see if there is a second port number
			if (dashPos != len - 1)
			{
				// the dash wasn't at the end, so there's an upper-bound
				upperBound = Integer.parseInt(value.substring(dashPos + 1, len));
			}
		}

		return new PortRange(lowerBound, upperBound);
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

	/**
	 * Returns whether the range is bounded by a lower port number.
	 *
	 * @return true if lower-bounded, false otherwise
	 */
	public boolean isLowerBounded()
	{
		return lowerBound != UNBOUND;
	}

	/**
	 * Returns whether the range is bounded by an upper port number.
	 *
	 * @return true if upper-bounded, false otherwise
	 */
	public boolean isUpperBounded()
	{
		return upperBound != UNBOUND;
	}

	/**
	 * Returns whether the range is actually a single port number.
	 *
	 * @return true if the range is a single port number, false otherwise
	 */
	public boolean isSinglePort()
	{
		return lowerBound == upperBound && lowerBound != UNBOUND;
	}

	/**
	 * Returns whether the range is unbound, which means that it specifies no port number or range. This is typically used with addresses that include no port information.
	 *
	 * @return true if the range is unbound, false otherwise
	 */
	public boolean isUnbound()
	{
		return lowerBound == UNBOUND && upperBound == UNBOUND;
	}

	private transient volatile int hashCode = 0; // Effective Java - Item 9

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
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}

		if (!(o instanceof PortRange))
		{
			return false;
		}

		final PortRange other = (PortRange) o;
		return lowerBound == other.lowerBound && upperBound == other.upperBound;
	}

	/**
	 * <p>
	 * encode
	 * </p>
	 *
	 * @return encoded port range
	 */
	public String encode()
	{
		if (isUnbound())
			return "";

		if (isSinglePort())
			return Integer.toString(lowerBound, 10);

		if (!isLowerBounded())
			return "-" + Integer.toString(upperBound, 10);

		if (!isUpperBounded())
			return Integer.toString(lowerBound, 10) + "-";

		return Integer.toString(lowerBound, 10) + "-" + Integer.toString(upperBound, 10);
	}

}

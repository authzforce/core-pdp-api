/**
 *
 * Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistribution of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in the design, construction, operation or maintenance of any nuclear facility.
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

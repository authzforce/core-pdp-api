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
package org.ow2.authzforce.core.pdp.api.policy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Version patterns used in policy references to match specific policy
 * version(s). This class also provides a simple set of comparison methods for
 * matching against the patterns.
 * 
 */
public class VersionPatterns
{

	// the three constraints
	private final PolicyVersionPattern versionPattern;
	private final PolicyVersionPattern earliestVersionPattern;
	private final PolicyVersionPattern latestVersionPattern;

	private static class PolicyVersionPattern
	{
		private static final int WILDCARD = -1;
		private static final int PLUS = -2;

		private final String xacmlVersionMatch;
		private final List<Integer> matchNumbers;
		private final PolicyVersion asLiteral;

		private PolicyVersionPattern(String xacmlVersionMatch) throws IllegalArgumentException
		{
			assert xacmlVersionMatch != null;
			if (xacmlVersionMatch.isEmpty() || xacmlVersionMatch.startsWith(".") || xacmlVersionMatch.endsWith("."))
			{
				throw new IllegalArgumentException("Invalid VersionMatch expression: '" + xacmlVersionMatch + "'");
			}

			final String[] tokens = xacmlVersionMatch.split("\\.");
			matchNumbers = new ArrayList<>(tokens.length);
			boolean patternCharFound = false;
			for (int i = 0; i < tokens.length; i++)
			{
				final String token = tokens[i];
				switch (token)
				{
					case "*":
						matchNumbers.add(WILDCARD);
						patternCharFound = true;
						break;
					case "+":
						patternCharFound = true;
						matchNumbers.add(PLUS);
						break;
					default:
						final int number;
						try
						{
							number = Integer.parseInt(tokens[i], 10);
						} catch (NumberFormatException e)
						{
							throw new IllegalArgumentException("Invalid VersionMatch expression: '" + xacmlVersionMatch + "'", e);
						}

						if (number < 0)
						{
							throw new IllegalArgumentException("Invalid VersionMatch expression: '" + xacmlVersionMatch + "'. Number #" + i + " (=" + number + ") is not a positive integer");
						}

						matchNumbers.add(number);
						break;
				}
			}

			this.xacmlVersionMatch = xacmlVersionMatch;
			this.asLiteral = patternCharFound ? null : new PolicyVersion(xacmlVersionMatch, matchNumbers);
		}

		@Override
		public String toString()
		{
			return xacmlVersionMatch;
		}

		private boolean matches(PolicyVersion version)
		{
			final Iterator<Integer> versionNumsIterator = version.getNumberSequence().iterator();
			final Iterator<Integer> matchNumsIterator = this.matchNumbers.iterator();
			while (matchNumsIterator.hasNext() && versionNumsIterator.hasNext())
			{
				final int matchNum = matchNumsIterator.next();
				final int versionNum = versionNumsIterator.next();
				switch (matchNum)
				{
					case PLUS:
						// always matches everything from here
						return true;
					case WILDCARD:
						// always matches any versionNumbers[i], so go on
						break;
					default:
						if (matchNum != versionNum)
						{
							return false;
						}

						// else same number, so go on
						break;
				}
			}

			/*
			 * At this point, last matchNum is either a wildcard or integer.
			 * Version matches iff there is no extra number in either
			 * matchNumbers or versionNumbers.
			 */
			return !matchNumsIterator.hasNext() && !versionNumsIterator.hasNext();
		}

		public boolean isLaterOrMatches(PolicyVersion version)
		{
			final Iterator<Integer> versionNumsIterator = version.getNumberSequence().iterator();
			final Iterator<Integer> matchNumsIterator = this.matchNumbers.iterator();
			while (matchNumsIterator.hasNext() && versionNumsIterator.hasNext())
			{
				final int matchNum = matchNumsIterator.next();
				final int versionNum = versionNumsIterator.next();
				switch (matchNum)
				{
					case PLUS:
						// always matches everything from here
						return true;
					case WILDCARD:
						/*
						 * Always matches any versionNumbers[i], and we could
						 * always find an acceptable version V > version
						 * argument that matches this pattern (matchNumbers) by
						 * taking a single number greater than versionNumbers[i]
						 * at the same index in V. So versionNumbers is earlier
						 * than the latest acceptable.
						 */
						return true;
					default:
						if (matchNum < versionNum)
						{
							return false;
						}

						if (matchNum > versionNum)
						{
							return true;
						}

						// else same number, so go on
						break;
				}
			}

			/*
			 * At this point, we know matchNumbers is a sequence of numbers (no
			 * wildcard/plus symbol). It is later than or matches versionNumbers
			 * iff there is no extra number in versionNums.
			 */
			return !versionNumsIterator.hasNext();
		}

		public boolean isEarlierOrMatches(PolicyVersion version)
		{
			final Iterator<Integer> versionNumsIterator = version.getNumberSequence().iterator();
			final Iterator<Integer> matchNumsIterator = this.matchNumbers.iterator();
			while (matchNumsIterator.hasNext() && versionNumsIterator.hasNext())
			{
				final int matchNum = matchNumsIterator.next();
				final int versionNum = versionNumsIterator.next();
				switch (matchNum)
				{
					case PLUS:
						// always matches everything from here
						return true;
					case WILDCARD:
						if (versionNum != 0)
						{
							/*
							 * We can find an earlier matching version (with any
							 * number < versionNum here).
							 */
							return true;
						}

						// versionNum = 0. Result depends on the next numbers.
						break;
					default:
						if (matchNum < versionNum)
						{
							return true;
						}

						if (matchNum > versionNum)
						{
							return false;
						}

						// else same number, so go on
						break;
				}
			}

			/*
			 * If there is no extra numbers in matchNumbers.length, it is
			 * earlier or matches versionNums
			 */
			return !matchNumsIterator.hasNext();
		}

	}

	/**
	 * Creates a <code>VersionConstraints</code> with the three optional
	 * constraint strings. Each of the three strings must conform to the
	 * VersionMatchType type defined in the XACML schema. Any of the strings may
	 * be null to specify that the given constraint is not used.
	 * 
	 * @param versionMatch
	 *            matching expression for the version; or null if none
	 * @param earliestMatch
	 *            matching expression for the earliest acceptable version; or
	 *            null if none
	 * @param latestMatch
	 *            matching expression for the earliest acceptable version; or
	 *            null if none
	 * @throws IllegalArgumentException
	 *             if one of the match expressions is invalid
	 */
	public VersionPatterns(String versionMatch, String earliestMatch, String latestMatch) throws IllegalArgumentException
	{
		this.versionPattern = versionMatch == null ? null : new PolicyVersionPattern(versionMatch);
		this.earliestVersionPattern = earliestMatch == null ? null : new PolicyVersionPattern(earliestMatch);
		this.latestVersionPattern = latestMatch == null ? null : new PolicyVersionPattern(latestMatch);
		if (this.versionPattern != null && this.versionPattern.asLiteral != null)
		{
			if (this.earliestVersionPattern != null && !this.earliestVersionPattern.matches(this.versionPattern.asLiteral))
			{
				throw new IllegalArgumentException("Version (literal) '" + versionPattern + "' and EarliestVersion '" + earliestVersionPattern + "' cannot be both matched by the same version.");
			}

			if (this.latestVersionPattern != null && !this.latestVersionPattern.matches(this.versionPattern.asLiteral))
			{
				throw new IllegalArgumentException("Version (literal) '" + versionPattern + "' and LatestVersion '" + latestVersionPattern + "' cannot be both matched by the same version.");
			}
		}

		if (this.earliestVersionPattern != null && this.earliestVersionPattern.asLiteral != null && this.latestVersionPattern != null && this.latestVersionPattern.asLiteral != null && earliestVersionPattern.asLiteral.compareTo(latestVersionPattern.asLiteral) > 0)
		{
			throw new IllegalArgumentException("EarliestVersion (literal) '" + earliestVersionPattern + "' > LatestVersion (literal) '" + latestVersionPattern + "'!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return String.format("Version=%s,EarliestVersion=%s,LatestVersion=%s", (versionPattern == null) ? "*" : versionPattern, (earliestVersionPattern == null) ? "*" : earliestVersionPattern, (latestVersionPattern == null) ? "*" : latestVersionPattern);
	}

	/**
	 * Check version against LatestVersion pattern
	 * 
	 * @param version
	 *            input version to be checked
	 * @return true iff LatestVersion matched
	 */
	public boolean matchLatestVersion(PolicyVersion version)
	{
		return latestVersionPattern == null || latestVersionPattern.isLaterOrMatches(version);
	}

	/**
	 * Check version against EarliestVersion pattern
	 * 
	 * @param version
	 *            input version to be checked
	 * @return true iff EarliestVersion matched
	 */
	public boolean matchEarliestVersion(PolicyVersion version)
	{
		return earliestVersionPattern == null || earliestVersionPattern.isEarlierOrMatches(version);
	}

	/**
	 * Check version against Version pattern
	 * 
	 * @param version
	 *            input version to be checked
	 * @return true iff Version matched
	 */
	public boolean matchVersion(PolicyVersion version)
	{
		return versionPattern == null || versionPattern.matches(version);
	}

	/**
	 * Get Version pattern:
	 * 
	 * @return Version to be matched
	 */
	public String getVersionPattern()
	{
		return this.versionPattern.toString();
	}

	/**
	 * Get EarliestVersion pattern: matching expression for the earliest
	 * acceptable version
	 * 
	 * @return EarliestVersion to be matched
	 */
	public String getEarliestVersionPattern()
	{
		return this.earliestVersionPattern.toString();
	}

	/**
	 * Get LatestVersion pattern: matching expression for the latest acceptable
	 * version
	 * 
	 * @return LatestVersion to be matched
	 */
	public String getLatestVersionPattern()
	{
		return this.latestVersionPattern.toString();
	}

	// public static void main(String... args)
	// {
	// PolicyVersionPattern vp = new PolicyVersionPattern("0.+");
	// PolicyVersion v = new PolicyVersion("1.2.4.5");
	// System.out.println(vp.isLaterOrMatches(v));
	// }
}

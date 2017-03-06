/**
 * Copyright 2012-2017 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.api.policy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Version patterns used in policy references to match specific policy version(s). This class also provides a simple set
 * of comparison methods for matching against the patterns.
 * 
 */
public class VersionPatterns
{

	// the three constraints
	private final PolicyVersionPattern versionPattern;
	private final PolicyVersionPattern earliestVersionPattern;
	private final PolicyVersionPattern latestVersionPattern;

	private static final class PolicyVersionPattern
	{
		private static final int WILDCARD_INT = -1;
		private static final int PLUS_INT = -2;
		private static final Integer WILDCARD_INTEGER = Integer.valueOf(WILDCARD_INT);
		private static final Integer PLUS_INTEGER = Integer.valueOf(PLUS_INT);

		private final String xacmlVersionMatch;
		private final List<Integer> matchNumbers;
		private final PolicyVersion asLiteral;

		private PolicyVersionPattern(final String xacmlVersionMatch) throws IllegalArgumentException
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
				switch (token) {
					case "*":
						matchNumbers.add(WILDCARD_INTEGER);
						patternCharFound = true;
						break;
					case "+":
						patternCharFound = true;
						matchNumbers.add(PLUS_INTEGER);
						break;
					default:
						final Integer number;
						try
						{
							number = Integer.valueOf(tokens[i], 10);
						}
						catch (final NumberFormatException e)
						{
							throw new IllegalArgumentException(
									"Invalid VersionMatch expression: '" + xacmlVersionMatch + "'", e);
						}

						if (number.intValue() < 0)
						{
							throw new IllegalArgumentException("Invalid VersionMatch expression: '" + xacmlVersionMatch
									+ "'. Number #" + i + " (=" + number + ") is not a positive integer");
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

		private boolean matches(final PolicyVersion version)
		{
			final Iterator<Integer> versionNumsIterator = version.getNumberSequence().iterator();
			final Iterator<Integer> matchNumsIterator = this.matchNumbers.iterator();
			while (matchNumsIterator.hasNext() && versionNumsIterator.hasNext())
			{
				final Integer matchNum = matchNumsIterator.next();
				final Integer versionNum = versionNumsIterator.next();
				switch (matchNum.intValue()) {
					case PLUS_INT:
						// always matches everything from here
						return true;
					case WILDCARD_INT:
						// always matches any versionNumbers[i], so go on
						break;
					default:
						if (matchNum.intValue() != versionNum.intValue())
						{
							return false;
						}

						// else same number, so go on
						break;
				}
			}

			/*
			 * At this point, last matchNum is either a wildcard or integer. Version matches iff there is no extra
			 * number in either matchNumbers or versionNumbers.
			 */
			return !matchNumsIterator.hasNext() && !versionNumsIterator.hasNext();
		}

		public boolean isLaterOrMatches(final PolicyVersion version)
		{
			final Iterator<Integer> versionNumsIterator = version.getNumberSequence().iterator();
			final Iterator<Integer> matchNumsIterator = this.matchNumbers.iterator();
			while (matchNumsIterator.hasNext() && versionNumsIterator.hasNext())
			{
				final Integer matchNum = matchNumsIterator.next();
				final Integer versionNum = versionNumsIterator.next();
				switch (matchNum.intValue()) {
					case PLUS_INT:
						// always matches everything from here
						return true;
					case WILDCARD_INT:
						/*
						 * Always matches any versionNumbers[i], and we could always find an acceptable version V >
						 * version argument that matches this pattern (matchNumbers) by taking a single number greater
						 * than versionNumbers[i] at the same index in V. So versionNumbers is earlier than the latest
						 * acceptable.
						 */
						return true;
					default:
						final int compareToResult = matchNum.compareTo(versionNum);
						if (compareToResult < 0)
						{
							return false;
						}

						if (compareToResult > 0)
						{
							return true;
						}

						// else same number, so go on
						break;
				}
			}

			/*
			 * At this point, we know matchNumbers is a sequence of numbers (no wildcard/plus symbol). It is later than
			 * or matches versionNumbers iff there is no extra number in versionNums.
			 */
			return !versionNumsIterator.hasNext();
		}

		public boolean isEarlierOrMatches(final PolicyVersion version)
		{
			final Iterator<Integer> versionNumsIterator = version.getNumberSequence().iterator();
			final Iterator<Integer> matchNumsIterator = this.matchNumbers.iterator();
			while (matchNumsIterator.hasNext() && versionNumsIterator.hasNext())
			{
				final Integer matchNum = matchNumsIterator.next();
				final Integer versionNum = versionNumsIterator.next();
				switch (matchNum.intValue()) {
					case PLUS_INT:
						// always matches everything from here
						return true;
					case WILDCARD_INT:
						if (versionNum.intValue() != 0)
						{
							/*
							 * We can find an earlier matching version (with any number < versionNum here).
							 */
							return true;
						}

						// versionNum = 0. Result depends on the next numbers.
						break;
					default:
						final int compareToResult = matchNum.compareTo(versionNum);
						if (compareToResult < 0)
						{
							return true;
						}

						if (compareToResult > 0)
						{
							return false;
						}

						// else same number, so go on
						break;
				}
			}

			/*
			 * If there is no extra numbers in matchNumbers.length, it is earlier or matches versionNums
			 */
			return !matchNumsIterator.hasNext();
		}

	}

	/**
	 * Creates a <code>VersionConstraints</code> with the three optional constraint strings. Each of the three strings
	 * must conform to the VersionMatchType type defined in the XACML schema. Any of the strings may be null to specify
	 * that the given constraint is not used.
	 * 
	 * @param versionMatch
	 *            matching expression for the version; or null if none
	 * @param earliestMatch
	 *            matching expression for the earliest acceptable version; or null if none
	 * @param latestMatch
	 *            matching expression for the earliest acceptable version; or null if none
	 * @throws IllegalArgumentException
	 *             if one of the match expressions is invalid
	 */
	public VersionPatterns(final String versionMatch, final String earliestMatch, final String latestMatch)
			throws IllegalArgumentException
	{
		this.versionPattern = versionMatch == null ? null : new PolicyVersionPattern(versionMatch);
		this.earliestVersionPattern = earliestMatch == null ? null : new PolicyVersionPattern(earliestMatch);
		this.latestVersionPattern = latestMatch == null ? null : new PolicyVersionPattern(latestMatch);
		if (this.versionPattern != null && this.versionPattern.asLiteral != null)
		{
			if (this.earliestVersionPattern != null
					&& !this.earliestVersionPattern.matches(this.versionPattern.asLiteral))
			{
				throw new IllegalArgumentException("Version (literal) '" + versionPattern + "' and EarliestVersion '"
						+ earliestVersionPattern + "' cannot be both matched by the same version.");
			}

			if (this.latestVersionPattern != null && !this.latestVersionPattern.matches(this.versionPattern.asLiteral))
			{
				throw new IllegalArgumentException("Version (literal) '" + versionPattern + "' and LatestVersion '"
						+ latestVersionPattern + "' cannot be both matched by the same version.");
			}
		}

		if (this.earliestVersionPattern != null && this.earliestVersionPattern.asLiteral != null
				&& this.latestVersionPattern != null && this.latestVersionPattern.asLiteral != null
				&& earliestVersionPattern.asLiteral.compareTo(latestVersionPattern.asLiteral) > 0)
		{
			throw new IllegalArgumentException("EarliestVersion (literal) '" + earliestVersionPattern
					+ "' > LatestVersion (literal) '" + latestVersionPattern + "'!");
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
		return String.format("Version=%s,EarliestVersion=%s,LatestVersion=%s",
				(versionPattern == null) ? "*" : versionPattern,
				(earliestVersionPattern == null) ? "*" : earliestVersionPattern,
				(latestVersionPattern == null) ? "*" : latestVersionPattern);
	}

	/**
	 * Check version against LatestVersion pattern
	 * 
	 * @param version
	 *            input version to be checked
	 * @return true iff LatestVersion matched
	 */
	public boolean matchLatestVersion(final PolicyVersion version)
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
	public boolean matchEarliestVersion(final PolicyVersion version)
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
	public boolean matchVersion(final PolicyVersion version)
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
	 * Get EarliestVersion pattern: matching expression for the earliest acceptable version
	 * 
	 * @return EarliestVersion to be matched
	 */
	public String getEarliestVersionPattern()
	{
		return this.earliestVersionPattern.toString();
	}

	/**
	 * Get LatestVersion pattern: matching expression for the latest acceptable version
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

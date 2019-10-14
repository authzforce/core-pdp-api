/**
 * Copyright 2012-2019 THALES.
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
import java.util.regex.Pattern;

/**
 * Policy Version pattern (as defined by XACML VersionMatchType)
 *
 */
public final class PolicyVersionPattern
{
	private static final int WILDCARD_INT = -1;
	private static final int PLUS_INT = -2;
	private static final Integer WILDCARD_INTEGER = Integer.valueOf(WILDCARD_INT);
	private static final Integer PLUS_INTEGER = Integer.valueOf(PLUS_INT);

	private final String xacmlVersionMatch;
	private final List<Integer> matchNumbers;
	final PolicyVersion asLiteral;

	/**
	 * Constructs an instance from XACML VersionMatchType-compliant string
	 * 
	 * @param xacmlVersionMatch
	 *            XACML VersionMatchType-compliant string
	 * @throws IllegalArgumentException
	 *             {@code xacmlVersionMatch} is not a valid VersionMatch
	 */
	public PolicyVersionPattern(final String xacmlVersionMatch) throws IllegalArgumentException
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
						throw new IllegalArgumentException("Invalid VersionMatch expression: '" + xacmlVersionMatch + "'", e);
					}

					if (number.intValue() < 0)
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

	private static String convertMatchNumberToRegex(final Integer number)
	{
		switch (number)
		{
			case WILDCARD_INT:
				return "\\d+";
			case PLUS_INT:
				return "(\\d+\\.)*\\d+";
			default:
				return number.toString();
		}
	}

	/**
	 * @return the equivalent of this XACML VersionMatchType expression as regular expression (valid for {@link Pattern}) without leading '^' or trailing '$'.
	 */
	public String toRegex()
	{
		final StringBuilder regexBuilder;
		final Iterator<Integer> numberIterator = matchNumbers.iterator();
		if (numberIterator.hasNext())
		{
			regexBuilder = new StringBuilder(convertMatchNumberToRegex(numberIterator.next()));
		}
		else
		{
			return ".*";
		}

		while (numberIterator.hasNext())
		{
			regexBuilder.append("\\.");
			final String replacement = convertMatchNumberToRegex(numberIterator.next());
			regexBuilder.append(replacement);
		}

		return regexBuilder.toString();
	}

	/**
	 * Matches a given policy version
	 * 
	 * @param version
	 *            version to be matched against the pattern
	 * @return true iff the version matches
	 */
	public boolean matches(final PolicyVersion version)
	{
		final Iterator<Integer> versionNumsIterator = version.getNumberSequence().iterator();
		final Iterator<Integer> matchNumsIterator = this.matchNumbers.iterator();
		while (matchNumsIterator.hasNext() && versionNumsIterator.hasNext())
		{
			final Integer matchNum = matchNumsIterator.next();
			final Integer versionNum = versionNumsIterator.next();
			switch (matchNum.intValue())
			{
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
		 * At this point, last matchNum is either a wildcard or integer. Version matches iff there is no extra number in either matchNumbers or versionNumbers.
		 */
		return !matchNumsIterator.hasNext() && !versionNumsIterator.hasNext();
	}

	/**
	 * Checks whether the pattern matches the given version or later. This allows to enforce the LatestVersion pattern in XACML Policy(Set)IdReferences.
	 * 
	 * @param version
	 *            version to be matched/compared
	 * @return true iff this pattern matches {@code version} or later.
	 */
	public boolean isLaterOrMatches(final PolicyVersion version)
	{
		final Iterator<Integer> versionNumsIterator = version.getNumberSequence().iterator();
		final Iterator<Integer> matchNumsIterator = this.matchNumbers.iterator();
		while (matchNumsIterator.hasNext() && versionNumsIterator.hasNext())
		{
			final Integer matchNum = matchNumsIterator.next();
			final Integer versionNum = versionNumsIterator.next();
			switch (matchNum.intValue())
			{
				case PLUS_INT:
					// always matches everything from here
					return true;
				case WILDCARD_INT:
					/*
					 * Always matches any versionNumbers[i], and we could always find an acceptable version V > version argument that matches this pattern (matchNumbers) by taking a single number
					 * greater than versionNumbers[i] at the same index in V. So versionNumbers is earlier than the latest acceptable.
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
		 * At this point, we know matchNumbers is a sequence of numbers (no wildcard/plus symbol). It is later than or matches versionNumbers iff there is no extra number in versionNums.
		 */
		return !versionNumsIterator.hasNext();
	}

	/**
	 * Checks whether the pattern matches the given version or earlier. This allows to enforce the EarliestVersion pattern in XACML Policy(Set)IdReferences.
	 * 
	 * @param version
	 *            version to be matched/compared
	 * @return true iff this pattern matches {@code version} or earlier.
	 */
	public boolean isEarlierOrMatches(final PolicyVersion version)
	{
		final Iterator<Integer> versionNumsIterator = version.getNumberSequence().iterator();
		final Iterator<Integer> matchNumsIterator = this.matchNumbers.iterator();
		while (matchNumsIterator.hasNext() && versionNumsIterator.hasNext())
		{
			final Integer matchNum = matchNumsIterator.next();
			final Integer versionNum = versionNumsIterator.next();
			switch (matchNum.intValue())
			{
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

	/**
	 * Return the pattern as literal version if the internal XACML VersionMatch is actually a literal version string (no wildcard character like '*' or '+')
	 * 
	 * @return literal/constant policy version, or null if the pattern is not a literal value (based on VersionMatch expression that contains '*' or '+')
	 */
	public PolicyVersion toLiteral()
	{
		return this.asLiteral;
	}

}
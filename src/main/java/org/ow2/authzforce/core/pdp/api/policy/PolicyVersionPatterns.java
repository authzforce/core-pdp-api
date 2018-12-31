/**
 * Copyright 2012-2018 THALES.
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

import java.util.Optional;

/**
 * Version patterns used in policy references to match specific policy
 * version(s). This class also provides a simple set of comparison methods for
 * matching against the patterns.
 * 
 */
public final class PolicyVersionPatterns
{
	/**
	 * Wildcard pattern, i.e. version pattern that matches any version ('*')
	 */
	public static final PolicyVersionPattern WILDCARD = new PolicyVersionPattern("*");

	// the three constraints
	private final Optional<PolicyVersionPattern> versionPattern;
	private final Optional<PolicyVersionPattern> earliestVersionPattern;
	private final Optional<PolicyVersionPattern> latestVersionPattern;

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
	public PolicyVersionPatterns(final String versionMatch, final String earliestMatch, final String latestMatch)
			throws IllegalArgumentException
	{
		this.versionPattern = versionMatch == null ? Optional.empty()
				: Optional.of(new PolicyVersionPattern(versionMatch));
		this.earliestVersionPattern = earliestMatch == null ? Optional.empty()
				: Optional.of(new PolicyVersionPattern(earliestMatch));
		this.latestVersionPattern = latestMatch == null ? Optional.empty()
				: Optional.of(new PolicyVersionPattern(latestMatch));
		if (this.versionPattern.isPresent())
		{
			final PolicyVersion versionLiteral = this.versionPattern.get().toLiteral();
			if (versionLiteral != null)
			{
				if (this.earliestVersionPattern.isPresent()
						&& !this.earliestVersionPattern.get().matches(versionLiteral))
				{
					throw new IllegalArgumentException(
							"Version (literal) '" + versionPattern.get() + "' and EarliestVersion '"
									+ earliestVersionPattern.get() + "' cannot be both matched by the same version.");
				}

				if (this.latestVersionPattern.isPresent() && !this.latestVersionPattern.get().matches(versionLiteral))
				{
					throw new IllegalArgumentException(
							"Version (literal) '" + versionPattern.get() + "' and LatestVersion '"
									+ latestVersionPattern.get() + "' cannot be both matched by the same version.");
				}
			}
		}

		if (this.earliestVersionPattern.isPresent() && this.latestVersionPattern.isPresent())
		{
			final PolicyVersion earliestVersionLiteral = this.earliestVersionPattern.get().toLiteral();
			final PolicyVersion latestVersionLiteral = this.latestVersionPattern.get().toLiteral();
			if (earliestVersionLiteral != null && latestVersionLiteral != null
					&& earliestVersionLiteral.compareTo(latestVersionLiteral) > 0)
			{
				throw new IllegalArgumentException("EarliestVersion (literal) '" + earliestVersionPattern
						+ "' > LatestVersion (literal) '" + latestVersionPattern + "'!");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Version=%s,EarliestVersion=%s,LatestVersion=%s", versionPattern.orElse(WILDCARD),
				earliestVersionPattern.orElse(WILDCARD), latestVersionPattern.orElse(WILDCARD));
	}

	/**
	 * Check version against LatestVersion pattern
	 * 
	 * @param version
	 *            input version to be checked
	 * @return true iff LatestVersion matched
	 */
	public boolean matchLatestVersion(final PolicyVersion version) {
		return !latestVersionPattern.isPresent() || latestVersionPattern.get().isLaterOrMatches(version);
	}

	/**
	 * Check version against EarliestVersion pattern
	 * 
	 * @param version
	 *            input version to be checked
	 * @return true iff EarliestVersion matched
	 */
	public boolean matchEarliestVersion(final PolicyVersion version) {
		return !earliestVersionPattern.isPresent() || earliestVersionPattern.get().isEarlierOrMatches(version);
	}

	/**
	 * Check version against Version pattern
	 * 
	 * @param version
	 *            input version to be checked
	 * @return true iff Version matched
	 */
	public boolean matchVersion(final PolicyVersion version) {
		return !versionPattern.isPresent() || versionPattern.get().matches(version);
	}

	/**
	 * Get Version pattern:
	 * 
	 * @return Version to be matched; null if none
	 */
	public Optional<PolicyVersionPattern> getVersionPattern() {
		return this.versionPattern;
	}

	/**
	 * Get EarliestVersion pattern: matching expression for the earliest
	 * acceptable version
	 * 
	 * @return EarliestVersion pattern to be matched; null if none
	 */
	public Optional<PolicyVersionPattern> getEarliestVersionPattern() {
		return this.earliestVersionPattern;
	}

	/**
	 * Get LatestVersion pattern: matching expression for the latest acceptable
	 * version
	 * 
	 * @return LatestVersion pattern to be matched; null if none
	 */
	public Optional<PolicyVersionPattern> getLatestVersionPattern() {
		return this.latestVersionPattern;
	}

	// public static void main(String... args)
	// {
	// PolicyVersionPattern vp = new PolicyVersionPattern("0.+");
	// PolicyVersion v = new PolicyVersion("1.2.4.5");
	// System.out.println(vp.isLaterOrMatches(v));
	// }
}

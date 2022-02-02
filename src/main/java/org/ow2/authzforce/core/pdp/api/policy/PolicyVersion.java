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
package org.ow2.authzforce.core.pdp.api.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Representation of XACML VersionType:
 * <p>
 * 
 * <pre>
 * {@code 
 * <xs:simpleType name="VersionType"> 
 * 	<xs:restriction base="xs:string"> 
 * 		<xs:pattern value="(\d+\.)*\d+"/> 
 * 	</xs:restriction> 
 * </xs:simpleType>
 * }
 * </pre>
 * 
 * 
 */
public final class PolicyVersion implements Comparable<PolicyVersion>
{
	private static final IllegalArgumentException UNDEFINED_VERSION_EXCEPTION = new IllegalArgumentException(
			"Policy(Set) Version undefined");
	private static final IllegalArgumentException UNDEFINED_COMPARED_VERSION_EXCEPTION = new IllegalArgumentException(
			"Other Policy(Set) Version for comparison is undefined");

	private final String version;
	private final List<Integer> numbers;

	// cached hashCode() result
	private transient volatile int hashCode = 0; // Effective Java - Item 9

	PolicyVersion(final String version, final List<Integer> numbers)
	{
		assert version != null;
		this.numbers = Collections.unmodifiableList(numbers);
		this.version = version;
	}

	/**
	 * Creates instance from version in text form
	 * 
	 * @param version
	 *            version string
	 * @throws IllegalArgumentException
	 *             if version is null or not valid according to pattern definition in XACML schema: "(\d+\.)*\d+"
	 */
	public PolicyVersion(final String version) throws IllegalArgumentException
	{
		if (version == null)
		{
			throw UNDEFINED_VERSION_EXCEPTION;
		}

		if (version.isEmpty() || version.startsWith(".") || version.endsWith("."))
		{
			throw new IllegalArgumentException("Invalid Policy(Set) Version: '" + version + "'");
		}

		final String[] tokens = version.split("\\.");
		final List<Integer> intTokens = new ArrayList<>(tokens.length);
		for (int i = 0; i < tokens.length; i++)
		{
			final Integer number;
			try
			{
				number = Integer.valueOf(tokens[i], 10);
			}
			catch (final NumberFormatException e)
			{
				throw new IllegalArgumentException("Invalid Policy(Set) Version: '" + version + "'", e);
			}

			if (number < 0)
			{
				throw new IllegalArgumentException("Invalid Policy(Set) Version: '" + version + "'. Number #" + i
						+ " (=" + number + ") is not a positive integer");
			}

			intTokens.add(number);
		}

		this.numbers = Collections.unmodifiableList(intTokens);
		this.version = version;
	}

	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = numbers.hashCode();
		}

		return hashCode;
	}

	@Override
	public boolean equals(final Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof PolicyVersion))
		{
			return false;
		}

		final PolicyVersion other = (PolicyVersion) obj;
		return this.numbers.equals(other.numbers);
	}

	@Override
	public int compareTo(final PolicyVersion other)
	{
		if (other == null)
		{
			throw UNDEFINED_COMPARED_VERSION_EXCEPTION;
		}

		final Iterator<Integer> thisIterator = this.numbers.iterator();
		final Iterator<Integer> otherIterator = other.numbers.iterator();
		while (thisIterator.hasNext() && otherIterator.hasNext())
		{
			final int comparResult = thisIterator.next().compareTo(otherIterator.next());
			// if not equal number, we're done
			if (comparResult != 0)
			{
				return comparResult;
			}
		}

		return thisIterator.hasNext() ? 1 : otherIterator.hasNext() ? -1 : 0;
	}

	// public static void main(String[] args)
	// {
	// final PolicyVersion v1 = new PolicyVersion("1.0.0");
	// final PolicyVersion v2 = new PolicyVersion("1.0.1");
	// System.out.println(v1.compareTo(v2));
	// }

	/**
	 * Return the original version string from which this object was instantiated
	 */
	@Override
	public String toString()
	{
		return version;
	}

	/**
	 * Get version as sequence of positive integers
	 * 
	 * @return sequence of positive integers from version
	 */
	public List<Integer> getNumberSequence()
	{
		return numbers;
	}

}

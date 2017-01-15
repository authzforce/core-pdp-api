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

import javax.security.auth.x500.X500Principal;

/**
 * Representation of an X.500 Directory Name.
 *
 * 
 * @version $Id: $
 */
public final class X500NameValue extends SimpleValue<String>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * XACML datatype URI
	 */
	public static final String TYPE_URI = "urn:oasis:names:tc:xacml:1.0:data-type:x500Name";

	private final X500Principal x500Name;

	private transient volatile int hashCode = 0; // Effective Java - Item 9

	/**
	 * Returns a new <code>X500NameAttributeValue</code> that represents the X500 Name value indicated by the string provided.
	 *
	 * @param value
	 *            a string representing the desired value
	 * @throws java.lang.IllegalArgumentException
	 *             if value is not a valid XACML X500Name
	 */
	public X500NameValue(final String value) throws IllegalArgumentException
	{
		super(TYPE_URI, value);
		try
		{
			this.x500Name = new X500Principal(value);
		}
		catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Invalid value (X.500 Distinguished Name) for datatype: " + TYPE_URI, e);
		}
	}

	/**
	 * Implements XACML function 'urn:oasis:names:tc:xacml:1.0:function:x500Name-match' with this as first argument.
	 *
	 * @param other
	 *            the second argument
	 * @return true if and only if this matches some terminal sequence of RDNs from the <code>other</other>'s value when compared using x500Name-equal.
	 */
	public boolean match(final X500NameValue other)
	{
		final String otherCanonicalName = other.x500Name.getName(X500Principal.CANONICAL);
		final String thisCanonicalName = this.x500Name.getName(X500Principal.CANONICAL);
		final boolean isStringSuffix = otherCanonicalName.endsWith(thisCanonicalName);
		if (!isStringSuffix)
		{
			return false;
		}

		/*
		 * Not enough to be a String suffix, thisCanonicalName must be a terminal sequence of RDNs of otherCanonicalName
		 */
		final int otherNameLen = otherCanonicalName.length();
		final int thisNameLen = thisCanonicalName.length();
		if (otherNameLen == thisNameLen)
		{
			// same string
			return true;
		}

		// otherNameLen >= thisNameLen +1
		/*
		 * Not the same length so otherCanonicalName must be: '...x,thisCanonicalName' where x is not an escape string for ','. Index otherCanonicalName.length() - (thisCanonicalName.length() + 1)
		 * corresponds to ',' in otherCanonicalName
		 */
		final int indexBeforeSuffix = otherNameLen - (thisNameLen + 1);
		if (otherCanonicalName.charAt(indexBeforeSuffix) != ',')
		{
			return false;
		}

		/*
		 * We have a comma before this name.
		 */
		if (otherNameLen <= thisNameLen + 1)
		{
			// nothing else before the comma
			return true;
		}

		// otherNameLen >= thisNameLen +2
		/*
		 * We have another character before the comma. Make sure this is not a backslash to escape the comma.
		 */
		return otherCanonicalName.charAt(indexBeforeSuffix - 1) != '\\';
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = this.x500Name.hashCode();
		}

		return hashCode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof X500NameValue))
		{
			return false;
		}

		final X500NameValue other = (X500NameValue) obj;
		/*
		 * This equals() has the same effect as the algorithm described in the spec
		 */
		return x500Name.equals(other.x500Name);
	}

	/** {@inheritDoc} */
	@Override
	public String printXML()
	{
		return this.value;
	}

	// For quick testing
	// public static void main(final String[] args) throws InvalidNameException
	// {
	// System.out.println(new LdapName("cn=John Smith, o=Medico Corp, c=US").equals(new
	// LdapName("cn= John Smith,o =Medico Corp, C=US")));
	// System.out.println(new LdapName(escapeDN("ou=test+cn=bob,dc =example,dc=com")));
	// System.out.println(new LdapName("cn=John Smith, o=Medico Corp, c=US").endsWith(new
	// LdapName("o=Medico Corp, c=US").getRdns()));
	// }

}

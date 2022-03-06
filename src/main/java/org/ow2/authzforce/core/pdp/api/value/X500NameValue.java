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
package org.ow2.authzforce.core.pdp.api.value;

import javax.security.auth.x500.X500Principal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import org.ow2.authzforce.xacml.identifiers.XacmlDatatypeId;

/**
 * Representation of an X.500 Directory Name.
 *
 * 
 * @version $Id: $
 */
public final class X500NameValue extends StringParseableValue<String>
{
	private final X500Principal x500Name;

	private transient volatile int hashCode = 0; // Effective Java - Item 9

	private transient volatile XdmItem xdmItem = null;


	/**
	 * Creates a new <code>X500NameValue</code> from an {@link X500Principal}.
	 *
	 * @param value
	 *            a string representing the X500 name (considered immutable, therefore suppress Spotbugs warning)
	 * @throws java.lang.IllegalArgumentException
	 *             if value is not a valid XACML X500Name
	 */
	@SuppressFBWarnings(value="EI_EXPOSE_REP2", justification="X500Principal is immutable")
	public X500NameValue(final X500Principal value) throws IllegalArgumentException
	{
		super(value.getName());
		this.x500Name = value;
	}

	/**
	 * Creates a new <code>X500NameValue</code> from string form
	 *
	 * @param value
	 *            a string representing the X500 name
	 * @throws java.lang.IllegalArgumentException
	 *             if value is not a valid XACML X500Name
	 */
	public X500NameValue(final String value) throws IllegalArgumentException
	{
		super(value);
		try
		{
			this.x500Name = new X500Principal(value);
		} catch (final IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Invalid value (X.500 Distinguished Name) for datatype: " + XacmlDatatypeId.X500_NAME.value(), e);
		}
	}

	@SuppressFBWarnings(value="EI_EXPOSE_REP", justification="According to Saxon documentation, an XdmValue is immutable.")
	@Override
	public XdmItem getXdmItem()
	{
		if(xdmItem == null)
		{
			// Mapped to String to match ItemType declared in StandardDatatypes class
			xdmItem = new XdmAtomicValue(value);
		}

		return xdmItem;
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

/**
 * Copyright (C) 2012-2015 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.api;

/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This is the base class that all <code>AttributeProvider</code> modules extend.
 */
public abstract class BaseAttributeProviderModule implements CloseableAttributeProviderModule
{

	protected static final UnsupportedOperationException UNSUPPORTED_ATTRIBUTE_CATEGORY_EXCEPTION = new UnsupportedOperationException(
			"Unsupported attribute category");
	protected static final UnsupportedOperationException UNSUPPORTED_ATTRIBUTE_ISSUER_EXCEPTION = new UnsupportedOperationException(
			"Unsupported attribute issuer");
	protected static final UnsupportedOperationException UNSUPPORTED_ATTRIBUTE_ID_EXCEPTION = new UnsupportedOperationException("Unsupported attribute ID");
	protected static final UnsupportedOperationException UNSUPPORTED_ATTRIBUTE_DATATYPE_EXCEPTION = new UnsupportedOperationException(
			"Unsupported attribute datetype");
	private static final IllegalArgumentException UNDEF_MODULE_INSTANCE_ID = new IllegalArgumentException("Undefined attribute Provider module's instance ID");

	private final String instanceID;

	// cached method result
	private transient final int hashCode;
	private transient final String toString;

	/**
	 * Instantiates the attribute Provider module
	 * 
	 * @param instanceID
	 *            module instance ID (to be used as unique identifier for this instance in the logs for example);
	 * @param attributeFactory
	 *            factory for creating attribute values
	 * @param depAttributeProvider
	 *            dependency attribute Provider. This module may require other attributes as input to do the job. As it does not know how to get them (it is not
	 *            its job), it may call this {@code depAttributeProvider} to get them on its behalf.
	 * @throws IllegalArgumentException
	 *             if instanceId null
	 */
	protected BaseAttributeProviderModule(String instanceID) throws IllegalArgumentException
	{
		if (instanceID == null)
		{
			throw UNDEF_MODULE_INSTANCE_ID;
		}

		this.instanceID = instanceID;
		this.hashCode = instanceID.hashCode();
		this.toString = "AttributeProvider[" + instanceID + "]";
	}

	/**
	 * Get user-defined ID for this module instance
	 * 
	 * @return instance ID
	 */
	public final String getInstanceID()
	{
		return this.instanceID;
	}

	@Override
	public int hashCode()
	{
		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof BaseAttributeProviderModule))
		{
			return false;
		}

		final BaseAttributeProviderModule other = (BaseAttributeProviderModule) obj;
		return this.instanceID.equals(other.instanceID);
	}

	@Override
	public String toString()
	{
		return toString;
	}

}

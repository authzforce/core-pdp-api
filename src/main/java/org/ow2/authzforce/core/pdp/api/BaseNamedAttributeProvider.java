/**
 * Copyright 2012-2021 THALES.
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
package org.ow2.authzforce.core.pdp.api;

/**
 * This is a convenience class to help implement {@link CloseableNamedAttributeProvider}.
 */
public abstract class BaseNamedAttributeProvider implements CloseableNamedAttributeProvider
{

	private static final IllegalArgumentException UNDEF_INSTANCE_ID = new IllegalArgumentException("Undefined Attribute Provider's instance ID");

	private final String instanceID;

	// cached method result
	private transient final int hashCode;
	private transient final String toString;

	/**
	 * Instantiates the Attribute Provider
	 * 
	 * @param instanceID
	 *            instance ID (to be used as unique identifier for this instance in the logs for example);
	 * @throws IllegalArgumentException
	 *             if instanceId null
	 */
	protected BaseNamedAttributeProvider(final String instanceID) throws IllegalArgumentException
	{
		if (instanceID == null)
		{
			throw UNDEF_INSTANCE_ID;
		}

		this.instanceID = instanceID;
		this.hashCode = instanceID.hashCode();
		this.toString = "AttributeProvider[" + instanceID + "]";
	}

	/**
	 * Get user-defined ID for this instance
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
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof BaseNamedAttributeProvider))
		{
			return false;
		}

		final BaseNamedAttributeProvider other = (BaseNamedAttributeProvider) obj;
		return this.instanceID.equals(other.instanceID);
	}

	@Override
	public String toString()
	{
		return toString;
	}

}

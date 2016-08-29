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
package org.ow2.authzforce.core.pdp.api;

import java.util.Objects;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

/**
 * This class provides a skeletal implementation of the {@link DecisionResult} interface to minimize the effort required
 * to implement this interface. Note that this class only overrides/implements {@link #equals(Object)},
 * {@link #hashCode()} and {@link #getStatus()}.
 */
public abstract class AbstractDecisionResult implements DecisionResult
{
	protected final Status status;

	private transient volatile int hashCode = 0;

	protected AbstractDecisionResult(final Status status)
	{
		this.status = status;
	}

	@Override
	public final Status getStatus()
	{
		return this.status;
	}

	/** {@inheritDoc} */
	@Override
	public final int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(this.getDecision(), this.getExtendedIndeterminate(), this.status,
					this.getPepActions());
		}

		return hashCode;
	}

	/** {@inheritDoc} */
	@Override
	public final boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof DecisionResult))
		{
			return false;
		}

		final DecisionResult other = (DecisionResult) obj;
		if (this.getDecision() != other.getDecision()
				|| this.getExtendedIndeterminate() != other.getExtendedIndeterminate())
		{
			return false;
		}

		// Status is optional in XACML
		if (this.status == null)
		{
			if (other.getStatus() != null)
			{
				return false;
			}
		}
		else if (!this.status.equals(other.getStatus()))
		{
			return false;
		}

		final PepActions otherPepActions = other.getPepActions();
		final PepActions thisPepActions = other.getPepActions();
		if (thisPepActions == null)
		{
			return otherPepActions == null || otherPepActions.isEmpty();
		}

		return thisPepActions.equals(otherPepActions);
	}
}
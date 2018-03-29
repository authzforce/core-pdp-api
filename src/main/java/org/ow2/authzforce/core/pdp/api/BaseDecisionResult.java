/**
 * Copyright 2012-2018 Thales Services SAS.
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

import java.util.Objects;

import com.google.common.collect.ImmutableList;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

/**
 * This class provides a skeletal implementation of the {@link DecisionResult} interface to minimize the effort required to implement this interface. Note that this class only overrides/implements
 * {@link #equals(Object)}, {@link #hashCode()} and {@link #getStatus()}.
 */
public abstract class BaseDecisionResult implements DecisionResult
{
	private final Status status;

	private transient volatile int hashCode = 0;

	protected BaseDecisionResult(final Status status)
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
			hashCode = Objects.hash(this.getDecision(), this.getExtendedIndeterminate(), this.status, this.getPepActions());
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
		if (this.getDecision() != other.getDecision() || this.getExtendedIndeterminate() != other.getExtendedIndeterminate())
		{
			return false;
		}

		// Status is optional in XACML
		if (!Objects.equals(this.status, other.getStatus()))
		{
			return false;
		}

		final ImmutableList<PepAction> otherPepActions = other.getPepActions();
		assert otherPepActions != null;
		final ImmutableList<PepAction> thisPepActions = this.getPepActions();

		return thisPepActions.equals(otherPepActions);
	}
}
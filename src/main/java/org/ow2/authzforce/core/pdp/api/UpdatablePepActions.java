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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;

import com.google.common.collect.ImmutableList;

/**
 * Updatable PEP actions (obligations/advice). "Updatable" means here that it only accepts new PEP actions (cannot be removed from it)
 *
 * @version $Id: $
 */
public final class UpdatablePepActions implements PepActions
{
	// always non-null fields
	private final List<Obligation> obligationList;
	private final List<Advice> adviceList;

	private transient volatile int hashCode = 0;

	/**
	 * Instantiates PEP actions set from obligations/advice
	 *
	 * @param obligations
	 *            obligation list; null if no obligation
	 * @param advices
	 *            advice list; null if no advice
	 */
	public UpdatablePepActions(final Optional<List<Obligation>> obligations, final Optional<List<Advice>> advices)
	{
		this.obligationList = obligations.orElse(new ArrayList<>());
		this.adviceList = advices.orElse(new ArrayList<>());
	}

	/**
	 * Instantiates PEP actions set initially with empty obligation and advice list
	 */
	public UpdatablePepActions()
	{
		this(Optional.empty(), Optional.empty());
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get the internal obligation list
	 */
	@Override
	public ImmutableList<Obligation> getObligatory()
	{
		return ImmutableList.copyOf(obligationList);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get the internal advice list
	 */
	@Override
	public ImmutableList<Advice> getAdvisory()
	{
		return ImmutableList.copyOf(adviceList);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(this.obligationList, this.adviceList);
		}

		return hashCode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof UpdatablePepActions))
		{
			return false;
		}

		final UpdatablePepActions other = (UpdatablePepActions) obj;
		return this.obligationList.equals(other.obligationList) && this.adviceList.equals(other.adviceList);
	}

	/**
	 * Merge extra PEP actions. Used when combining results from child Rules of Policy or child Policies of PolicySet
	 * 
	 * @param newObligations
	 *            extra obligations
	 * @param newAdviceList
	 *            extra advice elements
	 */
	public void addAll(final List<Obligation> newObligations, final List<Advice> newAdviceList)
	{
		if (newObligations != null)
		{
			this.obligationList.addAll(newObligations);
		}

		if (newAdviceList != null)
		{
			this.adviceList.addAll(newAdviceList);
		}
	}

	/**
	 * 
	 * Merge extra PEP actions. Used when combining results from child Rules of Policy or child Policies of PolicySet
	 * 
	 * @param pepActions
	 *            extra PEP actions (obligations/advice)
	 */
	public void add(final PepActions pepActions)
	{
		if (pepActions == null)
		{
			return;
		}

		addAll(pepActions.getObligatory(), pepActions.getAdvisory());
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "[obligations=" + obligationList + ", advices=" + adviceList + "]";
	}

	@Override
	public boolean isEmpty()
	{
		return this.obligationList.isEmpty() && this.adviceList.isEmpty();
	}

}

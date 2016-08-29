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

import com.google.common.collect.ImmutableList;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;

/**
 * Static immutable {@link PepActions}, e.g. empty actions, or static methods returning immutable {@link PepActions} or
 *
 */
public final class ImmutablePepActions implements PepActions
{

	// always non-null fields, immutable
	private final ImmutableList<Obligation> obligationList;
	private final ImmutableList<Advice> adviceList;

	private transient volatile int hashCode;

	private transient volatile String toString;

	private ImmutablePepActions()
	{
		this.obligationList = ImmutableList.of();
		this.adviceList = ImmutableList.of();
		this.hashCode = 1;
		this.toString = "( obligation_list= null, advice_list= null )";
	}

	/**
	 * Instantiates PEP action set from obligations/advice. Not used if there is neither any obligation and nor any
	 * advice, in which case the {@link ImmutablePepActions#EMPTY} must be used.
	 *
	 * @param obligationList
	 *            obligation list; null if no obligation
	 * @param adviceList
	 *            advice list; null if no advice
	 */
	private ImmutablePepActions(ImmutableList<Obligation> obligationList, ImmutableList<Advice> adviceList)
	{
		assert obligationList != null && !obligationList.isEmpty() || adviceList != null && !adviceList.isEmpty();

		this.obligationList = obligationList == null ? ImmutableList.<Obligation>of() : obligationList;
		this.adviceList = adviceList == null ? ImmutableList.<Advice>of() : adviceList;
		this.hashCode = 0;
		this.toString = null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get the internal obligation list
	 */
	@Override
	public ImmutableList<Obligation> getObligatory()
	{
		return obligationList;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get the internal advice list
	 */
	@Override
	public ImmutableList<Advice> getAdvisory()
	{
		return adviceList;
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
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof PepActions))
		{
			return false;
		}

		final PepActions other = (PepActions) obj;
		return this.obligationList.equals(other.getObligatory()) && this.adviceList.equals(other.getAdvisory());
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		if (toString == null)
		{
			toString = "[obligation_list=" + obligationList + ", advice_list=" + adviceList + "]";
		}

		return toString;
	}

	/**
	 * Get immutable {@link PepActions} from obligations/advice elements.
	 * 
	 * @param obligationList
	 *            obligations; null if no obligation
	 * @param adviceList
	 *            advice elements; null if no advice
	 * @return immutable instance; {@link ImmutablePepActions#EMPTY} is returned if
	 *         {@code obligationList == null || obligationList.isEmpty()) && (adviceList == null || adviceList.isEmpty()}
	 */
	public static ImmutablePepActions getInstance(ImmutableList<Obligation> obligationList,
			ImmutableList<Advice> adviceList)
	{
		if ((obligationList == null || obligationList.isEmpty()) && (adviceList == null || adviceList.isEmpty()))
		{
			return EMPTY;
		}

		return new ImmutablePepActions(obligationList, adviceList);
	}

	/**
	 * Get immutable view (copy) of other PEP actions.
	 *
	 * @param pepActions
	 *            obligations; null if no obligation
	 *
	 * @return immutable instance; {@link ImmutablePepActions#EMPTY} is returned if
	 *         {@code pepActions == null || (obligationList == null || obligationList.isEmpty()) && (adviceList == null ||
	 adviceList.isEmpty())}
	 */
	public static ImmutablePepActions getInstance(PepActions pepActions)
	{
		if (pepActions == null)
		{
			return EMPTY;
		}

		return getInstance(pepActions.getObligatory(), pepActions.getAdvisory());
	}

	/**
	 * Empty PEP actions (no obligation/advice)
	 */
	public static final ImmutablePepActions EMPTY = new ImmutablePepActions();

	@Override
	public boolean isEmpty()
	{
		return this.obligationList.isEmpty() && this.adviceList.isEmpty();
	}
}
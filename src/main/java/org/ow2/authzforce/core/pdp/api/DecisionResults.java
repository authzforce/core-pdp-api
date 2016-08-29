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

import javax.xml.bind.JAXBElement;

import com.google.common.collect.ImmutableList;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

/**
 * Factory for creating immutable {@link DecisionResult}s
 *
 */
public final class DecisionResults
{
	private static final class ImmutableNotApplicableResult extends AbstractDecisionResult
	{
		private transient volatile String toString = null;

		/* For NotApplicable result */
		private ImmutableNotApplicableResult(final Status status)
		{
			super(status);
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			if (toString == null)
			{
				toString = "Result( decision= NotApplicable, status=" + status + " )";
			}
			return toString;
		}

		@Override
		public ImmutablePepActions getPepActions()
		{
			return null;
		}

		@Override
		public ImmutableList<JAXBElement<IdReferenceType>> getApplicablePolicies()
		{
			return null;
		}

		@Override
		public DecisionType getDecision()
		{
			return DecisionType.NOT_APPLICABLE;
		}

		@Override
		public DecisionType getExtendedIndeterminate()
		{
			return DecisionType.NOT_APPLICABLE;
		}
	}

	private static abstract class ApplicableResult extends AbstractDecisionResult
	{

		protected final ImmutableList<JAXBElement<IdReferenceType>> applicablePolicyIdList;

		private ApplicableResult(final Status status,
				final ImmutableList<JAXBElement<IdReferenceType>> applicablePolicyIdList)
		{
			super(status);

			this.applicablePolicyIdList = applicablePolicyIdList == null
					? ImmutableList.<JAXBElement<IdReferenceType>>of() : applicablePolicyIdList;
		}

		@Override
		public final ImmutableList<JAXBElement<IdReferenceType>> getApplicablePolicies()
		{
			return this.applicablePolicyIdList;
		}

	}

	private static final class ImmutableIndeterminateResult extends ApplicableResult
	{
		/**
		 * Extended Indeterminate value, as defined in section 7.10 of XACML 3.0 core: <i>potential effect value which
		 * could have occurred if there would not have been an error causing the “Indeterminate”</i>. We use the
		 * following convention:
		 * <ul>
		 * <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
		 * <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
		 * <li>{@link DecisionType#INDETERMINATE} means "Indeterminate{DP}"</li>
		 * <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means the decision is not Indeterminate, and
		 * therefore any extended Indeterminate value should be ignored</li>
		 * </ul>
		 * 
		 */
		private final DecisionType extIndeterminate;

		private transient volatile String toString = null;

		private ImmutableIndeterminateResult(final DecisionType extendedIndeterminate, final Status status,
				final ImmutableList<JAXBElement<IdReferenceType>> applicablePolicyIdList)
		{
			super(status, applicablePolicyIdList);
			/*
			 * There must be a reason for indeterminate indicated in the status, therefore status != null
			 */
			assert extendedIndeterminate != null && status != null;

			this.extIndeterminate = extendedIndeterminate;
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			if (toString == null)
			{
				toString = "Result( decision= Indeterminate, extendedIndeterminate=" + extIndeterminate + ", status="
						+ status + " )";
			}
			return toString;
		}

		@Override
		public ImmutablePepActions getPepActions()
		{
			return null;
		}

		@Override
		public DecisionType getDecision()
		{
			return DecisionType.INDETERMINATE;
		}

		@Override
		public DecisionType getExtendedIndeterminate()
		{
			return this.extIndeterminate;
		}

	}

	// Immutable Deny/Permit result
	private static final class ImmutableDPResult extends ApplicableResult
	{

		private final DecisionType decision;

		private final ImmutablePepActions pepActions;

		private transient volatile String toString = null;

		/*
		 * For permit/deny result
		 */
		private ImmutableDPResult(final DecisionType decision, final Status status,
				final ImmutablePepActions pepActions,
				final ImmutableList<JAXBElement<IdReferenceType>> applicablePolicyIdList)
		{
			super(status, applicablePolicyIdList);
			assert decision == DecisionType.PERMIT || decision == DecisionType.DENY;

			this.decision = decision;
			this.pepActions = pepActions == null ? ImmutablePepActions.EMPTY : pepActions;
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			if (toString == null)
			{
				toString = "Result( decision=" + decision + ", status=" + status + ", pepActions=" + pepActions
						+ ", applicablePolicies= " + applicablePolicyIdList + " )";
			}
			return toString;
		}

		@Override
		public DecisionType getDecision()
		{
			return this.decision;
		}

		@Override
		public DecisionType getExtendedIndeterminate()
		{
			return DecisionType.NOT_APPLICABLE;
		}

		@Override
		public ImmutablePepActions getPepActions()
		{
			return this.pepActions;
		}
	}

	private static final RuntimeException NULL_DECISION_ARG_RUNTIME_EXCEPTION = new RuntimeException(
			"Undefined decision");

	private static final RuntimeException NULL_INDETERMINATE_CAUSE_RUNTIME_EXCEPTION = new RuntimeException(
			"No cause provided for Indeterminate result");

	/**
	 * Simple immutable Permit Decision result (no status, no obligation/advice)
	 */
	public static final DecisionResult SIMPLE_PERMIT = new ImmutableDPResult(DecisionType.PERMIT, null, null, null);

	/**
	 * Simple immutable Deny Decision result (no status, no obligation/advice)
	 */
	public static final DecisionResult SIMPLE_DENY = new ImmutableDPResult(DecisionType.DENY, null, null, null);

	/**
	 * Simple immutable NotApplicable Decision result (no status)
	 */
	public static final DecisionResult SIMPLE_NOT_APPLICABLE = new ImmutableNotApplicableResult(null);

	/**
	 * Instantiates a Permit decision with optional PEP actions (obligations and advice).
	 * 
	 *
	 * @param status
	 *            status; even if decision is Permit/Deny, there may be a status "ok" (standard status in XACML 3.0) or
	 *            internal error on attribute resolution but not resulting in Indeterminate because of special combining
	 *            algorithm ignoring such results (like deny-unless-permit) or MustBePresent="false"
	 * @param pepActions
	 *            PEP actions (obligations/advices)
	 * @param applicablePolicyIdList
	 *            list of identifiers of applicable policies that contributed to this result. If not null, the created
	 *            instance uses only an immutable copy of this list.
	 * @return permit result, more particularly {@link #SIMPLE_PERMIT} iff
	 *         {@code status  == null && pepActions == null}.
	 */
	public static DecisionResult getPermit(final Status status, final ImmutablePepActions pepActions,
			final ImmutableList<JAXBElement<IdReferenceType>> applicablePolicyIdList)
	{
		if (status == null && (pepActions == null || pepActions.isEmpty())
				&& (applicablePolicyIdList == null || applicablePolicyIdList.isEmpty()))
		{
			return SIMPLE_PERMIT;
		}

		return new ImmutableDPResult(DecisionType.PERMIT, status, pepActions, applicablePolicyIdList);
	}

	/**
	 * Instantiates a Deny decision with optional PEP actions (obligations and advice).
	 * 
	 *
	 * @param status
	 *            status; even if decision is Permit/Deny, there may be a status "ok" (standard status in XACML 3.0) or
	 *            internal error on attribute resolution but not resulting in Indeterminate because of special combining
	 *            algorithm ignoring such results (like deny-unless-permit) or MustBePresent="false"
	 * @param pepActions
	 *            PEP actions (obligations/advices)
	 * @param applicablePolicyIdList
	 *            list of identifiers of applicable policies that contributed to this result. If not null, the created
	 *            instance uses only an immutable copy of this list.
	 * @return deny result, more particularly {@link #SIMPLE_DENY} iff {@code status  == null && pepActions == null}.
	 */
	public static DecisionResult getDeny(final Status status, final ImmutablePepActions pepActions,
			final ImmutableList<JAXBElement<IdReferenceType>> applicablePolicyIdList)
	{
		if (status == null && (pepActions == null || pepActions.isEmpty())
				&& (applicablePolicyIdList == null || applicablePolicyIdList.isEmpty()))
		{
			return SIMPLE_DENY;
		}

		return new ImmutableDPResult(DecisionType.DENY, status, pepActions, applicablePolicyIdList);
	}

	/**
	 * Instantiates a NotApplicable decision with optional status.
	 * 
	 *
	 * @param status
	 *            status; even if decision is NotApplicable, there may be a status "ok" (standard status in XACML 3.0)
	 *            or internal error on attribute resolution but not resulting in Indeterminate because of special
	 *            combining algorithm ignoring such results (like deny-unless-permit) or MustBePresent="false"
	 * @return deny result, more particularly {@link #SIMPLE_DENY} iff {@code status  == null && pepActions == null}.
	 */
	public static DecisionResult getNotApplicable(final Status status)
	{
		if (status == null)
		{
			return SIMPLE_NOT_APPLICABLE;
		}

		return new ImmutableNotApplicableResult(status);
	}

	/**
	 * Instantiates a Indeterminate Decision result with a given error status
	 *
	 * @param extendedIndeterminate
	 *            Extended Indeterminate value (XACML 3.0 Core, section 7.10). We use the following convention:
	 *            <ul>
	 *            <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
	 *            <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
	 *            <li>{@link DecisionType#INDETERMINATE} or null means "Indeterminate{DP}"</li>
	 *            <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means the decision is not
	 *            Indeterminate, and therefore any extended Indeterminate value should be ignored</li>
	 *            </ul>
	 * @param cause
	 *            reason/code for Indeterminate
	 * @param applicablePolicyIdList
	 *            list of identifiers of applicable policies that contributed to this result. If not null, the created
	 *            instance uses only an immutable copy of this list.
	 * @return Indeterminate result
	 */
	public static DecisionResult newIndeterminate(final DecisionType extendedIndeterminate, final Status cause,
			final ImmutableList<JAXBElement<IdReferenceType>> applicablePolicyIdList)
	{
		if (cause == null)
		{
			throw NULL_INDETERMINATE_CAUSE_RUNTIME_EXCEPTION;
		}

		return new ImmutableIndeterminateResult(
				extendedIndeterminate == null ? DecisionType.INDETERMINATE : extendedIndeterminate, cause,
				applicablePolicyIdList);
	}

	/**
	 * Get immutable decision result from extended decision and obligations/advice elements
	 * 
	 * @param extendedDecision
	 *            extended decision
	 * @param pepActions
	 *            obligations/advice elements
	 * @param applicablePolicyIdList
	 *            list of identifiers of applicable policies that contributed to this result. If not null, the created
	 *            instance uses only an immutable copy of this list.
	 * @return decision result
	 */
	public static DecisionResult getInstance(final ExtendedDecision extendedDecision,
			final ImmutablePepActions pepActions,
			final ImmutableList<JAXBElement<IdReferenceType>> applicablePolicyIdList)
	{
		if (extendedDecision == null)
		{
			throw NULL_DECISION_ARG_RUNTIME_EXCEPTION;
		}

		final DecisionType decision = extendedDecision.getDecision();
		if (decision == null)
		{
			throw NULL_DECISION_ARG_RUNTIME_EXCEPTION;
		}

		final Status status = extendedDecision.getStatus();
		switch (decision) {
			case PERMIT:
				return getPermit(status, pepActions, applicablePolicyIdList);
			case DENY:
				return getDeny(status, pepActions, applicablePolicyIdList);
			case NOT_APPLICABLE:
				return getNotApplicable(status);
			default: // INDETERMINATE
				return newIndeterminate(extendedDecision.getExtendedIndeterminate(), status, applicablePolicyIdList);
		}
	}

	private DecisionResults()
	{
		// prevent instantiation
	}

}
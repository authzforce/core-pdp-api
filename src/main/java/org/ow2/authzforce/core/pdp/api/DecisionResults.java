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

import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

/**
 * Factory for creating immutable {@link DecisionResult}s
 *
 */
public final class DecisionResults
{
	private static final class ImmutableNotApplicableResult extends BaseDecisionResult
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
				toString = "Result[ decision= NotApplicable, status=" + getStatus() + " ]";
			}
			return toString;
		}

		@Override
		public ImmutableList<PepAction> getPepActions()
		{
			return ImmutableList.of();
		}

		@Override
		public ImmutableList<PrimaryPolicyMetadata> getApplicablePolicies()
		{
			return ImmutableList.of();
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

		@Override
		public Optional<IndeterminateEvaluationException> getCauseForIndeterminate()
		{
			return Optional.empty();
		}
	}

	private static abstract class ApplicableResult extends BaseDecisionResult
	{

		protected final ImmutableList<PrimaryPolicyMetadata> applicablePolicyIdList;

		private ApplicableResult(final Status status, final ImmutableList<PrimaryPolicyMetadata> applicablePolicyIdList)
		{
			super(status);

			this.applicablePolicyIdList = applicablePolicyIdList == null ? ImmutableList.<PrimaryPolicyMetadata>of() : applicablePolicyIdList;
		}

		@Override
		public final ImmutableList<PrimaryPolicyMetadata> getApplicablePolicies()
		{
			return this.applicablePolicyIdList;
		}

	}

	private static final class ImmutableIndeterminateResult extends ApplicableResult
	{
		private final Optional<IndeterminateEvaluationException> cause;

		/**
		 * Extended Indeterminate value, as defined in section 7.10 of XACML 3.0 core: <i>potential effect value which could have occurred if there would not have been an error causing the
		 * “Indeterminate”</i>. We use the following convention:
		 * <ul>
		 * <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
		 * <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
		 * <li>{@link DecisionType#INDETERMINATE} means "Indeterminate{DP}"</li>
		 * <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means the decision is not Indeterminate, and therefore any extended Indeterminate value should be ignored</li>
		 * </ul>
		 * 
		 */
		private final DecisionType extIndeterminate;

		private transient volatile String toString = null;

		private ImmutableIndeterminateResult(final DecisionType extendedIndeterminate, final IndeterminateEvaluationException cause, final ImmutableList<PrimaryPolicyMetadata> applicablePolicyIdList)
		{
			super(cause.getTopLevelStatus(), applicablePolicyIdList);
			/*
			 * There must be a reason for indeterminate, therefore cause != null
			 */
			assert extendedIndeterminate != null;

			this.extIndeterminate = extendedIndeterminate;
			this.cause = Optional.of(cause);
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			if (toString == null)
			{
				toString = "Result( decision= Indeterminate, extendedIndeterminate=" + extIndeterminate + ", status=" + getStatus() + " )";
			}
			return toString;
		}

		@Override
		public ImmutableList<PepAction> getPepActions()
		{
			return ImmutableList.of();
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

		@Override
		public Optional<IndeterminateEvaluationException> getCauseForIndeterminate()
		{
			return cause;
		}

	}

	// Immutable Deny/Permit result
	private static final class ImmutableDPResult extends ApplicableResult
	{

		private final DecisionType decision;

		private final ImmutableList<PepAction> pepActions;

		private transient volatile String toString = null;

		/*
		 * For permit/deny result
		 */
		private ImmutableDPResult(final DecisionType decision, final Status status, final ImmutableList<PepAction> pepActions, final ImmutableList<PrimaryPolicyMetadata> applicablePolicyIdList)
		{
			super(status, applicablePolicyIdList);
			assert decision == DecisionType.PERMIT || decision == DecisionType.DENY;

			this.decision = decision;
			this.pepActions = pepActions == null ? ImmutableList.of() : pepActions;
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			if (toString == null)
			{
				toString = "Result( decision=" + decision + ", status=" + getStatus() + ", pepActions=" + pepActions + ", applicablePolicies= " + applicablePolicyIdList + " )";
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
		public ImmutableList<PepAction> getPepActions()
		{
			return this.pepActions;
		}

		@Override
		public Optional<IndeterminateEvaluationException> getCauseForIndeterminate()
		{
			return Optional.empty();
		}
	}

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
	 *            status; even if decision is Permit/Deny, there may be a status "ok" (standard status in XACML 3.0) or internal error on attribute resolution but not resulting in Indeterminate
	 *            because of special combining algorithm ignoring such results (like deny-unless-permit) or MustBePresent="false"
	 * @param pepActions
	 *            PEP actions (obligations/advices)
	 * @param applicablePolicyIdList
	 *            list of identifiers of applicable policies that contributed to this result. If not null, the created instance uses only an immutable copy of this list.
	 * @return permit result, more particularly {@link #SIMPLE_PERMIT} iff {@code status  == null && pepActions == null}.
	 */
	public static DecisionResult getPermit(final Status status, final ImmutableList<PepAction> pepActions, final ImmutableList<PrimaryPolicyMetadata> applicablePolicyIdList)
	{
		if (status == null && (pepActions == null || pepActions.isEmpty()) && (applicablePolicyIdList == null || applicablePolicyIdList.isEmpty()))
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
	 *            status; even if decision is Permit/Deny, there may be a status "ok" (standard status in XACML 3.0) or internal error on attribute resolution but not resulting in Indeterminate
	 *            because of special combining algorithm ignoring such results (like deny-unless-permit) or MustBePresent="false"
	 * @param pepActions
	 *            PEP actions (obligations/advices)
	 * @param applicablePolicyIdList
	 *            list of identifiers of applicable policies that contributed to this result. If not null, the created instance uses only an immutable copy of this list.
	 * @return deny result, more particularly {@link #SIMPLE_DENY} iff {@code status  == null && pepActions == null}.
	 */
	public static DecisionResult getDeny(final Status status, final ImmutableList<PepAction> pepActions, final ImmutableList<PrimaryPolicyMetadata> applicablePolicyIdList)
	{
		if (status == null && (pepActions == null || pepActions.isEmpty()) && (applicablePolicyIdList == null || applicablePolicyIdList.isEmpty()))
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
	 *            status; even if decision is NotApplicable, there may be a status "ok" (standard status in XACML 3.0) or internal error on attribute resolution but not resulting in Indeterminate
	 *            because of special combining algorithm ignoring such results (like deny-unless-permit) or MustBePresent="false"
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
	 *            <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means the decision is not Indeterminate, and therefore any extended Indeterminate value should be ignored</li>
	 *            </ul>
	 * @param applicablePolicyIdList
	 *            list of identifiers of applicable policies that contributed to this result. If not null, the created instance uses only an immutable copy of this list.
	 * @param cause
	 *            cause of the Indeterminate result
	 * @return Indeterminate result
	 * @throws IllegalArgumentException
	 *             if {@code cause  == null}
	 */
	public static DecisionResult newIndeterminate(final DecisionType extendedIndeterminate, final IndeterminateEvaluationException cause,
	        final ImmutableList<PrimaryPolicyMetadata> applicablePolicyIdList) throws IllegalArgumentException
	{
		Preconditions.checkNotNull(cause, "No cause defined for Indeterminate result");
		return new ImmutableIndeterminateResult(extendedIndeterminate == null ? DecisionType.INDETERMINATE : extendedIndeterminate, cause, applicablePolicyIdList);
	}

	/**
	 * Get immutable decision result from extended decision and obligations/advice elements
	 * 
	 * @param extendedDecision
	 *            extended decision
	 * @param pepActions
	 *            obligations/advice elements
	 * @param applicablePolicyIdList
	 *            list of identifiers of applicable policies that contributed to this result. If not null, the created instance uses only an immutable copy of this list.
	 * @return decision result
	 * @throws IllegalArgumentException
	 *             if
	 *             {@code extendedDecision == null || extendedDecision.getDecision() ==null || (extendedDecision.getDecision() == INDETERMINATE && !extendedDecision.getCauseForIndeterminate().isPresent())}
	 */
	public static DecisionResult getInstance(final ExtendedDecision extendedDecision, final ImmutableList<PepAction> pepActions, final ImmutableList<PrimaryPolicyMetadata> applicablePolicyIdList)
	        throws IllegalArgumentException
	{
		Preconditions.checkNotNull(extendedDecision, "Undefined extendedDecision");
		final DecisionType decision = extendedDecision.getDecision();
		Preconditions.checkNotNull(decision, "Undefined decision");
		final Status status = extendedDecision.getStatus();
		switch (decision)
		{
			case PERMIT:
				return getPermit(status, pepActions, applicablePolicyIdList);
			case DENY:
				return getDeny(status, pepActions, applicablePolicyIdList);
			case NOT_APPLICABLE:
				return getNotApplicable(status);
			default: // INDETERMINATE
				final Optional<IndeterminateEvaluationException> cause = extendedDecision.getCauseForIndeterminate();
				Preconditions.checkArgument(cause.isPresent(), "No cause defined for Indeterminate result");
				return newIndeterminate(extendedDecision.getExtendedIndeterminate(), cause.get(), applicablePolicyIdList);
		}
	}

	private DecisionResults()
	{
		// prevent instantiation
	}

}
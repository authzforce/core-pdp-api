/**
 * Copyright 2012-2019 THALES.
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
import java.util.Optional;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

import com.google.common.base.Preconditions;

/**
 * (Immutable) ExtendedDecision factory
 *
 */
public final class ExtendedDecisions
{
	/**
	 * Simple Permit/Deny/NotApplicable decision (no status/obligation/advice), used for constants only
	 */
	private static final class SimpleImmutableExtendedDecision implements ExtendedDecision
	{
		private final DecisionType decision;

		private final int hashCode;

		private final String toString;

		private SimpleImmutableExtendedDecision(final DecisionType decision)
		{
			assert decision != null && decision != DecisionType.INDETERMINATE;

			this.decision = decision;
			this.hashCode = decision.hashCode();
			this.toString = "ExtendedDecision( decision= " + decision + ", status= null )";
		}

		@Override
		public DecisionType getDecision()
		{
			return decision;
		}

		@Override
		public Status getStatus()
		{
			return null;
		}

		@Override
		public DecisionType getExtendedIndeterminate()
		{
			return DecisionType.NOT_APPLICABLE;
		}

		/** {@inheritDoc} */
		@Override
		public int hashCode()
		{
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

			if (!(obj instanceof ExtendedDecision))
			{
				return false;
			}

			final ExtendedDecision other = (ExtendedDecision) obj;
			return this.decision == other.getDecision() && other.getExtendedIndeterminate() == null && other.getStatus() == null;
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			return toString;
		}

		@Override
		public Optional<IndeterminateEvaluationException> getCauseForIndeterminate()
		{
			return Optional.empty();
		}
	}

	/**
	 * Simple immutable Permit extended Decision (no status)
	 */
	public static final ExtendedDecision SIMPLE_PERMIT = new SimpleImmutableExtendedDecision(DecisionType.PERMIT);

	/**
	 * Simple immutable Deny Decision result (no status)
	 */
	public static final ExtendedDecision SIMPLE_DENY = new SimpleImmutableExtendedDecision(DecisionType.DENY);

	/**
	 * Simple immutable NoApplicable Decision result (no status)
	 */
	public static final ExtendedDecision SIMPLE_NOT_APPLICABLE = new SimpleImmutableExtendedDecision(DecisionType.NOT_APPLICABLE);

	/**
	 * For Indeterminate result
	 */
	private static final class IndeterminateExtendedDecision implements ExtendedDecision
	{
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

		private final Optional<IndeterminateEvaluationException> cause;

		private transient volatile int hashCode = 0;

		private transient volatile String toString = null;

		/*
		 * For indeterminate result
		 */
		private IndeterminateExtendedDecision(final DecisionType extendedIndeterminate, final IndeterminateEvaluationException cause)
		{
			/*
			 * There must be a reason for indeterminate, therefore cause != null
			 */
			assert extendedIndeterminate != null && cause != null;

			this.extIndeterminate = extendedIndeterminate;
			// Create Optional to #getCauseForIndeterminate()
			this.cause = Optional.of(cause);
		}

		/** {@inheritDoc} */
		@Override
		public int hashCode()
		{
			if (hashCode == 0)
			{
				hashCode = Objects.hash(this.extIndeterminate, this.cause.get());
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

			if (!(obj instanceof ExtendedDecision))
			{
				return false;
			}

			final ExtendedDecision other = (ExtendedDecision) obj;
			final Optional<IndeterminateEvaluationException> otherCause = other.getCauseForIndeterminate();
			if (!otherCause.isPresent())
			{
				return false;
			}

			return other.getDecision() == DecisionType.INDETERMINATE && extIndeterminate.equals(other.getExtendedIndeterminate()) && cause.get().equals(otherCause.get());
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			if (toString == null)
			{
				toString = "ExtendedDecision( decision= Indeterminate, extendedIndeterminate= " + extIndeterminate + ", status= " + cause.get().getTopLevelStatus() + " )";
			}
			return toString;
		}

		@Override
		public DecisionType getDecision()
		{
			return DecisionType.INDETERMINATE;
		}

		@Override
		public Status getStatus()
		{
			return this.cause.get().getTopLevelStatus();
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

	/**
	 * For "determinate" (permit/deny/NotApplicable) result, as opposed to Indeterminate
	 */
	private static final class ImmutableDeterminateExtendedDecision implements ExtendedDecision
	{

		private final DecisionType decision;

		private final Status status;

		private transient volatile int hashCode = 0;

		private transient volatile String toString = null;

		private ImmutableDeterminateExtendedDecision(final DecisionType decision, final Status status)
		{
			/*
			 * For cases when status == null, use one of the SIMPLE_* constants
			 */
			assert decision != null && decision != DecisionType.INDETERMINATE && status != null;

			this.decision = decision;
			this.status = status;
		}

		/** {@inheritDoc} */
		@Override
		public int hashCode()
		{
			if (hashCode == 0)
			{
				hashCode = Objects.hash(this.decision, this.status);
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

			if (!(obj instanceof ExtendedDecision))
			{
				return false;
			}

			final ExtendedDecision other = (ExtendedDecision) obj;
			return this.decision == other.getDecision() && other.getExtendedIndeterminate() == null && Objects.equals(status, other.getStatus());
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
		public Status getStatus()
		{
			return this.status;
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			if (toString == null)
			{
				toString = "ExtendedDecision( decision= " + decision + ", status= " + status + " )";
			}
			return toString;
		}

		@Override
		public Optional<IndeterminateEvaluationException> getCauseForIndeterminate()
		{
			return Optional.empty();
		}
	}

	private ExtendedDecisions()
	{
		// prevent instantiation with default constructor
	}

	/**
	 * Instantiates a Permit decision
	 * 
	 *
	 * @param status
	 *            status; even if decision is Permit/Deny, there may be a status "ok" (standard status in XACML 3.0) or internal error on attribute resolution but not resulting in Indeterminate
	 *            because of special combining algorithm ignoring such results (like deny-unless-permit) or MustBePresent="false"
	 * @return permit result, more particularly {@link #SIMPLE_PERMIT} iff {@code status  == null}.
	 */
	public static ExtendedDecision getPermit(final Status status)
	{
		if (status == null)
		{
			return SIMPLE_PERMIT;
		}

		return new ImmutableDeterminateExtendedDecision(DecisionType.PERMIT, status);
	}

	/**
	 * Instantiates a Deny decision
	 * 
	 *
	 * @param status
	 *            status; even if decision is Permit/Deny, there may be a status "ok" (standard status in XACML 3.0) or internal error on attribute resolution but not resulting in Indeterminate
	 *            because of special combining algorithm ignoring such results (like deny-unless-permit) or MustBePresent="false"
	 * @return deny result, more particularly {@link #SIMPLE_DENY} iff {@code status  == null}.
	 */
	public static ExtendedDecision getDeny(final Status status)
	{
		if (status == null)
		{
			return SIMPLE_DENY;
		}

		return new ImmutableDeterminateExtendedDecision(DecisionType.DENY, status);
	}

	/**
	 * Instantiates a NotApplicable decision
	 * 
	 *
	 * @param status
	 *            status; even if decision is Permit/Deny, there may be a status "ok" (standard status in XACML 3.0) or internal error on attribute resolution but not resulting in Indeterminate
	 *            because of special combining algorithm ignoring such results (like deny-unless-permit) or MustBePresent="false"
	 * @return deny result, more particularly {@link #SIMPLE_NOT_APPLICABLE} iff {@code status  == null}.
	 */
	public static ExtendedDecision getNotApplicable(final Status status)
	{
		if (status == null)
		{
			return SIMPLE_NOT_APPLICABLE;
		}

		return new ImmutableDeterminateExtendedDecision(DecisionType.NOT_APPLICABLE, status);
	}

	/**
	 * Instantiates a Indeterminate Decision result with a given summary error info (status) and optional stacktrace (cause
	 *
	 * @param extendedIndeterminate
	 *            (required) Extended Indeterminate value (XACML 3.0 Core, section 7.10). We use the following convention:
	 *            <ul>
	 *            <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
	 *            <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
	 *            <li>{@link DecisionType#INDETERMINATE} or null means "Indeterminate{DP}"</li>
	 *            <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means the decision is not Indeterminate, and therefore any extended Indeterminate value should be ignored</li>
	 *            </ul>
	 * @param cause
	 *            cause of the Indeterminate result
	 * @return Indeterminate result
	 * @throws IllegalArgumentException
	 *             if {@code cause ==null}
	 */
	public static ExtendedDecision newIndeterminate(final DecisionType extendedIndeterminate, final IndeterminateEvaluationException cause) throws IllegalArgumentException
	{
		Preconditions.checkNotNull(cause, "No cause provided for Indeterminate result");
		return new IndeterminateExtendedDecision(extendedIndeterminate == null ? DecisionType.INDETERMINATE : extendedIndeterminate, cause);
	}

}
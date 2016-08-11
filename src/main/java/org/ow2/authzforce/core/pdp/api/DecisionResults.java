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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

/**
 * Result of evaluation of {@link Decidable} (Policy, Rule...). This is different from the final Result in the Response by the PDP as it does not have the Attributes to be included in the final
 * Result; and Obligations/Advices are packaged together in a {@link PepActions} field.
 * 
 */
public final class DecisionResults
{
	private DecisionResults()
	{

	}

	private static final class ImmutableSimpleDecisionResult implements DecisionResult
	{
		private static final IllegalArgumentException ILLEGAL_DECISION_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Decision");

		private static final Result SIMPLE_NOT_APPLICABLE_XACML = new Result(DecisionType.NOT_APPLICABLE, null, null, null, null, null);
		private static final Result SIMPLE_PERMIT_XACML = new Result(DecisionType.PERMIT, null, null, null, null, null);
		private static final Result SIMPLE_DENY_XACML = new Result(DecisionType.DENY, null, null, null, null, null);

		private final DecisionType decision;

		private final transient int hashCode;

		private final transient String toString;

		private ImmutableSimpleDecisionResult(final DecisionType decision)
		{
			if (decision == null)
			{
				throw ILLEGAL_DECISION_ARGUMENT_EXCEPTION;
			}

			this.decision = decision;
			this.hashCode = this.decision.hashCode();
			this.toString = "Result[decision=" + decision + ", others=null ]";
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

			if (!(obj instanceof ImmutableSimpleDecisionResult))
			{
				return false;
			}

			final ImmutableSimpleDecisionResult other = (ImmutableSimpleDecisionResult) obj;
			return this.decision == other.getDecision();
		}

		/**
		 * {@inheritDoc}
		 *
		 * Get XACML Decision
		 */
		@Override
		public DecisionType getDecision()
		{
			return this.decision;
		}

		/**
		 * {@inheritDoc}
		 *
		 * Get PEP actions (Obligations/Advices)
		 */
		@Override
		public PepActions getPepActions()
		{
			return null;
		}

		/**
		 * {@inheritDoc}
		 *
		 * Status code/message/detail
		 */
		@Override
		public Status getStatus()
		{
			return null;
		}

		/**
		 * {@inheritDoc}
		 *
		 * Get identifiers of policies found applicable for the decision request
		 */
		@Override
		public List<JAXBElement<IdReferenceType>> getApplicablePolicyIdList()
		{
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public DecisionType getExtendedIndeterminate()
		{
			return DecisionType.NOT_APPLICABLE;
		}

		/** {@inheritDoc} */
		@Override
		public String toString()
		{
			return toString;
		}

		@Override
		public Set<AttributeGUID> getUsedNamedAttributes()
		{
			return null;
		}

		@Override
		public Set<AttributeSelectorId> getUsedExtraAttributeContents()
		{
			return null;
		}

		@Override
		public Result toXACMLResult(final List<Attributes> returnedAttributes)
		{
			if (this == SIMPLE_NOT_APPLICABLE)
			{
				return SIMPLE_NOT_APPLICABLE_XACML;
			}

			if (this == SIMPLE_PERMIT)
			{
				return SIMPLE_PERMIT_XACML;
			}

			if (this == SIMPLE_DENY)
			{
				return SIMPLE_DENY_XACML;
			}

			return new Result(this.decision, null, null, null, null, null);
		}
	}

	/**
	 * NotApplicable decision result
	 */
	public static final DecisionResult SIMPLE_NOT_APPLICABLE = new ImmutableSimpleDecisionResult(DecisionType.NOT_APPLICABLE);

	/**
	 * Deny result with no obligation/advice/Included attribute/policy identifiers. Deny decision and nothing else.
	 */
	public static final DecisionResult SIMPLE_DENY = new ImmutableSimpleDecisionResult(DecisionType.DENY);

	/**
	 * Permit result with no obligation/advice/Included attribute/policy identifiers. Permit decision and nothing else.
	 */
	public static final DecisionResult SIMPLE_PERMIT = new ImmutableSimpleDecisionResult(DecisionType.PERMIT);

}

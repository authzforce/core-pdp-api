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

import com.google.common.collect.ImmutableList;

/**
 * Pre-made constant decision evaluation results corresponding to XACML NotApplicable, Permit and Deny with only the Decision element set (no obligation/advice, no returned attribute, etc.). Use this
 * to return a {@link DecisionResult} if no other element/attribute is needed in the result besides the Decision.
 */
public final class PdpDecisionResults
{
	private PdpDecisionResults()
	{
		// prevent external instantiation by making default constructor private
	}

	private static final class SimpleImmutableDecisionResult implements PdpDecisionResult
	{
		private static final IllegalArgumentException INVALID_DECISION_ARGUMENT_EXCEPTION = new IllegalArgumentException(
				"Not a simple decision result. Expected: NotApplicable, Permit, Deny (without any field other than the Decision)");

		private static final Result SIMPLE_NOT_APPLICABLE_XACML = new Result(DecisionType.NOT_APPLICABLE, null, null, null, null, null);
		private static final Result SIMPLE_PERMIT_XACML = new Result(DecisionType.PERMIT, null, null, null, null, null);
		private static final Result SIMPLE_DENY_XACML = new Result(DecisionType.DENY, null, null, null, null, null);

		private final DecisionType decision;

		private final transient int hashCode;

		private final transient String toString;

		private SimpleImmutableDecisionResult(final DecisionType decision)
		{
			assert decision != null;

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

			if (!(obj instanceof PdpDecisionResult))
			{
				return false;
			}

			final PdpDecisionResult other = (PdpDecisionResult) obj;
			return this.decision == other.getDecision() && other.getStatus() == null && (other.getPepActions() == null || other.getPepActions().isEmpty())
					&& (other.getApplicablePolicies() == null || other.getApplicablePolicies().isEmpty());
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
		public ImmutablePepActions getPepActions()
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
		public ImmutableList<JAXBElement<IdReferenceType>> getApplicablePolicies()
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

			throw INVALID_DECISION_ARGUMENT_EXCEPTION;
		}
	}

	/**
	 * NotApplicable decision result where only the Decision element is set, no Status, no obligation/advice, etc.
	 */
	public static final PdpDecisionResult SIMPLE_NOT_APPLICABLE = new SimpleImmutableDecisionResult(DecisionType.NOT_APPLICABLE);

	/**
	 * Deny result with no obligation/advice/Included attribute/policy identifiers. Deny decision and nothing else.
	 */
	public static final PdpDecisionResult SIMPLE_DENY = new SimpleImmutableDecisionResult(DecisionType.DENY);

	/**
	 * Permit result with no obligation/advice/Included attribute/policy identifiers. Permit decision and nothing else.
	 */
	public static final PdpDecisionResult SIMPLE_PERMIT = new SimpleImmutableDecisionResult(DecisionType.PERMIT);

}

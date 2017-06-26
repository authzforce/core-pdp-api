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
package org.ow2.authzforce.core.pdp.api;

import java.util.List;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdvice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligations;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIdentifierList;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

/**
 * Immutable implementation of {@link PdpDecisionResult} to be used as result from {@link PDPEngine#evaluate(PdpDecisionRequest)} evaluation.
 *
 */
public final class ImmutablePdpDecisionResult implements PdpDecisionResult
{

	private static final IllegalArgumentException NULL_DECISION_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Decision");

	private final DecisionType decision;

	private final Status status;

	private final ImmutablePepActions pepActions;

	/**
	 * Extended Indeterminate value, only in case {@link #getDecision()} returns {@value DecisionType#INDETERMINATE}, else it should be ignored, as defined in section 7.10 of XACML 3.0 core:
	 * <i>potential effect value which could have occurred if there would not have been an error causing the “Indeterminate”</i>. We use the following convention:
	 * <ul>
	 * <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
	 * <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
	 * <li>{@link DecisionType#INDETERMINATE} means "Indeterminate{DP}"</li>
	 * <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means the decision is not Indeterminate, and therefore any extended Indeterminate value should be ignored</li>
	 * </ul>
	 * 
	 */
	private final DecisionType extIndeterminate;

	private final ImmutableList<JAXBElement<IdReferenceType>> applicablePolicyIdList;

	private volatile int hashCode = 0;

	private volatile String toString = null;

	/**
	 * Creates instance
	 * 
	 * @param decision
	 *            decision (non-null)
	 * @param extendedIndeterminate
	 *            Extended Indeterminate decision, non-null
	 * @param status
	 *            Status (e.g. error info if Indeterminate) of the Result
	 * @param pepActions
	 *            PEP actions (obligations/advice)
	 * @param applicablePolicyIdList
	 *            (identifiers of applicable policies)
	 * @throws IllegalArgumentException
	 *             if {@code decision == null}
	 */
	public ImmutablePdpDecisionResult(final DecisionType decision, final DecisionType extendedIndeterminate, final Status status, final ImmutablePepActions pepActions,
			final ImmutableList<JAXBElement<IdReferenceType>> applicablePolicyIdList) throws IllegalArgumentException
	{
		if (decision == null)
		{
			throw NULL_DECISION_ARGUMENT_EXCEPTION;
		}

		this.decision = decision;
		this.status = status;
		this.pepActions = pepActions;
		this.extIndeterminate = extendedIndeterminate == null ? DecisionType.NOT_APPLICABLE : extendedIndeterminate;
		this.applicablePolicyIdList = decision == DecisionType.NOT_APPLICABLE ? null : applicablePolicyIdList == null ? ImmutableList.<JAXBElement<IdReferenceType>> of() : applicablePolicyIdList;
	}

	/**
	 * Clones the input {@code decisionResult}
	 * 
	 * @param decisionResult
	 *            base decision result
	 */
	public ImmutablePdpDecisionResult(final DecisionResult decisionResult)
	{
		this(decisionResult.getDecision(), decisionResult.getExtendedIndeterminate(), decisionResult.getStatus(), decisionResult.getPepActions(), decisionResult.getApplicablePolicies());
	}

	/**
	 * Creates Indeterminate decision result
	 * 
	 * @param status
	 *            Status field (error info) of the Result
	 */
	public ImmutablePdpDecisionResult(final Status status)
	{
		this(DecisionType.INDETERMINATE, DecisionType.INDETERMINATE, status, null, null);
	}

	@Override
	public ImmutableList<JAXBElement<IdReferenceType>> getApplicablePolicies()
	{
		return this.applicablePolicyIdList;
	}

	@Override
	public DecisionType getDecision()
	{
		return this.decision;
	}

	@Override
	public DecisionType getExtendedIndeterminate()
	{
		return this.extIndeterminate;
	}

	@Override
	public ImmutablePepActions getPepActions()
	{
		return this.pepActions;
	}

	@Override
	public Status getStatus()
	{
		return this.status;
	}

	@Override
	public Result toXACMLResult(final List<Attributes> returnedAttributes)
	{
		final List<Obligation> obligationList;
		final List<Advice> adviceList;
		if (pepActions == null)
		{
			obligationList = null;
			adviceList = null;
		}
		else
		{
			obligationList = this.pepActions.getObligatory();
			adviceList = this.pepActions.getAdvisory();
		}

		return new Result(this.decision, this.status, obligationList == null || obligationList.isEmpty() ? null : new Obligations(obligationList), adviceList == null || adviceList.isEmpty() ? null
				: new AssociatedAdvice(adviceList), returnedAttributes, applicablePolicyIdList == null || applicablePolicyIdList.isEmpty() ? null : new PolicyIdentifierList(applicablePolicyIdList));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hashCode(this.decision, this.extIndeterminate, this.status, this.applicablePolicyIdList, this.pepActions);
		}

		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof ImmutablePdpDecisionResult))
		{
			return false;
		}

		final ImmutablePdpDecisionResult other = (ImmutablePdpDecisionResult) obj;

		if (decision != other.decision)
		{
			return false;
		}

		if (extIndeterminate != other.extIndeterminate)
		{
			return false;
		}

		if (status == null)
		{
			if (other.status != null)
			{
				return false;
			}
		}
		else if (!status.equals(other.status))
		{
			return false;
		}

		if (applicablePolicyIdList == null)
		{
			if (other.applicablePolicyIdList != null)
			{
				return false;
			}
		}
		else if (!applicablePolicyIdList.equals(other.applicablePolicyIdList))
		{
			return false;
		}

		if (pepActions == null)
		{
			if (other.pepActions != null)
			{
				return false;
			}
		}
		else if (!pepActions.equals(other.pepActions))
		{
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if (toString == null)
		{
			toString = "ImmutablePdpDecisionResult [decision=" + decision + ", status=" + status + ", pepActions=" + pepActions + ", extIndeterminate=" + extIndeterminate
					+ ", applicablePolicyIdList=" + applicablePolicyIdList + "]";
		}

		return toString;
	}

}
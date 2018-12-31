/**
 * Copyright 2012-2018 THALES.
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

import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * PEP Action, i.e. XACML Obligation/Advice
 *
 */
public final class PepAction
{

	private final String actionId;
	private final boolean isMandatory;
	private final ImmutableList<PepActionAttributeAssignment<?>> attAssignments;

	private transient volatile int hashCode = 0;
	private transient volatile String toString = null;

	/**
	 * Constructor
	 * 
	 * @param actionId
	 *            action ID (XACML ObligationId/AdviceId)
	 * @param isMandatory
	 *            true iff the action is mandatory (XACML Obligation, else Advice)
	 * @param attributeAssignments
	 *            action arguments (parameter assignments)
	 */
	public PepAction(String actionId, boolean isMandatory, ImmutableList<PepActionAttributeAssignment<?>> attributeAssignments)
	{
		Preconditions.checkArgument(actionId != null, "PEP action (obligation/advice) ID == null (undefined)");
		Preconditions.checkArgument(attributeAssignments != null, "PEP action (obligation/advice) attribute assignments == null (undefined)");
		this.actionId = actionId;
		this.isMandatory = isMandatory;
		this.attAssignments = attributeAssignments;
	}

	/**
	 * @return the action ID (XACML ObligationId / AdviceId)
	 */
	public String getId()
	{
		return actionId;
	}

	/**
	 * True iff it is an obligation (mandatory action), else an advice
	 * 
	 * @return the isMandatory
	 */
	public boolean isMandatory()
	{
		return isMandatory;
	}

	/**
	 * @return the action's arguments (attribute assignments)
	 */
	public ImmutableList<PepActionAttributeAssignment<?>> getAttributeAssignments()
	{
		return attAssignments;
	}

	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(isMandatory, actionId, attAssignments);
		}

		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof PepAction))
		{
			return false;
		}

		final PepAction other = (PepAction) obj;
		return this.isMandatory == other.isMandatory && this.actionId.equals(other.actionId) && this.attAssignments.equals(other.attAssignments);
	}

	@Override
	public String toString()
	{
		if (toString == null)
		{
			toString = "PepAction [actionId=" + actionId + ", isMandatory=" + isMandatory + ", attAssignments=" + attAssignments + "]";
		}

		return toString;
	}

}

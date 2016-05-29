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

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

/**
 * Result of evaluation of {@link Decidable} (Policy, Rule...). This is different from the final Result in the Response by the PDP as it does not have the Attributes to be included in the final
 * Result; and Obligations/Advices are packaged together in a {@link PepActions} field.
 * 
 */
public interface DecisionResult
{

	/**
	 * Get identifiers of policies found applicable for the decision request
	 * 
	 * @return identifiers of policies found applicable for the decision request
	 */
	List<JAXBElement<IdReferenceType>> getApplicablePolicyIdList();

	/**
	 * Get XACML Decision
	 * 
	 * @return decision
	 */
	DecisionType getDecision();

	/**
	 * Get PEP actions (Obligations/Advices)
	 * 
	 * @return PEP actions
	 */
	PepActions getPepActions();

	/**
	 * Status code/message/detail
	 * 
	 * @return status
	 */
	Status getStatus();

	/**
	 * Merge extra PEP actions and/or matched policy identifiers. Used when combining results from child Rules of Policy or child Policies of PolicySet
	 * 
	 * @param newPepActions
	 *            new PEP actions
	 * @param newMatchedPolicyIdList
	 *            new matched policy identifiers
	 */
	void merge(PepActions newPepActions, List<JAXBElement<IdReferenceType>> newMatchedPolicyIdList);

	/**
	 * Provides the Extended Indeterminate value, only in case {@link #getDecision()} returns {@value DecisionType#INDETERMINATE}, else it should be ignored, as defined in section 7.10 of XACML 3.0
	 * core: <i>potential effect value which could have occurred if there would not have been an error causing the “Indeterminate”</i>. We use the following convention:
	 * <ul>
	 * <li>{@link DecisionType#DENY} means "Indeterminate{D}"</li>
	 * <li>{@link DecisionType#PERMIT} means "Indeterminate{P}"</li>
	 * <li>{@link DecisionType#INDETERMINATE} means "Indeterminate{DP}"</li>
	 * <li>{@link DecisionType#NOT_APPLICABLE} is the default value and means the decision is not Indeterminate, and therefore any extended Indeterminate value should be ignored</li>
	 * </ul>
	 * 
	 * @return extended Indeterminate value
	 * 
	 */
	DecisionType getExtendedIndeterminate();

}

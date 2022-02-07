/*
 * Copyright 2012-2022 THALES.
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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import java.util.Optional;

/**
 * Extended decision, i.e. XACML Decision with optional ExtendedIndeterminate and Status if Indeterminate. This is mostly used as return type of combining algorithms.
 * 
 */
public interface ExtendedDecision
{

	/**
	 * Get XACML Decision
	 * 
	 * @return decision
	 */
	DecisionType getDecision();

	/**
	 * Status code/message/detail
	 * 
	 * @return status
	 */
	Optional<ImmutableXacmlStatus> getStatus();

	/**
	 * Provides the Extended Indeterminate value, iff {@code #getDecision() == DecisionType.INDETERMINATE}, else it should be ignored, as defined in section 7.10 of XACML 3.0 core: <i>potential effect
	 * value which could have occurred if there would not have been an error causing the “Indeterminate”</i>. We use the following convention:
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

	/**
	 * Gets the optional error stacktrace detailing the cause for the Indeterminate decision. It is always present iff {@code #getDecision() == DecisionType.INDETERMINATE}
	 * 
	 * @return error stacktrace associated to Indeterminate
	 */
	Optional<IndeterminateEvaluationException> getCauseForIndeterminate();

}

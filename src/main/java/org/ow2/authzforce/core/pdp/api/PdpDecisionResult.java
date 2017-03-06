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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

import java.util.List;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

import org.ow2.authzforce.core.pdp.api.value.Datatype;

/**
 * PDP's final evaluation result (for each Individual Decision Request as defined in XACML Multiple Decision Profile) that may be converted to a XACML {@link Result} for inclusion in the final
 * {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.Response}. Compared to intermediate Policy/Rule evaluation results, it has extra fields such as the optional list of identifiers of all policies
 * found applicable during the evaluation.
 * 
 */
public interface PdpDecisionResult extends DecisionResult
{

	/**
	 * Get identifiers of the named attributes actually used during evaluation, i.e. for which {@link EvaluationContext#getAttributeDesignatorResult(AttributeGUID, Datatype)} was called. This may be
	 * useful for the caller to know on which specific request parts the decision relied upon.
	 * 
	 * @return the list of used named attributes
	 */
	Set<AttributeGUID> getUsedNamedAttributes();

	/**
	 * Get identifiers of the Attributes/Content parts actually used during evaluation, i.e. for which {@link EvaluationContext#getAttributeSelectorResult(AttributeSelectorId, Datatype)} was called.
	 * This may be useful for the caller to know on which specific request parts the decision relied upon.
	 * 
	 * @return the list of used Attributes/Content(s)
	 */
	Set<AttributeSelectorId> getUsedExtraAttributeContents();

	/**
	 * Convert this to XACML Result. Note that an XACML Result has less information than this.
	 * 
	 * @param returnedAttributes
	 *            XACML Request attributes with IncludeInResult=true
	 * 
	 * @return XACML Result
	 */
	Result toXACMLResult(List<Attributes> returnedAttributes);

}

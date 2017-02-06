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

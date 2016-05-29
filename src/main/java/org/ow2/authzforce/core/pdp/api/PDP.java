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
package org.ow2.authzforce.core.pdp.api;

import java.util.List;
import java.util.Map;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

/**
 * This is the interface for the XACML PDP engines, providing the starting point for request evaluation.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public interface PDP
{

	/**
	 * Evaluates a XML/JAXB-based XACML decision request
	 * <p>
	 * Note that if the request is somehow invalid (it was missing a required attribute, it was using an unsupported scope, etc), then the result will be a
	 * decision of INDETERMINATE.
	 * 
	 * @param request
	 *            the request to evaluate
	 * @param namespaceURIsByPrefix
	 *            namespace prefix-URI mappings (e.g. "... xmlns:prefix=uri") in the original XACML Request bound to {@code req}, used as part of the context
	 *            for XPath evaluation
	 * @return the response to the request
	 */
	Response evaluate(Request request, Map<String, String> namespaceURIsByPrefix);

	/**
	 * Equivalent to {@link #evaluate(Request, Map)} with second parameter set to null.
	 * 
	 * @param request
	 *            the request to evaluate
	 * @return the response to the request
	 */
	Response evaluate(Request request);

	/**
	 * Generic API (serialization-format-agnostic) for evaluating decision requests according to XACML specification. To be used instead of
	 * {@link #evaluate(Request)} or {@link #evaluate(Request, Map)} when calling the PDP Java API directly (native Java call, e.g. embedded PDP), or when the
	 * original request format is NOT XML.
	 * <p>
	 * Note that AuthzForce PDP core implementation {@link #evaluate(Request, Map)} calls this method internally to get the final result. Therefore, for better
	 * performances (from a caller's point of view), you should call this method directly whenever possible.
	 * 
	 * @param individualDecisionRequests
	 *            one or more Individual Decision Requests, as defined in the XACML Multiple Decision Profile (also mentioned in the Hierarchical Resource
	 *            Profile)
	 * @return decision results, one per item of {@code individualDecisionRequests}
	 */
	List<Result> evaluate(List<? extends IndividualDecisionRequest> individualDecisionRequests);

}

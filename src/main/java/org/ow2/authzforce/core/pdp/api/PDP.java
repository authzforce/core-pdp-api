/**
 *
 * Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistribution of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in the design, construction, operation or maintenance of any nuclear facility.
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

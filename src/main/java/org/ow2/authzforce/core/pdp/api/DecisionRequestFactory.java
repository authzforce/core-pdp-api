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

import java.util.Map;

import net.sf.saxon.s9api.XdmNode;

import org.ow2.authzforce.core.pdp.api.value.AttributeBag;

/**
 * {@link DecisionRequest} factory
 * 
 * @param <R>
 *            type of instance created by this factory
 *
 */
public interface DecisionRequestFactory<R extends DecisionRequest>
{
	/**
	 * Create instance of PDP decision request
	 * 
	 * @param namedAttributes
	 *            named Attributes (no extra Content element)
	 * @param contentNodesByCategory
	 *            extra XML Content elements by attribute Category
	 * @param returnApplicablePolicies
	 *            return list of applicable policy identifiers; equivalent of XACML Request's ReturnPolicyIdList flag
	 * 
	 * @return new instance
	 */
	R getInstance(final Map<AttributeFqn, AttributeBag<?>> namedAttributes, final Map<String, XdmNode> contentNodesByCategory, final boolean returnApplicablePolicies);

}

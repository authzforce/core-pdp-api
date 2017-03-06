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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

/**
 * Individual Decision Request, i.e. native Java equivalent of XACML Request that corresponds to one XACML Result element
 *
 */
public interface IndividualDecisionRequest extends PdpDecisionRequest
{

	/**
	 * Get Attributes elements containing only child Attribute elements with IncludeInResult=true
	 * 
	 * @return list of Attributes elements to include in final Result; null if none
	 */
	List<Attributes> getReturnedAttributes();

}
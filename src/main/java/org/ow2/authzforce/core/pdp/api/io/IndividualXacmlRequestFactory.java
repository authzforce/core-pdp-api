/**
 * Copyright 2012-2019 THALES.
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
package org.ow2.authzforce.core.pdp.api.io;

import org.ow2.authzforce.core.pdp.api.DecisionRequest;
import org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.ImmutableDecisionRequest;

import com.google.common.collect.ImmutableList;

/**
 * Individual {@link DecisionRequest} builder used in the context of Multiple Decision Request processing for specific type of I/O format, e.g. XACML/XML, XACML/JSON.... Used especially for
 * {@link DecisionRequestPreprocessor} implementations supporting the Multiple Decision Profile, in particular {@link MultipleXacmlRequestPreprocHelper}.
 * 
 * @param <R>
 *            type of I/O-dependent Individual {@link DecisionRequest} built by this builder in the context of Multiple Decision Request, e.g. XACML/XML, XACML/JSON...
 * @param <INPUT_ATTRIBUTE_CATEGORY>
 *            type of input attribute category in original I/O format, e.g. JAXB Attributes class for XACML/XML input, or JSON object for XACML/JSON input.
 */
public interface IndividualXacmlRequestFactory<R extends DecisionRequest, INPUT_ATTRIBUTE_CATEGORY>
{

	/**
	 * Creates immutable decision request suitable for the targeted I/O format
	 * 
	 * @param pdpEngineIndividualRequest
	 *            I/O-agnostic individual decision request for natively supported by the core PDP engine
	 * @param inputAttributeCategory
	 *            original I/O-specific input attribute category
	 * @return I/O-specific individual decision request
	 */
	R newInstance(ImmutableDecisionRequest pdpEngineIndividualRequest, ImmutableList<INPUT_ATTRIBUTE_CATEGORY> inputAttributeCategory);
}

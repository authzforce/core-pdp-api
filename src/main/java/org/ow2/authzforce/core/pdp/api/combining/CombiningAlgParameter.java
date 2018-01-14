/**
 * Copyright 2012-2018 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.api.combining;

import java.util.List;

import org.ow2.authzforce.core.pdp.api.Decidable;

/**
 * Represents a set of CombinerParameters to a combining algorithm that may or may not be associated with a policy/rule
 * 
 * @param <T>
 *            Type of combined element (Policy, Rule...) with which the CombinerParameters are associated
 */
public interface CombiningAlgParameter<T extends Decidable>
{

	/**
	 * Returns the combined element. If null, it means, this CombinerElement (i.e. all its CombinerParameters) is not associated with a particular rule
	 * 
	 * @return the combined element
	 */
	T getCombinedElement();

	/**
	 * Returns the <code>CombinerParameterEvaluator</code>s associated with this element.
	 * 
	 * @return a <code>List</code> of <code>CombinerParameterEvaluator</code>s
	 */
	List<CombinerParameterEvaluator> getParameters();
}

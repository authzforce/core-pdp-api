/**
 * Copyright (C) 2012-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.api;

import java.util.List;

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

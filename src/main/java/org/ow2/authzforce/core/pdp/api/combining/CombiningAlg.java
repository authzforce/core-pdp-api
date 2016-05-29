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
package org.ow2.authzforce.core.pdp.api.combining;

import java.util.List;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.PdpExtension;

/**
 * Combining algorithm. In combining policies, obligations and advice must be handled correctly. Specifically, no obligation/advice may be included in the
 * <code>Result</code> that doesn't match the permit/deny decision being returned. So, if INDETERMINATE or NOT_APPLICABLE is the returned decision, no
 * obligations/advice may be included in the result. If the decision of the combining algorithm is PERMIT or DENY, then obligations/advice with a matching
 * fulfillOn/AppliesTo effect are also included in the result.
 * 
 * @param <T>
 *            type of combined element (Policy, Rule...)
 */
public interface CombiningAlg<T extends Decidable> extends PdpExtension
{

	/**
	 * Combining algorithm evaluator
	 *
	 */
	interface Evaluator
	{
		/**
		 * Runs the combining algorithm in a specific evaluation context
		 * 
		 * @param context
		 *            the request evaluation context
		 * 
		 * @return combined result
		 */
		DecisionResult eval(EvaluationContext context);
	}

	/**
	 * Get to know whether this is a policy/policySet or rule-combining algorithm
	 * 
	 * @return the combinedElementType
	 */
	Class<T> getCombinedElementType();

	/**
	 * Creates instance of algorithm. To be implemented by algorithm implementations.
	 * 
	 * @param params
	 *            list of combining algorithm parameters that may be associated with a particular child element
	 * @param combinedElements
	 *            combined child elements
	 * 
	 * @return an instance of algorithm evaluator
	 * @throws UnsupportedOperationException
	 *             if this is a legacy algorithm and legacy support is disabled
	 * @throws IllegalArgumentException
	 *             if {@code params} are invalid for this algorithm
	 */
	Evaluator getInstance(List<CombiningAlgParameter<? extends T>> params, List<? extends T> combinedElements) throws UnsupportedOperationException,
			IllegalArgumentException;
}

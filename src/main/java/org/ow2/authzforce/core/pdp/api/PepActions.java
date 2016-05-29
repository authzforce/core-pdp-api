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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;

/**
 * PEP actions (obligations/advice)
 * 
 */
public interface PepActions
{
	/**
	 * Get the internal obligation list
	 * 
	 * @return obligations; empty if no obligation (always non-null)
	 */
	List<Obligation> getObligations();

	/**
	 * Get the internal advice list
	 * 
	 * @return advice; empty if no obligation (always non-null)
	 */
	List<Advice> getAdvices();

	/**
	 * Merge extra PEP actions. Used when combining results from child Rules of Policy or child Policies of PolicySet
	 * 
	 * @param newObligations
	 *            new obligation list
	 * @param newAdvices
	 *            new advice list
	 * 
	 */
	void merge(List<Obligation> newObligations, List<Advice> newAdvices);

	/**
	 * Merge extra PEP actions. Used when combining results from child Rules of Policy or child Policies of PolicySet
	 * 
	 * @param pepActions
	 *            PEP actions
	 */
	void merge(PepActions pepActions);
}

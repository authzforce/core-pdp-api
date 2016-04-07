/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

/**
 * Statically-defined policy evaluator interface, "Policy" referring to any XACML Policy* element:
 * Policy(Set), Policy(Set)IdReference. "Static" means here that the whole policy definition is fixed once and for all, i.e. it does not depend on the evaluation context.
 * 
 */
public interface StaticPolicyEvaluator extends PolicyEvaluator
{

	/**
	 * Get (static/context-independent) extra metadata of the evaluated policy. Always return the same result.
	 * 
	 * @return extra metadata of the evaluated policy.
	 */
	ExtraPolicyMetadata getExtraPolicyMetadata();

}

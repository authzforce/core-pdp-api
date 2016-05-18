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
import java.util.Map;

import org.ow2.authzforce.core.pdp.api.value.Bag;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

/**
 * Individual Decision Request, i.e. native Java equivalent of XACML Request that corresponds to one XACML Result element
 *
 */
public interface IndividualDecisionRequest
{

	/**
	 * Get named attributes by name
	 * 
	 * @return map of attribute name-value pairs, null if none (but {@link #getExtraContentsByCategory()} result may not be empty)
	 */
	Map<AttributeGUID, Bag<?>> getNamedAttributes();

	/**
	 * Get Attributes elements containing only child Attribute elements with IncludeInResult=true
	 * 
	 * @return list of Attributes elements to include in final Result; null if none
	 */
	List<Attributes> getReturnedAttributes();

	/**
	 * Get Attributes/Contents (parsed into XDM data model for XPath evaluation) by attribute category
	 * 
	 * @return extra XML Contents by category; null if none
	 */
	Map<String, XdmNode> getExtraContentsByCategory();

	/**
	 * Get ReturnPolicyIdList flag
	 * 
	 * @return true iff original XACML Request's ReturnPolicyIdList == true
	 */
	boolean isApplicablePolicyIdentifiersReturned();

}
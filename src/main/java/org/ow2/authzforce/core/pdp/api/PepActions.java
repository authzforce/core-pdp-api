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

import com.google.common.collect.ImmutableList;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;

/**
 * PEP actions (obligations/advice)
 * 
 */
public interface PepActions
{
	/**
	 * PEP action (obligation/advice) factory
	 *
	 * @param <JAXB_T>
	 *            JAXB-annotated PEP action type
	 * @version $Id: $
	 */
	interface Factory<JAXB_T>
	{
		/**
		 * Creates instance of PEP action (obligation/advice)
		 *
		 * @param attributeAssignments
		 *            XML/JAXB AttributeAssignments in the PEP action
		 * @param actionId
		 *            action ID (ObligationId, AdviceId)
		 * @return PEP action
		 */
		JAXB_T getInstance(List<AttributeAssignment> attributeAssignments, String actionId);

		/**
		 * Get name of PEP Action element in XACML model, e.g. 'Obligation'
		 *
		 * @return action element name
		 */
		String getActionXmlElementName();
	}

	/**
	 * Get an immutable list of the obligations
	 * 
	 * @return obligations; empty if no obligation (always non-null)
	 */
	ImmutableList<Obligation> getObligatory();

	/**
	 * Get an immutable list of the advice elements
	 * 
	 * @return advice; empty if no obligation (always non-null)
	 */
	ImmutableList<Advice> getAdvisory();

	/**
	 * Is there any obligation/advice?
	 * 
	 * @return true iff there is none
	 */
	boolean isEmpty();
}

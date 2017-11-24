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

import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

/**
 * "Designated" Attribute Provider, i.e. attribute provider supporting/assigned to designated attribute
 */
public interface DesignatedAttributeProvider extends AttributeProvider
{

	/**
	 * Returns a non-null non-empty <code>Set</code> of <code>AttributeDesignator</code>s provided/supported by this module.
	 * 
	 * @return a non-null non-empty <code>Set</code> of supported <code>AttributeDesignatorType</code>s
	 */
	Set<AttributeDesignatorType> getProvidedAttributes();

}

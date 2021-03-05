/*
 * Copyright 2012-2021 THALES.
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
 * "Named" Attribute Provider, i.e. providing "named attribute(s)" as defined in §7.3 of XACML 3.0 specification:
 * <p>
 * <i>A named attribute is the term used for the criteria that the specific attribute designators use to refer to particular attributes in the <Attributes> elements of the request context.</i>
 * </p>
 * 
 */
public interface NamedAttributeProvider extends AttributeProvider
{

	/**
	 * Returns a non-null non-empty <code>Set</code> of <code>AttributeDesignator</code>s provided/supported by this module.
	 * 
	 * @return a non-null non-empty <code>Set</code> of supported <code>AttributeDesignatorType</code>s
	 */
	Set<AttributeDesignatorType> getProvidedAttributes();

}

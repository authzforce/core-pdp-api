/*
 * Copyright 2012-2022 THALES.
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

/**
 * Environment properties set by PDP configuration parsers (before or during PDP instantiation) for later use by PDP extensions.
 *
 */
public interface EnvironmentProperties
{

	/**
	 * Replaces placeholders in the form of ${PROPERTY_NAME} with the corresponding property value. Implementations must support the enum constant of
	 * {@link EnvironmentPropertyName} for which {@link EnvironmentPropertyName#name()} is used as PROPERTY_NAME for replacement.
	 * 
	 * @param input
	 *            string possibly containing property placeholders ${...}
	 * @return string with all properties replaced
	 */
	String replacePlaceholders(String input);

}
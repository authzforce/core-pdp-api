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

/**
 * Enumeration of property names (or so-called global variables) usable in PDP configuration strings in form of ${PROPERTY_NAME}, set by the PDP configuration
 * parser. PDP modules can then replace them during instantiation using {@link EnvironmentProperties#replacePlaceholders(String)} when a EnvironmentProperties
 * object is provided.
 *
 */
public enum EnvironmentPropertyName
{
	/**
	 * PDP configuration file's parent directory if the PDP configuration is loaded from a file. This property can be used in file paths to resolve paths
	 * relative to this parent directory.
	 */
	PARENT_DIR
}
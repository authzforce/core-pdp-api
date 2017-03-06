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
package org.ow2.authzforce.core.pdp.api.value;

import javax.xml.datatype.DatatypeFactory;

/**
 * The base type for all atomic/non-bag values , this abstract class represents a value for a given datatype. All the standard primitive datatypes defined in the XACML specification extend this, as
 * well as Functions ("special" datatype because they can be used as parameter of higher-order functions). If you want to provide a new datatype, extend {@link DatatypeFactory} to provide a factory
 * for it.
 * 
 */
public interface AtomicValue extends Value
{
	// marker interface
}

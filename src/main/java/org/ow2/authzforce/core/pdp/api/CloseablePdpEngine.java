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

import java.io.Closeable;

/**
 * XACML PDP that implements {@link Closeable} because it may depend on various components that hold resources such as network resources and caches to get: the root policy or policies referenced by
 * the root policy; or to get attributes used in the policies from remote sources when not provided in the Request; or to get cached decisions for requests already evaluated in the past, etc.
 * Therefore, you are required to call {@link #close()} when you no longer need an instance - especially before replacing with a new instance - in order to make sure these resources are released
 * properly by each underlying module (e.g. invalidate the attribute caches and/or network resources).
 * 
 */
public interface CloseablePdpEngine extends PdpEngine, Closeable
{
	// marker interface
}

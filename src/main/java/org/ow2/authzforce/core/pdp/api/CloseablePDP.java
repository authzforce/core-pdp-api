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

import java.io.Closeable;

/**
 * XACML PDP that implements {@link Closeable} because it may depend on various components that hold resources such as network resources and caches to get: the root policy or policies referenced by
 * the root policy; or to get attributes used in the policies from remote sources when not provided in the Request; or to get cached decisions for requests already evaluated in the past, etc.
 * Therefore, you are required to call {@link #close()} when you no longer need an instance - especially before replacing with a new instance - in order to make sure these resources are released
 * properly by each underlying module (e.g. invalidate the attribute caches and/or network resources).
 * 
 * @param <INDIVIDUAL_DECISION_REQ_T>
 *            PDP implementation-specific type of Individual Decision Request
 * 
 */
public interface CloseablePDP<INDIVIDUAL_DECISION_REQ_T extends IndividualPdpDecisionRequest> extends PDPEngine<INDIVIDUAL_DECISION_REQ_T>, Closeable
{
	// marker interface
}

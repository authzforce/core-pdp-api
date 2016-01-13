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
package org.ow2.authzforce.core.pdp.api;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

import org.ow2.authzforce.xmlns.pdp.ext.AbstractDecisionCache;

/**
 * Authorization (XACML) decision result cache. Implements {@link Closeable} because a cache may use resources external to the JVM such as a disk or connection
 * to a remote server for persistence, replication, clustering, etc. Therefore, these resources must be released by calling {@link #close()} when it is no
 * longer needed.
 * 
 */
public interface DecisionCache extends Closeable
{
	/**
	 * Factory for creating instance of DecisionCache extension
	 * 
	 * @param <CONF_T>
	 *            type of extension configuration (initialization parameters)
	 */
	abstract class Factory<CONF_T extends AbstractDecisionCache> extends JaxbBoundPdpExtension<CONF_T>
	{

		/**
		 * Instantiates decision cache extension
		 * 
		 * @param conf
		 *            extension parameters
		 * @return instance of extension
		 */
		public abstract DecisionCache getInstance(CONF_T conf);
	}

	/**
	 * Gets the decision result(s) from the cache for the given decision request(s). The ability to get multiple cached results at once allows the Cache
	 * implementation to optimize the retrieval by requesting all in the same request, e.g. if the cache is in a remote storage/server.
	 * 
	 * @param individualDecisionRequests
	 *            decision request(s)
	 * @return a map where each entry key is a request from {@code requests}, and the value is the corresponding result from cache, or null if no such result
	 *         found in cache. Each request in {@code requests} but be a key in the Map returned, and the Map size must be equal to {@code requests.size()}.
	 */
	Map<IndividualDecisionRequest, Result> getAll(List<? extends IndividualDecisionRequest> individualDecisionRequests);

	/**
	 * Puts a XACML decision requests and corresponding results in cache. The ability to put multiple cache entries at once allows the Cache implementation to
	 * optimize the creation/update by doing them all in the same request, e.g. if the cache is in a remote storage/server.
	 * 
	 * @param resultsByRequest
	 *            (request, result) pairs as key-value pairs to be cached
	 */
	void putAll(Map<IndividualDecisionRequest, Result> resultsByRequest);

}

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
import java.util.List;
import java.util.Map;

import org.ow2.authzforce.xmlns.pdp.ext.AbstractDecisionCache;

/**
 * Authorization (XACML) decision result cache. Implements {@link Closeable} because a cache may use resources external to the JVM such as a disk or connection to a remote server for persistence,
 * replication, clustering, etc. Therefore, these resources must be released by calling {@link #close()} when it is no longer needed.
 * <p>
 * Implementations of this interface are expected to be thread-safe, and allow access by multiple concurrent threads.
 * <p>
 * Note: This is quite similar to Guava Cache interface but specialized for specific type of key and value.
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
	 * Get the decision result from the cache for the given decision request.
	 * 
	 * @param request
	 *            individual decision request
	 * @return the corresponding decision result from cache; null if there is no such result in cache.
	 */
	PdpDecisionResult get(PdpDecisionRequest request);

	/**
	 * Gets the decision result(s) from the cache for the given decision request(s). The ability to get multiple cached results at once allows the Cache implementation to optimize the retrieval by
	 * requesting all in the same request, e.g. if the cache is in a remote storage/server.
	 * 
	 * @param requests
	 *            individual decision request(s)
	 * @return a map where each entry key is an request from {@code requests}, and the value is the corresponding decision result from cache. If there is no such result in cache, the key must not be
	 *         present in the map. In other words, each request in {@code requests} must be a key in the Map returned, except if there is no corresponding result in cache. Therefore, there must not be
	 *         any null key/value in the map.
	 */
	<DECISION_REQ_T extends PdpDecisionRequest> Map<DECISION_REQ_T, PdpDecisionResult> getAll(List<DECISION_REQ_T> requests);

	/**
	 * Puts a decision request and corresponding result in cache.
	 * 
	 * @param request
	 *            individual decision request
	 * @param result
	 *            the corresponding decision result
	 */
	void put(PdpDecisionRequest request, PdpDecisionResult result);

	/**
	 * Puts decision requests and corresponding results in cache. The ability to put multiple cache entries at once allows the Cache implementation to optimize the creation/update by doing them all in
	 * the same request, e.g. if the cache is in a remote storage/server.
	 * 
	 * @param resultsByRequest
	 *            (request, result) pairs as key-value pairs to be cached
	 */
	<DECISION_REQ_T extends PdpDecisionRequest> void putAll(Map<DECISION_REQ_T, PdpDecisionResult> resultsByRequest);

}

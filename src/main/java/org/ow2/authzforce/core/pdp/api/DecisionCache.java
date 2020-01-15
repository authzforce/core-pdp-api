/**
 * Copyright 2012-2020 THALES.
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
import java.util.List;
import java.util.Map;

import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
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
		 * @param envProps
		 *            environment properties
		 * @param attributeValueFactories
		 *            AttributeValue factories for the decision cache system to be able to create/restore AttributeValues from deserialized data stored or produced by external - possibly remote -
		 *            systems (e.g. cache storage database). Remember that such attribute values can be present in decision results, typically in AttributeAssignments of Obligations/Advice.
		 * @return instance of extension
		 */
		public abstract DecisionCache getInstance(CONF_T conf, AttributeValueFactoryRegistry attributeValueFactories, EnvironmentProperties envProps);
	}

	/**
	 * Tells the PDP to always pass a valid/non-null {@link EvaluationContext} argument - representing the PDP's evaluation context - to other methods of this API with {@link EvaluationContext} arg.
	 * Else the PDP may pass a null value to save time and memory.
	 * 
	 * @return true iff a non-null {@link EvaluationContext} argument - referring to the PDP's evaluation context - is required for all methods of this API with {@link EvaluationContext} arg of this
	 *         {@link DecisionCache}.
	 */
	boolean isEvaluationContextRequired();

	/**
	 * Get the decision result from the cache for the given decision request.
	 * 
	 * @param request
	 *            individual decision request
	 * @param evalCtx
	 *            evaluation context that can be used to save context about any partial/preliminary evaluation done by this decision cache when there is no cached result for {code request} yet. In
	 *            this case, the PDP will call back {@link DecisionCache#put(DecisionRequest, DecisionResult, EvaluationContext)} with this same {@code evalCtx} after the PDP has computed the new
	 *            result. Therefore, this allows the decision cache to reuse some context during an evaluation, and also to do some evaluation itself. This argument may be null if not required, i.e.
	 *            {@link #isEvaluationContextRequired()} returns false.
	 * @return the corresponding decision result from cache; null if there is no such result in cache.
	 */
	DecisionResult get(DecisionRequest request, EvaluationContext evalCtx);

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
	<DECISION_REQ_T extends DecisionRequest> Map<DECISION_REQ_T, DecisionResult> getAll(List<DECISION_REQ_T> requests);

	/**
	 * Puts a decision request and corresponding result in cache.
	 * 
	 * @param request
	 *            individual decision request
	 * @param result
	 *            the corresponding decision result
	 * @param evalCtx
	 *            evaluation context that can be used to retrieve context about any partial/preliminary evaluation done by this decision cache when {@link #get(DecisionRequest, EvaluationContext)} was
	 *            called in the same request context. This argument may be null if not required, i.e. {@link #isEvaluationContextRequired()} returns false.
	 */
	void put(DecisionRequest request, DecisionResult result, EvaluationContext evalCtx);

	/**
	 * Puts decision requests and corresponding results in cache. The ability to put multiple cache entries at once allows the Cache implementation to optimize the creation/update by doing them all in
	 * the same request, e.g. if the cache is in a remote storage/server.
	 * 
	 * @param resultsByRequest
	 *            (request, result) pairs as key-value pairs to be cached
	 */
	<DECISION_REQ_T extends DecisionRequest> void putAll(Map<DECISION_REQ_T, DecisionResult> resultsByRequest);

}

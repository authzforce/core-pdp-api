/**
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
package org.ow2.authzforce.core.pdp.api.io;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ow2.authzforce.core.pdp.api.CloseablePdpEngine;
import org.ow2.authzforce.core.pdp.api.DecisionRequest;
import org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Generic implementation of {@link PdpEngineInoutAdapter}
 *
 * 
 * @param <ADAPTER_INPUT_DECISION_REQUEST>
 *            type of original input decision request handled by this class. It may correspond to multiple individual decision requests (e.g. using XACML Multiple Decision Profile). Usually
 *            serializable, e.g. XACML-schema-derived JAXB Request for XML.
 * @param <ADAPTEE_INPUT_DECISION_REQUEST>
 *            type of individual decision request passed to the adaptee, i.e. {@link CloseablePdpEngine} instance.
 * @param <ADAPTER_OUTPUT_DECISION_RESULT>
 *            type of output result corresponding to ADAPTER_INPUT_DECISION_REQUEST. Usually serializable, e.g. XACML-schema-derived JAXB Result for XML.
 *
 */
public final class BasePdpEngineAdapter<ADAPTER_INPUT_DECISION_REQUEST, ADAPTEE_INPUT_DECISION_REQUEST extends DecisionRequest, ADAPTER_OUTPUT_DECISION_RESULT> implements
		PdpEngineInoutAdapter<ADAPTER_INPUT_DECISION_REQUEST, ADAPTER_OUTPUT_DECISION_RESULT>
{

	private static final Logger LOGGER = LoggerFactory.getLogger(BasePdpEngineAdapter.class);
	private static final IllegalArgumentException ILLEGAL_ARGUMENT_EXCEPTION = new IllegalArgumentException("No input Decision Request");
	private final CloseablePdpEngine adaptee;
	private final DecisionRequestPreprocessor<ADAPTER_INPUT_DECISION_REQUEST, ADAPTEE_INPUT_DECISION_REQUEST> reqPreproc;
	private final DecisionResultPostprocessor<ADAPTEE_INPUT_DECISION_REQUEST, ADAPTER_OUTPUT_DECISION_RESULT> resultPostproc;

	/**
	 * Constructor
	 * 
	 * @param adaptedPdpEngine
	 *            adapted PDP engine
	 *
	 * @param requestPreproc
	 *            Decision request preprocessor, transforming to XACML Request prior to policy evaluation. is used.
	 * @param resultPostproc
	 *            Decision result postprocessor, transforming to XACML Result after policy evaluation.
	 * @throws java.lang.IllegalArgumentException
	 *             if any parameter is null
	 */
	public BasePdpEngineAdapter(final CloseablePdpEngine adaptedPdpEngine, final DecisionRequestPreprocessor<ADAPTER_INPUT_DECISION_REQUEST, ADAPTEE_INPUT_DECISION_REQUEST> requestPreproc,
			final DecisionResultPostprocessor<ADAPTEE_INPUT_DECISION_REQUEST, ADAPTER_OUTPUT_DECISION_RESULT> resultPostproc) throws IllegalArgumentException
	{
		Preconditions.checkNotNull(adaptedPdpEngine, "Undefined adaptedPdpEngine arg (adapted PDP engine)");
		Preconditions.checkNotNull(requestPreproc, "Undefined requestPreproc arg (decision request preprocessor)");
		Preconditions.checkNotNull(resultPostproc, "Undefined resultPostproc arg (decision result postrocessor)");
		this.adaptee = adaptedPdpEngine;
		this.reqPreproc = requestPreproc;
		this.resultPostproc = resultPostproc;
	}

	@Override
	public Iterable<PrimaryPolicyMetadata> getApplicablePolicies()
	{
		return this.adaptee.getApplicablePolicies();
	}

	/**
	 * Evaluates a XML/JAXB-based XACML decision request
	 * <p>
	 * Note that if the request is somehow invalid (it was missing a required attribute, it was using an unsupported scope, etc), then the result will be a decision of INDETERMINATE.
	 * 
	 * @param request
	 *            the request to evaluate
	 * @param namespaceURIsByPrefix
	 *            namespace prefix-URI mappings (e.g. "... xmlns:prefix=uri") in the original XACML Request bound to {@code req}, used as part of the context for XPath evaluation
	 * @return the response to the request
	 */
	@Override
	public ADAPTER_OUTPUT_DECISION_RESULT evaluate(final ADAPTER_INPUT_DECISION_REQUEST request, final Map<String, String> namespaceURIsByPrefix)
	{
		if (request == null)
		{
			throw ILLEGAL_ARGUMENT_EXCEPTION;
		}

		/*
		 * The request parser may return multiple individual decision requests from a single Request, e.g. if the request parser implements the Multiple Decision profile or Hierarchical Resource
		 * profile
		 */
		final List<ADAPTEE_INPUT_DECISION_REQUEST> individualDecisionRequests;
		try
		{
			individualDecisionRequests = this.reqPreproc.process(request, namespaceURIsByPrefix);
		}
		catch (final IndeterminateEvaluationException e)
		{
			LOGGER.info("Invalid or unsupported input XACML Request syntax", e);
			return this.resultPostproc.processClientError(e);
		}

		final Collection<Entry<ADAPTEE_INPUT_DECISION_REQUEST, ? extends DecisionResult>> resultsByRequest;
		try
		{
			resultsByRequest = this.adaptee.evaluate(individualDecisionRequests);
		}
		catch (final IndeterminateEvaluationException e)
		{
			LOGGER.info("Error preventing any individual decision request evaluation", e);
			return this.resultPostproc.processInternalError(e);
		}

		return this.resultPostproc.process(resultsByRequest);
	}

	/**
	 * Same as {@link #evaluate(Object, Map)} but with null/empty {@code namespaceURIsByPrefix}
	 * 
	 * @param request
	 *            the request to evaluate
	 * @return the response to the request
	 */
	@Override
	public ADAPTER_OUTPUT_DECISION_RESULT evaluate(final ADAPTER_INPUT_DECISION_REQUEST request)
	{
		return evaluate(request, null);
	}

	@Override
	public void close() throws IOException
	{
		this.adaptee.close();
	}

}
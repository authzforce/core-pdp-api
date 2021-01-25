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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.dom.DOMResult;

import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.PepAction;
import org.ow2.authzforce.core.pdp.api.PepActionAttributeAssignment;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.xacml.Xacml3JaxbHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableList;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdvice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligations;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIdentifierList;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusDetail;

/**
 * Convenient base class for {@link DecisionResultPostprocessor} implementations supporting core XACML-schema-defined XML output handled by JAXB framework
 * 
 */
public class BaseXacmlJaxbResultPostprocessor implements DecisionResultPostprocessor<IndividualXacmlJaxbRequest, Response>
{
	private static final IllegalArgumentException ILLEGAL_RESULTS_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined resultsByRequest arg");
	private static final IllegalArgumentException ILLEGAL_ERROR_ARG_EXCEPTION = new IllegalArgumentException("Undefined input error arg");

	private static List<AttributeAssignment> convert(final ImmutableList<PepActionAttributeAssignment<?>> attributeAssignments)
	{
		if (attributeAssignments == null)
		{
			return null;
		}

		return attributeAssignments.stream().map(attAssignment -> {
			final AttributeValue attVal = attAssignment.getValue();
			return new AttributeAssignment(attVal.getContent(), attAssignment.getDatatype().getId(), attVal.getXmlAttributes(), attAssignment.getAttributeId(),
			        attAssignment.getCategory().orElse(null), attAssignment.getIssuer().orElse(null));
		}).collect(Collectors.toList());
	}

	/**
	 * Convert AuthzForce-specific {@link DecisionResult} to XACML {@link Result}
	 * 
	 * @param request
	 *            request corresponding to result; iff null, some content from it, esp. the list of {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes}, is included in {@code result}
	 * @param result
	 *            native policy decision result
	 * @return XACML Result
	 */
	public static Result convert(final IndividualXacmlJaxbRequest request, final DecisionResult result)
	{
		final ImmutableList<PepAction> pepActions = result.getPepActions();
		assert pepActions != null;

		final List<Obligation> xacmlObligations;
		final List<Advice> xacmlAdvices;
		if (pepActions.isEmpty())
		{
			xacmlObligations = null;
			xacmlAdvices = null;
		}
		else
		{
			xacmlObligations = new ArrayList<>(pepActions.size());
			xacmlAdvices = new ArrayList<>(pepActions.size());
			pepActions.forEach(pepAction -> {
				final String pepActionId = pepAction.getId();
				final List<AttributeAssignment> xacmlAttAssignments = convert(pepAction.getAttributeAssignments());
				if (pepAction.isMandatory())
				{
					xacmlObligations.add(new Obligation(xacmlAttAssignments, pepActionId));
				}
				else
				{
					xacmlAdvices.add(new Advice(xacmlAttAssignments, pepActionId));
				}
			});
		}

		final ImmutableList<PrimaryPolicyMetadata> applicablePolicies = result.getApplicablePolicies();
		final PolicyIdentifierList jaxbPolicyIdentifiers;
		if (applicablePolicies == null || applicablePolicies.isEmpty())
		{
			jaxbPolicyIdentifiers = null;
		}
		else
		{
			final List<JAXBElement<IdReferenceType>> jaxbPolicyIdRefs = new ArrayList<>(applicablePolicies.size());
			for (final PrimaryPolicyMetadata applicablePolicy : applicablePolicies)
			{
				final IdReferenceType jaxbIdRef = new IdReferenceType(applicablePolicy.getId(), applicablePolicy.getVersion().toString(), null, null);
				final JAXBElement<IdReferenceType> jaxbPolicyIdRef = applicablePolicy.getType() == TopLevelPolicyElementType.POLICY
				        ? Xacml3JaxbHelper.XACML_3_0_OBJECT_FACTORY.createPolicyIdReference(jaxbIdRef)
				        : Xacml3JaxbHelper.XACML_3_0_OBJECT_FACTORY.createPolicySetIdReference(jaxbIdRef);
				jaxbPolicyIdRefs.add(jaxbPolicyIdRef);
			}

			jaxbPolicyIdentifiers = new PolicyIdentifierList(jaxbPolicyIdRefs);
		}

		return new Result(result.getDecision(), result.getStatus(), xacmlObligations == null || xacmlObligations.isEmpty() ? null : new Obligations(xacmlObligations),
		        xacmlAdvices == null || xacmlAdvices.isEmpty() ? null : new AssociatedAdvice(xacmlAdvices), request == null ? null : request.getAttributesToBeReturned(), jaxbPolicyIdentifiers);
	}

	private static void addStatusMessageForEachCause(final Throwable cause, final int currentCauseDepth, final int maxIncludedCauseDepth, final List<Element> statusDetailElements,
	        final Marshaller xacml3Marshaller) throws JAXBException
	{
		if (cause == null)
		{
			return;
		}

		assert statusDetailElements != null;

		// create JAXBElement: StatusMessage(cause.getMessage) and convert it to DOM Element
		final DOMResult domResult = new DOMResult();
		xacml3Marshaller.marshal(Xacml3JaxbHelper.XACML_3_0_OBJECT_FACTORY.createStatusMessage(cause.getMessage()), domResult);
		statusDetailElements.add(((Document) domResult.getNode()).getDocumentElement());

		if (currentCauseDepth == maxIncludedCauseDepth)
		{
			return;
		}

		addStatusMessageForEachCause(cause.getCause(), currentCauseDepth + 1, maxIncludedCauseDepth, statusDetailElements, xacml3Marshaller);
	}

	private final int maxDepthOfErrorCauseIncludedInResult;

	/**
	 * Constructor
	 * 
	 * @param clientRequestErrorVerbosityLevel
	 *            Level of verbosity of the error message trace returned in case of client request errors, e.g. invalid requests. Increasing this value usually helps the clients better pinpoint the
	 *            issue with their Requests. This result postprocessor returns all error messages in the Java stacktrace up to the same level as this parameter's value if the stacktrace is bigger,
	 *            else the full stacktrace.
	 * @throws IllegalArgumentException
	 *             if {@code clientRequestErrorVerbosityLevel < 0}
	 */
	public BaseXacmlJaxbResultPostprocessor(final int clientRequestErrorVerbosityLevel) throws IllegalArgumentException
	{
		if (clientRequestErrorVerbosityLevel < 0)
		{
			throw new IllegalArgumentException("Invalid clientRequestErrorVerbosityLevel: " + clientRequestErrorVerbosityLevel + ". Expected: non-negative.");
		}

		this.maxDepthOfErrorCauseIncludedInResult = clientRequestErrorVerbosityLevel;
	}

	@Override
	public final Class<IndividualXacmlJaxbRequest> getRequestType()
	{
		return IndividualXacmlJaxbRequest.class;
	}

	@Override
	public final Class<Response> getResponseType()
	{
		return Response.class;
	}

	@Override
	public Response process(final Collection<Entry<IndividualXacmlJaxbRequest, ? extends DecisionResult>> resultsByRequest)
	{
		if (resultsByRequest == null)
		{
			throw ILLEGAL_RESULTS_ARGUMENT_EXCEPTION;
		}

		final List<Result> results = resultsByRequest.stream().map(entry -> convert(entry.getKey(), entry.getValue())).collect(Collectors.toList());
		return new Response(results);
	}

	@Override
	public Response processClientError(final IndeterminateEvaluationException error)
	{
		if (error == null)
		{
			throw ILLEGAL_ERROR_ARG_EXCEPTION;
		}

		final Status finalStatus;
		if (maxDepthOfErrorCauseIncludedInResult == 0)
		{
			finalStatus = error.getTopLevelStatus();
		}
		else
		{
			/*
			 * Get Status with detailed cause description. The resulting status contains a StatusDetail element with a list of StatusMessage elements. The nth StatusMessage contains the message of the
			 * nth cause in the exception stacktrace (calling {@link Throwable#getMessage()}). The list stops when {@code maxDepthOfErrorCauseIncludedInResult} is reached or there isn't any more cause
			 * left in the stacktrace.
			 * 
			 * Therefore, maxDepthOfErrorCauseIncludedInResult represents the max depth of the cause to be included in the result Status, i.e. any deeper cause is not included. If {@code
			 * maxIncludedCauseDepth == 0}, the result is the same as {@link #getTopLevelStatus()}.
			 */
			final List<Element> statusDetailElements = new ArrayList<>(maxDepthOfErrorCauseIncludedInResult);
			final Marshaller marshaller;
			try
			{
				marshaller = Xacml3JaxbHelper.createXacml3Marshaller();
			}
			catch (final JAXBException e)
			{
				// Should not happen
				throw new RuntimeException("Failed to create XACML/JAXB marshaller to marshall IndeterminateEvaluationException causes into StatusDetail/StatusMessages of Indeterminate Result", e);
			}

			try
			{
				addStatusMessageForEachCause(error.getCause(), 1, maxDepthOfErrorCauseIncludedInResult, statusDetailElements, marshaller);
			}
			catch (final JAXBException e)
			{
				// Should not happen
				throw new RuntimeException("Failed to marshall IndeterminateEvaluationException causes into StatusDetail/StatusMessages of Indeterminate Result", e);
			}

			finalStatus = new StatusHelper(Collections.singletonList(error.getStatusCode()), Optional.ofNullable(error.getMessage()), Optional.of(new StatusDetail(statusDetailElements)));
		}

		final Result result = new Result(DecisionType.INDETERMINATE, finalStatus, null, null, null, null);
		return new Response(Collections.singletonList(result));
	}

	@Override
	public Response processInternalError(final IndeterminateEvaluationException error)
	{
		if (error == null)
		{
			throw ILLEGAL_ERROR_ARG_EXCEPTION;
		}

		final Result result = new Result(DecisionType.INDETERMINATE, error.getTopLevelStatus(), null, null, null, null);
		return new Response(Collections.singletonList(result));
	}

	/**
	 * Convenient base class for {@link org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor.Factory} implementations supporting core XACML-schema-defined XML output handled by JAXB framework
	 * 
	 */
	public static abstract class Factory implements DecisionResultPostprocessor.Factory<IndividualXacmlJaxbRequest, Response>
	{
		private final String id;

		protected Factory(final String id)
		{
			this.id = id;
		}

		@Override
		public final String getId()
		{
			return id;
		}

		@Override
		public final Class<IndividualXacmlJaxbRequest> getRequestType()
		{
			return IndividualXacmlJaxbRequest.class;
		}

		@Override
		public final Class<Response> getResponseType()
		{
			return Response.class;
		}

	}

}

/*
 * Copyright 2012-2023 THALES.
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

import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.MissingAttributeDetail;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusDetail;

import java.util.Optional;

/**
 * Exception wrapper for XACML Indeterminate/error caused by evaluation
 * <p>
 * TODO: although we consider Exceptions as a good solution (from a Java standpoint) to propagate error information with a full traceable stacktrace, from a functional/logical point of view, we could
 * improve performance by using return codes instead, whenever possible, especially where we don't lose any useful error info by doing so: cf.
 * <a href="https://www.baeldung.com/java-exceptions-performance">Performance Effects of Exceptions in Java</a>
 * </p>
 */
public class IndeterminateEvaluationException extends Exception
{
	private static final long serialVersionUID = 1L;

	private static Status validateArg(final ImmutableXacmlStatus status)
	{
		Preconditions.checkArgument(status != null && status.getStatusCode() != null, "Undefined status arg or status code from status arg");
		return status;
	}

	private static String validateArg(String errorMessage)
	{
		Preconditions.checkArgument(errorMessage != null && !errorMessage.isEmpty(), "Undefined/empty error message arg");
		return errorMessage;
	}


	private static Throwable validateArg(final Throwable cause)
	{
		Preconditions.checkArgument(cause != null && cause.getMessage() != null && !cause.getMessage().isEmpty(), "Undefined/empty error message arg");
		return cause;
	}

	private final ImmutableXacmlStatus xacmlStatus;

	// Top-level Status for the final XACML Result, may be different from xacmlStatus, in which case it overrides
	@SuppressFBWarnings(value="SE_BAD_FIELD", justification = "only used internally")
	private final Optional<ImmutableXacmlStatus> overridingTopLevelStatusInFinalResult;

	/**
	 * Instantiates with error message and XACML StatusCode (e.g. {@link org.ow2.authzforce.xacml.identifiers.XacmlStatusCode#PROCESSING_ERROR}), and internal cause for error.
	 *
	 * @param status XACML status, StatusCode value must be a valid xs:anyURI (used as XACML StatusCode Value)
	 * @param cause
	 *            internal cause of error
	 * @param topLevelStatusInFinalResult status is to be set as the top-level status in the final XACML Result, overrides any other error (status) in the stack trace
	 */
	private IndeterminateEvaluationException(final ImmutableXacmlStatus status, final Throwable cause, final Optional<ImmutableXacmlStatus> topLevelStatusInFinalResult)
	{
		super(validateArg(status).getStatusMessage(), cause);
		this.xacmlStatus = status;
		this.overridingTopLevelStatusInFinalResult = topLevelStatusInFinalResult;
	}

	// Any Status with a defined StatusDetail content be override the top-level Status
	private static boolean isTopLevelStatusInFinalResult(Status status) {
		final StatusDetail detail = status.getStatusDetail();
		if(detail == null) {
			return false;
		}

		return !detail.getAnies().isEmpty();
	}

	/**
	 * Instantiates with error message and XACML StatusCode (e.g. {@link org.ow2.authzforce.xacml.identifiers.XacmlStatusCode#PROCESSING_ERROR}), and internal cause for error.
	 * <p>
	 *     When the {@code cause} is itself an {@link IndeterminateEvaluationException}, use the {@link IndeterminateEvaluationException#IndeterminateEvaluationException(String, IndeterminateEvaluationException)}.
	 * </p>
	 *
	 * @param status XACML status, StatusCode value must be a valid xs:anyURI (used as XACML StatusCode Value)
	 * @param cause
	 *            internal cause of error
	 */
	public IndeterminateEvaluationException(final ImmutableXacmlStatus status, final Throwable cause)
	{
		this(status, cause, isTopLevelStatusInFinalResult(status)? Optional.of(status): Optional.empty());
	}

	/**
	 * Creates exception with message and XACML StatusCode (e.g. {@link org.ow2.authzforce.xacml.identifiers.XacmlStatusCode#PROCESSING_ERROR})
	 *
	 * @param status XACML status, StatusCode value must be a valid xs:anyURI (used as XACML StatusCode Value)
	 */
	public IndeterminateEvaluationException(final ImmutableXacmlStatus status)
	{
		this(status, null);
	}

	/**
	 * Instantiates with error message and XACML StatusCode (e.g. {@link org.ow2.authzforce.xacml.identifiers.XacmlStatusCode#PROCESSING_ERROR})
	 *
	 * @param message error message XACML status, StatusCode value must be a valid xs:anyURI (used as XACML StatusCode Value)
	 * @param xacmlStatusCode
	 *            XACML StatusCode value
	 * @param cause
	 * 	           internal cause of error
	 */
	public IndeterminateEvaluationException(final String message, final String xacmlStatusCode, final Throwable cause)
	{
		this(new ImmutableXacmlStatus(xacmlStatusCode, Optional.of(validateArg(message))), validateArg(cause));
	}

	/**
	 * Instantiates with error message and cause
	 *
	 * @param message error message XACML status, StatusCode value must be a valid xs:anyURI (used as XACML StatusCode Value)
	 * @param cause
	 * 	           internal cause of error
	 */
	public IndeterminateEvaluationException(final String message, final IndeterminateEvaluationException cause)
	{
		this(new ImmutableXacmlStatus(cause.getStatus().getStatusCode().getValue(), Optional.of(validateArg(message))), validateArg(cause), cause.getOverridingTopLevelStatus());
	}

	/**
	 * Instantiates with error message and XACML StatusCode (e.g. {@link org.ow2.authzforce.xacml.identifiers.XacmlStatusCode#PROCESSING_ERROR})
	 *
	 * @param message error message XACML status, StatusCode value must be a valid xs:anyURI (used as XACML StatusCode Value)
	 * @param xacmlStatusCode
	 *            XACML StatusCode value
	 */
	public IndeterminateEvaluationException(final String message, final String xacmlStatusCode)
	{
		this(new ImmutableXacmlStatus(xacmlStatusCode, Optional.of(validateArg(message))));
	}

	/**
	 * Instantiates with a missing-attribute error status with standard status code - unless a custom status code is defined - and a XACML {@link MissingAttributeDetail}
	 *
	 * @param message error message XACML status
	 * @param missingAttributeDetail
	 *            missing attribute detail
	 * @param customStatusCode overrides standard missing-attribute status code {@link org.ow2.authzforce.xacml.identifiers.XacmlStatusCode#MISSING_ATTRIBUTE}, must be a valid xs:anyURI (used as XACML StatusCode Value)
	 */
	public IndeterminateEvaluationException(final String message, final MissingAttributeDetail missingAttributeDetail, Optional<String> customStatusCode)
	{
		this(new ImmutableXacmlStatus(missingAttributeDetail, customStatusCode, Optional.of(message)));
	}

	/**
	 * Get status corresponding to this exception (last occurred)
	 *
	 * @return status (always non-null)
	 */
	public ImmutableXacmlStatus getStatus()
	{
		return this.xacmlStatus;
	}

	/**
	 * Get Status to be returned as top-level Status in the final Result regardless of any other Status in the error stack trace.
	 * 
	 * @return status
	 */
	public Optional<ImmutableXacmlStatus> getOverridingTopLevelStatus()
	{
		return this.overridingTopLevelStatusInFinalResult;
	}

	/**
	 * Get top-level status, i.e. {@link #getOverridingTopLevelStatus()} if present, else {@link #getStatus()}
	 *
	 * @return status top-level status for final result
	 */
	public ImmutableXacmlStatus getTopLevelStatus()
	{
		return this.overridingTopLevelStatusInFinalResult.orElse(xacmlStatus);
	}

}

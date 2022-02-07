/*
 * Copyright 2012-2022 THALES.
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
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

import java.util.Optional;

/**
 * Exception wrapper for XACML Indeterminate/error caused by evaluation
 * <p>
 * TODO: although we consider Exceptions as a good solution (from a Java standpoint) to propagate error information with a full traceable stacktrace, from a functional/logical point of view, we could
 * improve performance by using return codes instead, whenever possible, especially where we don't lose any useful error info by doing so: cf.
 * http://java-performance.info/throwing-an-exception-in-java-is-very-slow/
 * </p>
 */
public class IndeterminateEvaluationException extends Exception
{
	private static final long serialVersionUID = 1L;


	private static Status validateArg(ImmutableXacmlStatus status)
	{
		Preconditions.checkArgument(status != null && status.getStatusCode() != null, "Undefined status arg or status code from status arg");
		return status;
	}

	private static String validateArg(String errorMessage)
	{
		Preconditions.checkArgument(errorMessage != null && !errorMessage.isEmpty(), "Undefined/empty error message arg");
		return errorMessage;
	}


	private static Throwable validateArg(Throwable cause)
	{
		Preconditions.checkArgument(cause != null && cause.getMessage() != null && !cause.getMessage().isEmpty(), "Undefined/empty error message arg");
		return cause;
	}

	private final ImmutableXacmlStatus xacmlStatus;

	/**
	 * Instantiates with error message and XACML StatusCode (e.g. {@link org.ow2.authzforce.xacml.identifiers.XacmlStatusCode#PROCESSING_ERROR}), and internal cause for error
	 *
	 * @param status XACML status, StatusCode value must be a valid xs:anyURI (used as XACML StatusCode Value)
	 * @param cause
	 *            internal cause of error
	 */
	public IndeterminateEvaluationException(final ImmutableXacmlStatus status, final Throwable cause)
	{
		super(validateArg(status).getStatusMessage(), cause);
		this.xacmlStatus = status;
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
	public IndeterminateEvaluationException(final String message, String xacmlStatusCode, Throwable cause)
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
	public IndeterminateEvaluationException(final String message, IndeterminateEvaluationException cause)
	{
		this(new ImmutableXacmlStatus(cause.getTopLevelStatus().getStatusCode().getValue(), Optional.of(validateArg(message))), validateArg(cause));
	}

	/**
	 * Instantiates with error message and XACML StatusCode (e.g. {@link org.ow2.authzforce.xacml.identifiers.XacmlStatusCode#PROCESSING_ERROR})
	 *
	 * @param message error message XACML status, StatusCode value must be a valid xs:anyURI (used as XACML StatusCode Value)
	 * @param xacmlStatusCode
	 *            XACML StatusCode value
	 */
	public IndeterminateEvaluationException(final String message, String xacmlStatusCode)
	{
		this(new ImmutableXacmlStatus(xacmlStatusCode, Optional.of(validateArg(message))));
	}

	/**
	 * Get status corresponding to the top-level exception (last occurred) in the stacktrace
	 * 
	 * @return status (always non-null)
	 */
	public ImmutableXacmlStatus getTopLevelStatus()
	{
		return this.xacmlStatus;
	}

}

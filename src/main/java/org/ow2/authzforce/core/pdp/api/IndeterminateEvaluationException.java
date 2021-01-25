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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

import java.util.Optional;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

import com.google.common.base.Preconditions;

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
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String xacmlStatusCode;

	private transient volatile Status topLevelStatus = null;

	/**
	 * Creates exception with message and XACML StatusCode (e.g. {@link org.ow2.authzforce.xacml.identifiers.XacmlStatusCode#PROCESSING_ERROR})
	 * 
	 * @param message
	 *            exception message
	 * @param statusCode
	 *            XACML StatusCode value, must be a valid xs:anyURI (used as XACML StatusCode Value)
	 */
	public IndeterminateEvaluationException(final String message, final String statusCode)
	{
		this(message, statusCode, null);
	}

	/**
	 * Instantiates with error message and XACML StatusCode (e.g. {@link org.ow2.authzforce.xacml.identifiers.XacmlStatusCode#PROCESSING_ERROR}), and internal cause for error
	 * 
	 * @param message
	 *            exception message
	 * @param statusCode
	 *            XACML StatusCode value, must be a Valid xs:anyURI (used as XACML StatusCode Value)
	 * @param cause
	 *            internal cause of error
	 */
	public IndeterminateEvaluationException(final String message, final String statusCode, final Throwable cause)
	{
		super(message, cause);
		Preconditions.checkNotNull(statusCode, "Undefined status code (statusCode arg)");
		this.xacmlStatusCode = statusCode;
	}

	/**
	 * Get XACML status code for this "Indeterminate"
	 * 
	 * @return StatusCode value
	 */
	public String getStatusCode()
	{
		return xacmlStatusCode;
	}

	/**
	 * Get status corresponding to the top-level exception (last occurred) in the stacktrace
	 * 
	 * @return status
	 */
	public Status getTopLevelStatus()
	{
		if (topLevelStatus == null)
		{
			topLevelStatus = new StatusHelper(xacmlStatusCode, Optional.ofNullable(this.getMessage()));
		}

		return topLevelStatus;
	}

}

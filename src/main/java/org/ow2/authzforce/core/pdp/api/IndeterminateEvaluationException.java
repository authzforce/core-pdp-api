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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.dom.DOMResult;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusDetail;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

	/**
	 * Creates exception with message and XACML StatusCode (e.g. {@link StatusHelper#STATUS_PROCESSING_ERROR})
	 * 
	 * @param message
	 *            exception message
	 * @param statusCode
	 *            XACML StatusCode value, must be a valid xs:anyURI (used as XACML StatusCode Value)
	 */
	public IndeterminateEvaluationException(final String message, final String statusCode)
	{
		super(message);
		this.xacmlStatusCode = statusCode;
	}

	/**
	 * Instantiates with error message and XACML StatusCode (e.g. {@link StatusHelper#STATUS_PROCESSING_ERROR}), and internal cause for error
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
	 * Get Status
	 * 
	 * @return status
	 */
	public Status getStatus()
	{
		return new StatusHelper(xacmlStatusCode, Optional.ofNullable(this.getMessage()));
	}

	/**
	 * Get Status with detailed cause description. The resulting status contains a StatusDetail element with a list of StatusMessage elements. The nth StatusMessage contains the message of the nth
	 * cause in the exception stacktrace (calling {@link Throwable#getMessage()}) The list stops when {@code maxIncludedCauseDepth} is reached or there isn't any more cause left in the stacktrace.
	 *
	 * @param maxIncludedCauseDepth
	 *            max depth of the cause to be included in the result Status, i.e. any deeper cause is not included. If {@code maxIncludedCauseDepth < 1}, the result is the same as
	 *            {@link #getStatus()}.
	 *
	 * @return status
	 */
	public Status getStatus(final int maxIncludedCauseDepth)
	{
		if (maxIncludedCauseDepth < 1)
		{
			return getStatus();
		}

		final List<Element> statusDetailElements = new ArrayList<>(maxIncludedCauseDepth);
		final Marshaller marshaller;
		try
		{
			marshaller = JaxbXACMLUtils.createXacml3Marshaller();
		}
		catch (final JAXBException e)
		{
			// Should not happen
			throw new RuntimeException("Failed to create XACML/JAXB marshaller to marshall IndeterminateEvaluationException causes into StatusDetail/StatusMessages of Indeterminate Result", e);
		}

		try
		{
			addStatusMessageForEachCause(this.getCause(), 1, maxIncludedCauseDepth, statusDetailElements, marshaller);
		}
		catch (final JAXBException e)
		{
			// Should not happen
			throw new RuntimeException("Failed to marshall IndeterminateEvaluationException causes into StatusDetail/StatusMessages of Indeterminate Result", e);
		}

		return new StatusHelper(Collections.singletonList(xacmlStatusCode), Optional.ofNullable(this.getMessage()), Optional.of(new StatusDetail(statusDetailElements)));
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
		xacml3Marshaller.marshal(JaxbXACMLUtils.XACML_3_0_OBJECT_FACTORY.createStatusMessage(cause.getMessage()), domResult);
		statusDetailElements.add(((Document) domResult.getNode()).getDocumentElement());

		if (currentCauseDepth == maxIncludedCauseDepth)
		{
			return;
		}

		addStatusMessageForEachCause(cause.getCause(), currentCauseDepth + 1, maxIncludedCauseDepth, statusDetailElements, xacml3Marshaller);
	}

}

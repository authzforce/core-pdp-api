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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusCode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusDetail;

/**
 * Simplifies XACML Status handling.
 */
public final class StatusHelper extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Status
{

	private static final long serialVersionUID = 1L;

	/**
	 * STATUS OK (as specified by XACML standard)
	 */
	public static final StatusHelper OK = new StatusHelper(XacmlStatusCode.OK.value(), Optional.empty());

	/**
	 * Max depth of status code. StatusCode in XACML schema is a recursive structure like an error stacktrace that allows chaining status codes endlessly unless the implementation enforces a maximum
	 * depth as done here.
	 */
	public static final int MAX_STATUS_CODE_DEPTH = 10;

	/**
	 * Constructor that takes the status code, an optional message, and some detail to include with the status. Note that the specification explicitly says that a status code of OK, SyntaxError or
	 * ProcessingError may not appear with status detail, so an exception is thrown if one of these status codes is used and detail is included.
	 * 
	 * @param codes
	 *            a <code>List</code> of codes of type xs:anyURI, typically just one code, but this may contain any number of minor codes after the first item in the list, which is the major code
	 * @param message
	 *            a message to include with the code, or null if there should be no message
	 * @param detail
	 *            the status detail to include, or null if there is no detail
	 * 
	 * @throws IllegalArgumentException
	 *             if detail is included for a status code that doesn't allow detail
	 */
	public StatusHelper(final List<String> codes, final Optional<String> message, final Optional<StatusDetail> detail) throws IllegalArgumentException
	{
		if (codes == null)
		{
			throw new IllegalArgumentException("status code value undefined");
		}

		/*
		 * According to XACML 3.0 spec, section 5.57, if the code is ok, syntax error or processing error, there must not be any StatusDetail included
		 */
		if (detail.isPresent())
		{
			final String c = codes.iterator().next();
			if (c.equals(XacmlStatusCode.OK.value()) || c.equals(XacmlStatusCode.SYNTAX_ERROR.value()) || c.equals(XacmlStatusCode.PROCESSING_ERROR.value()))
			{
				throw new IllegalArgumentException("status detail not allowed with status code: " + c);
			}
		}

		final StatusCode statusCodeFromStrings = stringsToStatusCode(codes.iterator(), MAX_STATUS_CODE_DEPTH);
		if (statusCodeFromStrings == null)
		{
			throw new IllegalArgumentException("Invalid status code values: " + codes);
		}

		this.statusCode = statusCodeFromStrings;
		this.statusMessage = message.orElse(null);
		this.statusDetail = detail.orElse(null);
	}

	/**
	 * Constructor that takes both the status code and a message to include with the status.
	 * 
	 * @param codes
	 *            a <code>List</code> of codes of type xs:anyURI, typically just one code, but this may contain any number of minor codes after the first item in the list, which is the major code
	 * @param message
	 *            a message to include with the code
	 */
	public StatusHelper(final List<String> codes, final Optional<String> message)
	{
		this(codes, message, Optional.empty());
	}

	/**
	 * Constructor that takes only the status code.
	 * 
	 * @param code
	 *            status code, must be a valid xs:anyURI
	 * @param message
	 *            status message
	 */
	public StatusHelper(final String code, final Optional<String> message)
	{
		this(Collections.singletonList(code), message, Optional.empty());
	}

	/**
	 * Builds the chain of status codes (recursive) (similar to error stacktrace)
	 * 
	 * @param codesIterator iterator over status codes
	 * @param depth depth of the StatusCode (max length of the chain of status codes)
	 * @return recursive status code
	 */
	private static StatusCode stringsToStatusCode(final Iterator<String> codesIterator, final int depth)
	{
		if (!codesIterator.hasNext())
		{
			return null;
		}

		final String codeVal = codesIterator.next();
		if (codeVal == null)
		{
			throw new IllegalArgumentException("Null status code found");
		}

		final StatusCode nextStatusCode = depth == 0 ? null : stringsToStatusCode(codesIterator, depth - 1);
		return new StatusCode(nextStatusCode, codeVal);
	}

	// /**
	// * For testing instantiation of StatusHelper
	// *
	// * @param args
	// */
	// public static void main(String[] args)
	// {
	// final String[] codes = { "aaa", "bbb", "ccc" };
	// final List<String> codeList = Arrays.asList(codes);
	// final StatusHelper status = new StatusHelper(codeList, "OK", null);
	// System.out.println(status);
	// }

}

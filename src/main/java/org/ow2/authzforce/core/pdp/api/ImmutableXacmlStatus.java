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
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusCode;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Immutable XACML Status, simplifies XACML Status handling (not supporting StatusDetails, always set null).
 */
@Immutable
public final class ImmutableXacmlStatus extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Status
{

	private static final long serialVersionUID = 1L;

	/**
	 * STATUS OK (as specified by XACML standard)
	 */
	public static final ImmutableXacmlStatus OK = new ImmutableXacmlStatus(XacmlStatusCode.OK.value(), Optional.empty());

	/**
	 * Max depth of status code. StatusCode in XACML schema is a recursive structure like an error stacktrace that allows chaining status codes endlessly unless the implementation enforces a maximum
	 * depth as done here.
	 */
	public static final int MAX_STATUS_CODE_DEPTH = 10;

	/**
	 * Constructor that takes the status code, an optional message, and some detail to include with the status. Note that the specification explicitly says that a status code of OK, SyntaxError or
	 * ProcessingError may not appear with status detail, so an exception is thrown if one of these status codes is used and detail is included.
	 *
	 * Note: StatusDetail field not supported.
	 *  According to XACML 3.0 spec, section 5.57, if the code is ok, syntax error or processing error, there must not be any StatusDetail included.
	 * 
	 * @param codes
	 *            a <code>List</code> of codes of type xs:anyURI, typically just one code, but this may contain any number of minor codes after the first item in the list, which is the major code
	 * @param message
	 *            a message to include with the code, or null if there should be no message
	 * 
	 * @throws IllegalArgumentException
	 *             if detail is included for a status code that doesn't allow detail
	 */
	public ImmutableXacmlStatus(final List<String> codes, final Optional<String> message) throws IllegalArgumentException
	{
		if (codes == null || codes.isEmpty())
		{
			throw new IllegalArgumentException("status code value undefined");
		}

		// stringsToStatusCode(...) != null;
		this.statusCode = stringsToStatusCode(codes.iterator(), MAX_STATUS_CODE_DEPTH);
		this.statusMessage = message.orElse(null);
		this.statusDetail = null;
	}

	/**
	 * Constructor that takes only the status code.
	 * 
	 * @param code
	 *            status code, must be a valid xs:anyURI
	 * @param message
	 *            status message
	 */
	public ImmutableXacmlStatus(final String code, final Optional<String> message)
	{
		this(Collections.singletonList(code), message);
	}

	private static ImmutableXacmlStatusCode newImmutableXacmlStatusCode(final StatusCode statusCode, final int depth) {
		Preconditions.checkArgument(statusCode != null);
		final StatusCode nestedCode = depth == 0? null: statusCode.getStatusCode();
		// FIXME: potential infinite recursion
		return new ImmutableXacmlStatusCode(statusCode.getValue(), nestedCode == null? Optional.empty(): Optional.of(newImmutableXacmlStatusCode(nestedCode, depth -1)));
	}

	/**
	 * Constructor similar to {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.Status#Status(StatusCode, String, oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusDetail)} without StatusDetail (not supported)
	 * @param statusCode code
	 * @param statusMessage message
	 */
	public ImmutableXacmlStatus(StatusCode statusCode, String statusMessage) {
		super(newImmutableXacmlStatusCode(statusCode, MAX_STATUS_CODE_DEPTH), statusMessage, null);
	}

	/**
	 * Builds the chain of status codes (recursive) (similar to error stacktrace)
	 * 
	 * @param codesIterator iterator over status codes
	 * @param depth depth of the StatusCode (max length of the chain of status codes)
	 * @return recursive status code
	 */
	private static ImmutableXacmlStatusCode stringsToStatusCode(final Iterator<String> codesIterator, final int depth)
	{
		assert codesIterator.hasNext();
		final String codeVal = codesIterator.next();
		if (codeVal == null)
		{
			throw new IllegalArgumentException("Null status code found");
		}

		final ImmutableXacmlStatusCode nextStatusCode = depth == 0 || !codesIterator.hasNext() ? null : stringsToStatusCode(codesIterator, depth - 1);
		return new ImmutableXacmlStatusCode(codeVal, Optional.ofNullable(nextStatusCode));
	}

	// /**
	// * For testing instantiation of ImmutableXacmlStatus
	// *
	// * @param args
	// */
	// public static void main(String[] args)
	// {
	// final String[] codes = { "aaa", "bbb", "ccc" };
	// final List<String> codeList = Arrays.asList(codes);
	// final ImmutableXacmlStatus status = new ImmutableXacmlStatus(codeList, "OK", null);
	// System.out.println(status);
	// }

}

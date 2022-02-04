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

import java.util.Optional;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

/**
 * "Named" Attribute Provider, i.e. providing "named attribute(s)" as defined in §7.3 of XACML 3.0 specification (resolve {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType}s in a specific way, e.g. from a specific attribute source):
 * <p>
 * <i>A named attribute is the term used for the criteria that the specific attribute designators use to refer to particular attributes in the <Attributes> elements of the request context.</i>
 * </p>
 * 
 */
public interface NamedAttributeProvider
{

	UnsupportedOperationException NOT_IMPLEMENTED_EXCEPTION = new UnsupportedOperationException("Not implemented");

	/**
	 * Returns the non-null non-empty <code>Set</code> of <code>AttributeDesignator</code>s provided/supported by this Attribute provider.
	 *
	 * Each {@link AttributeDesignatorType#getCategory() } must return a non-null/non-empty value
	 *
	 * If any AttributeDesignator in the set does not specify an AttributeId (getAttributeId() = null), the Attribute provider is considered a category-wide attribute provider for the category (returned by {@link AttributeDesignatorType#getCategory()} ), i.e. may provide any attribute in this category.
	 * 
	 * @return a non-null non-empty <code>Set</code> of supported <code>AttributeDesignatorType</code>s
	 */
	Set<AttributeDesignatorType> getProvidedAttributes();

	/**
	 * Indicates support of {@link #beginMultipleDecisionRequest(EvaluationContext)}, i.e. whether this method must be called on this attribute provider at the beginning of each Multiple Decision Request evaluation
	 * @return true iff {@link #beginMultipleDecisionRequest(EvaluationContext)} is supported/implemented. If true, then {@link #beginMultipleDecisionRequest(EvaluationContext)} <b>must be implemented</b>.
	 */
	default boolean supportsBeginMultipleDecisionRequest() {
		return false;
	}

	/**
	 * When the Multiple Decision Profile is used, the PDP engine calls this method before evaluating the Individual Decision Requests of a given Multiple Decision request if the Attribute Provider supports it as indicated by {@link #supportsBeginMultipleDecisionRequest()}..
	 * This enables the attribute provider to set attributes and/or variables in the scope of the Multiple Decision Request, therefore reuse the same values in all its Individual Decision Requests.
	 * A typical use case is an AttributeProvider providing the current date/time which should be the same for all Individual Decision requests within the same Multiple Decision request in order to be consistent (e.g. AuthzForce built-in StandardEnvironmentAttributeProvider configured with override=true). In this case, the AttributeProvider may set the current date/time once and for all on the {@code mdpContext} for a given Multiple Decision request, and reuse it later for each Individual Decision context in {@link #get(AttributeFqn, Datatype, EvaluationContext, Optional)} .
	 * @param mdpContext context of a Multiple Decision request evaluation, will be passed on as {@code mdpContext} argument of  {@link #get(AttributeFqn, Datatype, EvaluationContext, Optional)} when Individual Decision requests are evaluated.
	 */
	default void beginMultipleDecisionRequest(EvaluationContext mdpContext) {
		throw NOT_IMPLEMENTED_EXCEPTION;
	}

	/**
	 * Indicates support of {@link #beginIndividualDecisionRequest(EvaluationContext, Optional)}, i.e. whether this method must be called on this attribute provider at the beginning of each Individual Decision Request evaluation
	 * @return true iff {@link #beginIndividualDecisionRequest(EvaluationContext, Optional)} is supported/implemented. If true, then {@link #beginIndividualDecisionRequest(EvaluationContext, Optional)} <b>must be implemented</b>.
	 */
	default boolean supportsBeginIndividualDecisionRequest() {
		return false;
	}

	/**
	 * The PDP engine calls this method before evaluating each Individual Decision Request (whether it is part of a Multiple Decision request or not) if the Attribute Provider supports it as indicated by {@link #supportsBeginIndividualDecisionRequest()}.
	 * This enables the attribute provider to do some validation of the request (e.g. check dependency attributes) and/or set/override attributes or variables of the request before the policy evaluation begins, therefore reuse those values for its benefit during the evaluation.
	 * A typical use case is an AttributeProvider providing the current date/time, e.g. either overriding current-* attributes (override mode) of the request or checking that current-date and current-time are consistent with current-dateTime.
	 * @param mdpContext context of a Multiple Decision request evaluation, will be passed on as {@code mdpContext} argument of  {@link #get(AttributeFqn, Datatype, EvaluationContext, Optional)} when AttributeDesignator/AttributeSelector are evaluated for a given Individual Decision request.
	 */
	default void beginIndividualDecisionRequest(EvaluationContext individualDecisionContext, Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException {
		throw NOT_IMPLEMENTED_EXCEPTION;
	}

	/**
	 * Provides values of the attribute matching the given designator data. If no value found, but no other error occurred, an empty bag is returned.
	 *
	 * @param attributeFQN
	 *            the global identifier (Category,Issuer,AttributeId) of the attribute to find, must match one of the AttributeDesignatorTypes returned by {@link #getProvidedAttributes()}
	 * @param individualDecisionContext
	 *            the (Individual Decision) request context
	 * @param mdpContext
	 * 	 *            the context of the Multiple Decision request that the {@code individualDecisionContext} belongs to if the Multiple Decision Profile is used; same as the {@code mdpContext} parameter of {@link #beginMultipleDecisionRequest(EvaluationContext)}.
	 * @param datatype
	 *            attribute datatype, must match the data-type of the AttributeDesignatorType matching {@code attributeFQN} in the {@link #getProvidedAttributes()}'s result set
	 * @return the result of retrieving the attribute, which will be a bag of values of type defined by {@code returnDatatype}; empty bag iff no value found and no error occurred.
	 * @throws UnsupportedOperationException
	 *             {@code attributeFQN} or {@code returnDatatype} are not supported (the PDP engine should try another attribute provider if any)
	 * @throws IndeterminateEvaluationException
	 *             {@code attributeFQN} or {@code returnDatatype} are supported but some error occurred while trying to resolve the attribute value(s)
	 */
	<AV extends AttributeValue> AttributeBag<AV> get(AttributeFqn attributeFQN, Datatype<AV> datatype, EvaluationContext individualDecisionContext, Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException;

}

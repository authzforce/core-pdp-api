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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;

import java.util.Optional;

/**
 * "Named" Attribute Provider, similar to {@link NamedAttributeProvider} but provides one and only one named attribute,
 * used for AttributeDesignator/AttributeSelector (ContextSelectorId) evaluation
 * 
 */
public interface SingleNamedAttributeProvider<AV extends AttributeValue>
{

	/**
	 * Returns the non-null <code>AttributeDesignator</code>s provided/supported by this provider.
	 * 
	 * @return the non-null supported <code>AttributeDesignatorType</code>
	 */
	AttributeDesignatorType getProvidedAttribute();

	/**
	 * When the Multiple Decision Profile is used, the PDP engine calls this method before evaluating the Individual Decision Requests of a given Multiple Decision request.
	 * This enables the attribute provider to set attributes and/or variables in the scope of the Multiple Decision Request, therefore reuse the same values in all its Individual Decision Requests.
	 * A typical use case is an AttributeProvider providing the current date/time which should be the same for all Individual Decision requests within the same Multiple Decision request in order to be consistent (e.g. AuthzForce built-in StandardEnvironmentAttributeProvider configured with override=true). In this case, the AttributeProvider may set the current date/time once and for all on the {@code mdpContext} for a given Multiple Decision request, and reuse it later for each Individual Decision context in {@link #get(EvaluationContext, Optional)} .
	 * @param mdpContext context of a Multiple Decision request evaluation, will be passed on as {@code mdpContext} argument of  {@link #get(EvaluationContext, Optional)} when Individual Decision requests are evaluated.
	 */
	default void beginMultipleDecisionRequest(EvaluationContext mdpContext) {
		// do nothing special by default
	}

	/**
	 * Provides values of the attribute matching the given designator data. If no value found, but no other error occurred, an empty bag is returned.
	 *
	 * @param individualDecisionContext
	 *            the (Individual Decision) request context
	 * @param mdpContext
	 * 	 *            the context of the Multiple Decision request that the {@code individualDecisionContext} belongs to if the Multiple Decision Profile is used; same as the {@code mdpContext} parameter of {@link #beginMultipleDecisionRequest(EvaluationContext)}.
	 * @throws UnsupportedOperationException
	 *             {@code attributeFQN} or {@code returnDatatype} are not supported (the PDP engine should try another attribute provider if any)
	 * @throws IndeterminateEvaluationException
	 *             {@code attributeFQN} or {@code returnDatatype} are supported but some error occurred while trying to resolve the attribute value(s)
	 */
	AttributeBag<AV> get(EvaluationContext individualDecisionContext, Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException;

}

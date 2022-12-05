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
package org.ow2.authzforce.core.pdp.api.expression;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.MissingAttributeDetail;
import org.ow2.authzforce.core.pdp.api.*;
import org.ow2.authzforce.core.pdp.api.value.*;

import java.util.List;
import java.util.Optional;

/**
 * AttributeDesignator evaluator initialized with and using an {@link NamedAttributeProvider} to retrieve the attribute value
 *
 * @param <AV>
 *            AttributeDesignator evaluation result value's primitive datatype
 * 
 * @version $Id: $
 */
public final class GenericAttributeProviderBasedAttributeDesignatorExpression<AV extends AttributeValue> implements AttributeDesignatorExpression<AV>
{
	private static final IllegalArgumentException NULL_ATTRIBUTE_PROVIDER_EXCEPTION = new IllegalArgumentException("Undefined Attribute Provider");

	private final AttributeFqn attrGUID;
	private final BagDatatype<AV> returnType;
	private final boolean mustBePresent;

	private final transient Bag.Validator mustBePresentEnforcer;
	private final transient SingleNamedAttributeProvider<AV> attrProvider;
	private final transient IndeterminateEvaluationException missingAttributeForUnknownReasonException;
	private final transient IndeterminateEvaluationException missingAttributeBecauseNullContextException;

	// lazy initialization
	private transient volatile String toString = null;
	private transient volatile int hashCode = 0;

	/** {@inheritDoc} */
	@Override
	public Optional<Bag<AV>> getValue()
	{
		/*
		 * context-dependent, therefore not constant
		 */
		return Optional.empty();
	}

	/**
	 * Return an instance of an AttributeDesignator based on an AttributeDesignatorType
	 *
	 * @param attributeName
	 *            the AttributeDesignatorType we want to convert
	 * @param mustBePresent
	 * 		AttributeDesignator's MustBePresent
	 * @param resultDatatype
	 *            expected datatype of the result attribute value ( {@code AV is the expected type of every element in the bag})
	 * @param attributeProvider
	 *            Attribute Provider responsible for finding the values of the attribute designated by {@code attributeName} in a given evaluation context at runtime.
	 *            When {@link #evaluate(EvaluationContext, Optional)} is called, all AttributeProvider(s) are called in the list order
	 * @throws IllegalArgumentException
	 *             if {@code attrDesignator.getCategory() == null || attrDesignator.getAttributeId() == null}
	 */
	public GenericAttributeProviderBasedAttributeDesignatorExpression(final AttributeFqn attributeName, boolean mustBePresent, final BagDatatype<AV> resultDatatype, final SingleNamedAttributeProvider<AV> attributeProvider)
	{
		if (attributeProvider == null)
		{
			throw NULL_ATTRIBUTE_PROVIDER_EXCEPTION;
		}

		this.attrProvider = attributeProvider;
		this.attrGUID = attributeName;
		this.returnType = resultDatatype;

		// error messages/exceptions
		final MissingAttributeDetail missingAttributeDetail = new MissingAttributeDetail(List.of(), attrGUID.getCategory(), attrGUID.getId(), returnType.getElementType().getId(), attrGUID.getIssuer().orElse(null));
		this.mustBePresent = mustBePresent;
		final ImmutableXacmlStatus errorStatusIfMissingAtt = new ImmutableXacmlStatus(missingAttributeDetail, Optional.empty(), Optional.empty());
		this.mustBePresentEnforcer = mustBePresent ? new Bags.NonEmptinessValidator(errorStatusIfMissingAtt) : Bags.DUMB_VALIDATOR;

		this.missingAttributeForUnknownReasonException = new IndeterminateEvaluationException("Missing named Attribute for unknown reason", missingAttributeDetail, Optional.empty());
		this.missingAttributeBecauseNullContextException = new IndeterminateEvaluationException("Missing named Attribute because request context undefined", missingAttributeDetail, Optional.empty());
	}

	@Override
	public AttributeFqn getAttributeFQN()
	{
		return this.attrGUID;
	}

	@Override
	public boolean isNonEmptyBagRequired()
	{
		return this.mustBePresent;
	}

	@Override
	public Bag<AV> evaluate(final EvaluationContext individualDecisionContext, Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
	{
		if (individualDecisionContext == null)
		{
			throw missingAttributeBecauseNullContextException;
		}

		final Bag<AV> bag = this.attrProvider.get(individualDecisionContext, mdpContext);
		if (bag == null)
		{
			throw this.missingAttributeForUnknownReasonException;
		}

		mustBePresentEnforcer.validate(bag);

		/*
		 * if we got here, it means that the bag wasn't empty, or bag was empty AND mustBePresent was false (so validate() succeeded), so we just return the result
		 */
		return bag;
	}

	/** {@inheritDoc} */
	@Override
	public Datatype<Bag<AV>> getReturnType()
	{
		return this.returnType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		if (toString == null)
		{
			toString = "AttributeDesignator [" + this.attrGUID + ", dataType= " + this.returnType.getElementType() + ", mustBePresent= "
			        + this.mustBePresent + "]";
		}

		return toString;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = this.attrGUID.hashCode();
		}

		return hashCode;
	}

	/** Equal iff the Attribute Category/Issuer/Id are equal */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof GenericAttributeProviderBasedAttributeDesignatorExpression))
		{
			return false;
		}

		final GenericAttributeProviderBasedAttributeDesignatorExpression<?> other = (GenericAttributeProviderBasedAttributeDesignatorExpression<?>) obj;
		return this.attrGUID.equals(other.attrGUID);
	}

}

package org.ow2.authzforce.core.pdp.api;

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

import com.google.common.collect.*;
import org.ow2.authzforce.core.pdp.api.expression.VariableReference;
import org.ow2.authzforce.core.pdp.api.value.*;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;

/**
 * A basic partial implementation of {@link EvaluationContext} associated to an XACML Request (abstract in a sense that is not XML or JSON (or other format) specific).
 * @version $Id: $
 */
public abstract class BaseEvaluationContext implements EvaluationContext
{
    /**
     * Logger used for all classes
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseEvaluationContext.class);

    private final Map<AttributeFqn, AttributeBag<?>> namedAttributes;

    private final Map<String, Entry<VariableReference<?>, Value>> varValsById = HashCollections.newMutableMap();

    private final Map<String, Object> mutableProperties = HashCollections.newMutableMap();

    private final boolean returnApplicablePolicyIdList;

    protected final ClassToInstanceMap<Listener> listeners = MutableClassToInstanceMap.create();
    private final Instant creationTimestamp;

    /**
     * Constructs a new <code>IndividualDecisionRequestContext</code> based on the given request attributes and extra contents with support for XPath evaluation against Content element in Attributes
     *
     * @param namedAttributeMap
     *            updatable named attribute map (attribute key and value pairs) from the original Request; null iff none. An attribute key is a global ID based on attribute category,issuer,id. An
     *            attribute value is a bag of primitive values.
     * @param returnApplicablePolicyIdList
     *            true iff list of IDs of policies matched during evaluation must be returned
     */
    protected BaseEvaluationContext(final Map<AttributeFqn, AttributeBag<?>> namedAttributeMap, final boolean returnApplicablePolicyIdList, Optional<Instant> requestTimestamp)
    {
        this.namedAttributes = namedAttributeMap == null ? HashCollections.newUpdatableMap()
                : HashCollections.newUpdatableMap(namedAttributeMap);
        this.returnApplicablePolicyIdList = returnApplicablePolicyIdList;
        this.creationTimestamp = requestTimestamp.orElse(Instant.now());
    }

    @Override
    public final Instant getCreationTimestamp()
    {
        return this.creationTimestamp;
    }

    /** {@inheritDoc} */
    @Override
    public final <AV extends AttributeValue> AttributeBag<AV> getNamedAttributeValue(final AttributeFqn attributeFqn, final Datatype<AV> datatype) throws IndeterminateEvaluationException
    {
        final AttributeBag<?> bagResult = namedAttributes.get(attributeFqn);
        if (bagResult == null)
        {
            return null;
        }

        if (!bagResult.getElementDatatype().equals(datatype))
        {
            throw new IndeterminateEvaluationException("Datatype (" + bagResult.getElementDatatype() + ") of AttributeDesignator " + attributeFqn + " in context is different from expected/requested ("
                    + datatype
                    + "). May be caused by refering to the same Attribute Category/Id/Issuer with different Datatypes in different policy elements and/or attribute providers, which is not allowed.", XacmlStatusCode.SYNTAX_ERROR.value());
        }

        /*
         * If datatype classes match, 'bagResult' should have the same type as 'datatypeClass'.
         */
        final AttributeBag<AV> result = (AttributeBag<AV>) bagResult;
        this.listeners.forEach((lt, l) -> l.namedAttributeValueConsumed(attributeFqn, result));
        return result;
    }

    @Override
    public final boolean putNamedAttributeValue(final AttributeFqn attributeFqn, final AttributeBag<?> result, boolean override)
    {
        if(override) {
            final Bag<?> oldValue = namedAttributes.put(attributeFqn, result);
            final boolean overridden = oldValue != null;
            if(LOGGER.isWarnEnabled() && overridden)
            {
                    LOGGER.warn("Overriding value of AttributeDesignator {} in evaluation context. Old: {}; new: {}", attributeFqn, oldValue, result);
            }

            this.listeners.forEach((lt, l) -> l.namedAttributeValueProduced(attributeFqn, result));
            return overridden;
        }

        // no override
        final Bag<?> old = namedAttributes.putIfAbsent(attributeFqn, result);
        if(old != null)
        {
            LOGGER.warn("Cannot set the value of AttributeDesignator {} already set in evaluation context (override=false): {}; with value: {}", attributeFqn, old, result);
            return true;
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final <V extends Value> V getVariableValue(final String variableId, final Datatype<V> expectedDatatype) throws IndeterminateEvaluationException
    {
        final Entry<VariableReference<?>, Value> entry = varValsById.get(variableId);
        if (entry == null)
        {
            return null;
        }

        try
        {
            return expectedDatatype.cast(entry.getValue());
        } catch (final ClassCastException e)
        {
            throw new IndeterminateEvaluationException("Datatype of variable '" + variableId + "' in context does not match expected datatype: " + expectedDatatype, XacmlStatusCode.PROCESSING_ERROR.value(), e);
        }
    }


    @Override
    public final ImmutableList<Entry<VariableReference<?>, Value>> getVariables()
    {
        return ImmutableList.copyOf(this.varValsById.values());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean putVariableIfAbsent(final VariableReference<?> variableRef, final Value value)
    {
        if (varValsById.putIfAbsent(variableRef.getVariableId(), new AbstractMap.SimpleImmutableEntry<>(variableRef, value)) != null)
        {
            LOGGER.error("Attempt to override value of Variable '{}' already set in evaluation context. Overriding value: {}", variableRef.getVariableId(), value);
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final Entry<VariableReference<?>, Value> removeVariable(final String variableId)
    {
        return varValsById.remove(variableId);
    }

    /** {@inheritDoc} */
    @Override
    public final Object getOther(final String key)
    {
        return mutableProperties.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsKey(final String key)
    {
        return mutableProperties.containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public final void putOther(final String key, final Object val)
    {
        mutableProperties.put(key, val);
    }

    /** {@inheritDoc} */
    @Override
    public final Object remove(final String key)
    {
        return mutableProperties.remove(key);
    }

    /** {@inheritDoc} */
    @Override
    public final Iterator<Entry<AttributeFqn, AttributeBag<?>>> getNamedAttributes()
    {
        final Set<Entry<AttributeFqn, AttributeBag<?>>> immutableAttributeSet = Collections.unmodifiableSet(namedAttributes.entrySet());
        return immutableAttributeSet.iterator();
    }

    @Override
    public final boolean isApplicablePolicyIdListRequested()
    {
        return returnApplicablePolicyIdList;
    }

    @Override
    public final <L extends Listener> L putListener(final Class<L> listenerType, final L listener)
    {
        return this.listeners.putInstance(listenerType, listener);
    }

    @Override
    public final <L extends Listener> L getListener(final Class<L> listenerType)
    {
        return this.listeners.getInstance(listenerType);
    }
}

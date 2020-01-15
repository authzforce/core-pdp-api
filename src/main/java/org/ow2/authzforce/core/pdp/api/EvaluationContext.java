/**
 * Copyright 2012-2020 THALES.
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

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.expression.AttributeDesignatorExpression;
import org.ow2.authzforce.core.pdp.api.expression.AttributeSelectorExpression;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.core.pdp.api.value.XPathValue;

import net.sf.saxon.s9api.XdmNode;

/**
 * Manages context for the policy evaluation of a given authorization decision request. Typically, an instance of this is instantiated whenever the PDP gets a request and needs to perform an
 * evaluation to a authorization decision. Such a context is used and possibly updated all along the evaluation of the request.
 * 
 */
public interface EvaluationContext
{
	/**
	 * Evaluation context listener. Can be used for instance by PDP extensions to be notified when the evaluation context is used (by the PDP engine typically) and then do specific actions in this
	 * case.
	 *
	 */
	interface Listener
	{

		/**
		 * To be called when {@link EvaluationContext#putNamedAttributeValueIfAbsent(AttributeFqn, AttributeBag)} is called iff the value was not available in the context yet (i.e. first time it is
		 * produced in the request context)
		 * 
		 * @param attributeFQN
		 *            attribute GUID (global ID = Category,Issuer,AttributeId)
		 * @param value
		 *            attribute value bag
		 */
		<AV extends AttributeValue> void namedAttributeValueProduced(AttributeFqn attributeFQN, AttributeBag<AV> value);

		/**
		 * To be called when {@link EvaluationContext#getNamedAttributeValue(AttributeFqn, Datatype)} is called
		 * 
		 * @param attributeFQN
		 *            attribute GUID (global ID = Category,Issuer,AttributeId)
		 * @param value
		 *            attribute value bag; null if the request named attribute was undefined in this context
		 */
		<AV extends AttributeValue> void namedAttributeValueConsumed(AttributeFqn attributeFQN, AttributeBag<AV> value);

		/**
		 * To be called when {@link EvaluationContext#putAttributeSelectorResultIfAbsent(AttributeSelectorExpression, Bag)} is called iff the result was not available in the context yet (i.e. first
		 * time it is produced in the request context)
		 * 
		 * @param attributeSelector
		 *            attribute selector
		 * @param contextSelectorBag
		 *            (optional) xPath-expression value of attribute referenced by ContextSelectorId if present
		 * @param result
		 *            evaluation result
		 */
		<AV extends AttributeValue> void attributeSelectorResultProduced(AttributeSelectorExpression<AV> attributeSelector, Optional<AttributeBag<XPathValue>> contextSelectorBag, Bag<AV> result);

		/**
		 * To be called when {@link EvaluationContext#getAttributeSelectorResult(AttributeSelectorExpression)} is called
		 * 
		 * @param attributeSelector
		 *            attribute selector
		 * @param result
		 *            evaluation result
		 */
		<AV extends AttributeValue> void attributeSelectorResultConsumed(AttributeSelectorExpression<AV> attributeSelector, Bag<AV> result);
	}

	/**
	 * Returns the value of a named attribute available in the request context. Used to evaluate {@link AttributeDesignatorExpression}, ContextSelectorId of {@link AttributeSelectorExpression}, or to
	 * get values of attributes on which {@link NamedAttributeProvider}s depends to resolve their own attributes (e.g. some module may need attribute X, such as a subject ID, as input to resolve
	 * attribute Y from an external source, such as subject role from a user database).
	 * 
	 * @param attributeFQN
	 *            attribute GUID (global ID = Category,Issuer,AttributeId)
	 * @param datatype
	 *            attribute value datatype
	 * 
	 * @return attribute value(s), null iff attribute unknown (not set) in this context, empty if attribute known in this context but no value
	 * @throws IndeterminateEvaluationException
	 *             if error occurred trying to determine the attribute value(s) in context. This is different from finding without error that the attribute is not in the context (and/or no value),
	 *             e.g. if there is a result but type is different from {@code attributeDatatype}.
	 */
	<AV extends AttributeValue> AttributeBag<AV> getNamedAttributeValue(AttributeFqn attributeFQN, Datatype<AV> datatype) throws IndeterminateEvaluationException;

	/**
	 * Get immutable iterator over the context attributes. DO NOT ever use this method to retrieve one or more specific attributes, in which case you must use
	 * {@link #getNamedAttributeValue(AttributeFqn, Datatype)} instead. This is only for iterating over all the attributes, e.g. for debugging/auditing.
	 * 
	 * @return context attributes iterator (implementations must guarantee that the iterator is immutable, i.e. does not allow changing the internal context)
	 */
	Iterator<Entry<AttributeFqn, AttributeBag<?>>> getNamedAttributes();

	/**
	 * Put Attribute values in the context, only if the attribute is not already known to this context. Indeed, an attribute value cannot be overridden once it is set in the context to comply with
	 * 7.3.5 Attribute retrieval: "Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated
	 * in the context before it is first tested, and is thereafter immutable during evaluation." Therefore, {@link #getNamedAttributeValue(AttributeFqn, Datatype)} should be called always before
	 * calling this, for the same {@code attributeFQN}
	 * 
	 * @param attributeFQN
	 *            attribute's global ID
	 * @param result
	 *            attribute values
	 * @return false iff there is already a matching value in this context (this operation did NOT succeed)
	 */
	boolean putNamedAttributeValueIfAbsent(AttributeFqn attributeFQN, AttributeBag<?> result);

	/**
	 * Returns available context evaluation result for a given AttributeSelector. This feature is optional. Any implementation that does not implement this method may throw
	 * {@link UnsupportedOperationException} .
	 * 
	 * @param attributeSelector
	 *            AttributeSelector
	 * @return attribute value(s), null iff AttributeSelector's bag of values unknown (not set) in this context because not evaluated yet; empty if it was evaluated in this context but not result,
	 *         i.e. bag is empty
	 * @throws IndeterminateEvaluationException
	 *             if error occurred trying to determine the result in context. This is different from finding without error that the result is not in the context (and/or no value), e.g. if there is a
	 *             result but type is different from {@code datatypeClass}.
	 */
	<AV extends AttributeValue> Bag<AV> getAttributeSelectorResult(AttributeSelectorExpression<AV> attributeSelector) throws IndeterminateEvaluationException;

	/**
	 * Put an Attribute Selector's values in the context, only if the AttributeSelector has not been already evaluated in this context. Therefore
	 * {@link #getAttributeSelectorResult(AttributeSelectorExpression)} should be called always before calling this, for the same {@code attributeSelector}
	 * 
	 * @param attributeSelector
	 *            AttributeSelector
	 * @param result
	 *            AttributeSelector value bag
	 * @return false iff there is already a matching value in this context (this operation could NOT succeed)
	 * @throws IndeterminateEvaluationException
	 *             if AttributeSelector evaluation is not supported (this is an optional feature of XACML specification)
	 */
	<AV extends AttributeValue> boolean putAttributeSelectorResultIfAbsent(AttributeSelectorExpression<AV> attributeSelector, Bag<AV> result) throws IndeterminateEvaluationException;

	/**
	 * Returns the {@literal <Content>} of the {@literal <Attibutes>} identified by a given category, to be used for AttributeSelector evaluation.
	 * 
	 * @param category
	 *            category of the Attributes element from which to get the Content.
	 * 
	 * @return the resulting Content node, or null if none in the request Attributes category
	 */

	XdmNode getAttributesContent(String category);

	/**
	 * Get value of a VariableDefinition's expression evaluated in this context and whose value has been cached with {@link #putVariableIfAbsent(String, Value)} . To be used when evaluating
	 * VariableReferences.
	 * 
	 * @param variableId
	 *            identifies the VariableDefinition
	 * @param datatype
	 *            datatype
	 * @return value of the evaluated VariableDefinition's expression, or null if not evaluated (yet) in this context
	 * @throws IndeterminateEvaluationException
	 *             if actual datatype of variable value in context does not match expected {@code datatype}
	 */
	<V extends Value> V getVariableValue(String variableId, Datatype<V> datatype) throws IndeterminateEvaluationException;

	/**
	 * Caches the value of a VariableDefinition's expression evaluated in this context only if variable is not already set in this context, for later retrieval by
	 * {@link #getVariableValue(String, Datatype)} when evaluating ValueReferences to the same VariableId.
	 * <p>
	 * The variable is set only if it was absent from context. In other words, this method does/must not allow setting the same variable twice. The reason is compliance with XACML spec 7.8
	 * VariableReference evaluation: "the value of an Expression element remains the same for the entire policy evaluation."
	 * </p>
	 * 
	 * @param variableId
	 *            identifies the VariableDefinition
	 * @param value
	 *            value of the VariableDefinition's expression evaluated in this context
	 * @return false iff there is already a value for this variable in context (this operation could NOT succeed).
	 */
	boolean putVariableIfAbsent(String variableId, Value value);

	/**
	 * Removes a variable (defined by VariableDefinition) from this context.
	 * 
	 * @param variableId
	 *            identifies the Variable to remove
	 * @return the value of the variable before removal, or null if there was no such variable set in this context.
	 */
	Value removeVariable(String variableId);

	/**
	 * Get custom property
	 * 
	 * @see java.util.Map#get(Object)
	 * @param key
	 * @return property
	 */
	Object getOther(String key);

	/**
	 * Check whether custom property is in the context
	 * 
	 * @see java.util.Map#containsKey(Object)
	 * @param key
	 * @return true if and only if key exists in updatable property keys
	 */
	boolean containsKey(String key);

	/**
	 * Puts custom property in the context
	 * 
	 * @see java.util.Map#put(Object, Object)
	 * @param key
	 * @param val
	 */
	void putOther(String key, Object val);

	/**
	 * Removes custom property from the context
	 * 
	 * @see java.util.Map#remove(Object)
	 * @param key
	 * @return the previous value associated with key, or null if there was no mapping for key.
	 */
	Object remove(String key);

	/**
	 * Equivalent of XACML Request ReturnPolicyIdList attribute. XACML §5.4.2: "This attribute is used to request that the PDP return a list of all fully applicable policies and policy sets which were
	 * used in the decision as a part of the decision response." For a more precise definition of "applicable" in this context, see {@link DecisionResult#getApplicablePolicies()}.
	 * 
	 * @return true iff original XACML Request's ReturnPolicyIdList=true
	 */
	boolean isApplicablePolicyIdListRequested();

	/**
	 * Registers a listener on this evaluation context
	 * 
	 * @param listenerType
	 *            listener type used as key for retrieving the listener with {@link #getListener(Class)}
	 * @param listener
	 * @return the listener previously associated with this class (possibly null), or null if there was no previous entry.
	 */
	<L extends Listener> L putListener(Class<L> listenerType, L listener);

	/**
	 * Returns the listener the specified class is mapped to. This will only return a value that was bound to this specific class, not a value that may have been bound to a subtype.
	 * 
	 * @param listenerType
	 *            listener type, used as key to retrieve the listener registered with this type with {@link #putListener(Class, Listener)}
	 * @return the listener associated with this class, or null if no entry for this class is present
	 */
	<L extends Listener> L getListener(Class<L> listenerType);

}

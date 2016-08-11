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
package org.ow2.authzforce.core.pdp.api;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

/**
 * Manages context for the policy evaluation of a given authorization decision
 * request. Typically, an instance of this is instantiated whenever the PDP gets
 * a request and needs to perform an evaluation to a authorization decision.
 * Such a context is used and possibly updated all along the evaluation of the
 * request.
 * 
 */
public interface EvaluationContext {

	/**
	 * Returns available context evaluation result for given
	 * AttributeDesignator.
	 * <p>
	 * WARNING: java.net.URI cannot be used here for XACML datatype/category/id,
	 * because not equivalent to XML schema anyURI type. Spaces are allowed in
	 * XSD anyURI [1], not in java.net.URI. [1]
	 * http://www.w3.org/TR/xmlschema-2/#anyURI
	 * </p>
	 * 
	 * @param attributeGUID
	 *            attribute GUID (global ID = Category,Issuer,AttributeId)
	 * @param attributeDatatype
	 *            attribute datatype
	 * 
	 * @return attribute value(s), null iff attribute unknown (not set) in this
	 *         context, empty if attribute known in this context but no value
	 * @throws IndeterminateEvaluationException
	 *             if error occurred trying to determine the attribute value(s)
	 *             in context. This is different from finding without error that
	 *             the attribute is not in the context (and/or no value), e.g.
	 *             if there is a result but type is different from
	 *             {@code datatypeClass}.
	 */
	<AV extends AttributeValue> Bag<AV> getAttributeDesignatorResult(AttributeGUID attributeGUID,
			Datatype<AV> attributeDatatype) throws IndeterminateEvaluationException;

	/**
	 * Get immutable iterator over the context attributes. DO NOT ever use this
	 * method to retrieve one or more specific attributes, in which case you
	 * must use {@link #getAttributeDesignatorResult(AttributeGUID, Datatype)}
	 * instead. This is only for iterating over all the attributes, e.g. for
	 * debugging/auditing.
	 * 
	 * @return context attributes iterator (implementations must guarantee that
	 *         the iterator is immutable, i.e. does not allow changing the
	 *         internal context)
	 */
	Iterator<Entry<AttributeGUID, Bag<?>>> getAttributes();

	/**
	 * Put Attribute values in the context, only if the attribute is not already
	 * known to this context. Indeed, an attribute value cannot be overridden
	 * once it is set in the context to comply with 7.3.5 Attribute retrieval:
	 * "Regardless of any dynamic modifications of the request context during
	 * policy evaluation, the PDP SHALL behave as if each bag of attribute
	 * values is fully populated in the context before it is first tested, and
	 * is thereafter immutable during evaluation." Therefore,
	 * {@link #getAttributeDesignatorResult(AttributeGUID, Datatype)} should be
	 * called always before calling this, for the same {@code attributeGUID}
	 * 
	 * @param attributeGUID
	 *            attribute's global ID
	 * @param result
	 *            attribute values
	 * @return false iff there is already a matching value in this context (this
	 *         operation did NOT succeed)
	 */
	boolean putAttributeDesignatorResultIfAbsent(AttributeGUID attributeGUID, Bag<?> result);

	/**
	 * Returns available context evaluation result for a given
	 * AttributeSelector. This feature is optional. Any implementation that does
	 * not implement this method may throw {@link UnsupportedOperationException}
	 * .
	 * <p>
	 * WARNING: java.net.URI cannot be used here for XACML
	 * datatype/category/contextSelectorId, because not equivalent to XML schema
	 * anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
	 * [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 * </p>
	 * 
	 * @param attributeSelectorId
	 *            AttributeSelector ID
	 * @param attributeDatatype
	 *            expected datatype (type of each element in the result bag)
	 * @return attribute value(s), null iff AttributeSelector's bag of values
	 *         unknown (not set) in this context because not evaluated yet;
	 *         empty if it was evaluated in this context but not result, i.e.
	 *         bag is empty
	 * @throws IndeterminateEvaluationException
	 *             if error occurred trying to determine the result in context.
	 *             This is different from finding without error that the result
	 *             is not in the context (and/or no value), e.g. if there is a
	 *             result but type is different from {@code datatypeClass}.
	 */
	<AV extends AttributeValue> Bag<AV> getAttributeSelectorResult(AttributeSelectorId attributeSelectorId,
			Datatype<AV> attributeDatatype) throws IndeterminateEvaluationException;

	/**
	 * Put an Attribute Selector's values in the context, only if the
	 * AttributeSelector has not been already evaluated in this context.
	 * Therefore
	 * {@link #getAttributeSelectorResult(AttributeSelectorId, Datatype)} should
	 * be called always before calling this, for the same
	 * {@code attributeSelectorId}
	 * 
	 * @param attributeSelectorId
	 *            AttributeSelector ID
	 * @param result
	 *            AttributeSelector value bag
	 * @return false iff there is already a matching value in this context (this
	 *         operation could NOT succeed)
	 * @throws IndeterminateEvaluationException
	 *             if AttributeSelector evaluation is not supported (this is an
	 *             optional feature of XACML specification)
	 */
	boolean putAttributeSelectorResultIfAbsent(AttributeSelectorId attributeSelectorId, Bag<?> result)
			throws IndeterminateEvaluationException;

	/**
	 * Returns the {@literal<Content>} of the {@literal<Attibutes>} identified
	 * by a given category, to be used for AttributeSelector evaluation.
	 * 
	 * @param category
	 *            category of the Attributes element from which to get the
	 *            Content.
	 * 
	 * @return the resulting Content node, or null if none in the request
	 *         Attributes category
	 */

	XdmNode getAttributesContent(String category);

	/**
	 * Get value of a VariableDefinition's expression evaluated in this context
	 * and whose value has been cached with
	 * {@link #putVariableIfAbsent(String, Value)} . To be used when evaluating
	 * VariableReferences.
	 * 
	 * @param variableId
	 *            identifies the VariableDefinition
	 * @param datatype
	 *            datatype
	 * @return value of the evaluated VariableDefinition's expression, or null
	 *         if not evaluated (yet) in this context
	 * @throws IndeterminateEvaluationException
	 *             if actual datatype of variable value in context does not
	 *             match expected {@code datatype}
	 */
	<V extends Value> V getVariableValue(String variableId, Datatype<V> datatype)
			throws IndeterminateEvaluationException;

	/**
	 * Caches the value of a VariableDefinition's expression evaluated in this
	 * context only if variable is not already set in this context, for later
	 * retrieval by {@link #getVariableValue(String, Datatype)} when evaluating
	 * ValueReferences to the same VariableId.
	 * <p>
	 * The variable is set only if it was absent from context. In other words,
	 * this method does/must not allow setting the same variable twice. The
	 * reason is compliance with XACML spec 7.8 VariableReference evaluation:
	 * "the value of an Expression element remains the same for the entire policy evaluation."
	 * </p>
	 * 
	 * @param variableId
	 *            identifies the VariableDefinition
	 * @param value
	 *            value of the VariableDefinition's expression evaluated in this
	 *            context
	 * @return false iff there is already a value for this variable in context
	 *         (this operation could NOT succeed).
	 */
	boolean putVariableIfAbsent(String variableId, Value value);

	/**
	 * Removes a variable (defined by VariableDefinition) from this context.
	 * 
	 * @param variableId
	 *            identifies the Variable to remove
	 * @return the value of the variable before removal, or null if there was no
	 *         such variable set in this context.
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
	 * @return the previous value associated with key, or null if there was no
	 *         mapping for key.
	 */
	Object remove(String key);

	/**
	 * Add reference of policy found applicable during evaluation
	 * 
	 * @param policyRef
	 *            applicable policy reference
	 * 
	 * @return true iff the policy is already in the list of applicable
	 *         policies. NB: this requires that implementations maintain a list
	 *         of those applicable policies. It may not be the case if this was
	 *         not requested in the XACML Request, i.e.
	 *         {@code ReturnPolicyIdList = false}, in which case it is always
	 *         false.
	 */
	boolean addApplicablePolicy(JAXBElement<IdReferenceType> policyRef);

	/**
	 * Get an immutable list of the policy references added via
	 * {@link #addApplicablePolicy(JAXBElement)}
	 * 
	 * @return applicable policies
	 */
	List<JAXBElement<IdReferenceType>> getApplicablePolicies();

	/**
	 * Get an immutable set of the identifiers of the named attributes actually
	 * used during evaluation, i.e. for which
	 * {@link #getAttributeDesignatorResult(AttributeGUID, Datatype)} was
	 * called.
	 * <p>
	 * NB: this is different from the attributes identified in
	 * {@link #getAttributes()} since these include all original Request
	 * attributes, some of which may not be used actually during the policy
	 * evaluation (e.g. if no AttributeDesignator matches, or the evaluation
	 * returns before reaching any matching AttributeDesignator). Therefore,
	 * there may be as much or less attributes in the
	 * 
	 * @return the list of named attributes used during evaluation
	 */
	Set<AttributeGUID> getUsedNamedAttributes();

	/**
	 * Get an immutable set of the identifiers of the Attributes/Content parts
	 * actually used during evaluation, i.e. for which
	 * {@link #getAttributeSelectorResult(AttributeSelectorId, Datatype)} was
	 * called
	 * 
	 * @return the list of Attributes/Content(s) used during evaluation
	 */
	Set<AttributeSelectorId> getUsedExtraAttributeContents();

}

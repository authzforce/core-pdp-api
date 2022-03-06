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

import com.google.common.collect.ImmutableList;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.QName;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

import java.util.Deque;
import java.util.Optional;

/**
 * Expression factory for parsing XACML {@link ExpressionType}s in policies: AttributeDesignator, AttributeSelector, Apply, etc.
 */
public interface ExpressionFactory
{
	/**
	 * XPath support status
	 * @return true iff XPath support enabled, else it is disabled
	 */
	boolean isXPathEnabled();

	/**
	 * Parses an XACML Expression into internal model of expression (evaluable).
	 * 
	 * @param expr
	 *            the JAXB ExpressionType derived from XACML model
	 * @param xPathCompiler
	 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default XPath version specified in {@link DefaultsType} element. Undefined if no such XPath version defined or XPath support disabled ({@link #isXPathEnabled()} returns false).
	 * @param longestVarRefChain
	 *            Longest chain of VariableReference references in the VariableDefinition's expression that is <code>expr</code> or contains <code>expr</code>, or null if <code>expr</code> is not in a
	 *            VariableDefinition. A VariableReference reference chain is a list of VariableIds, such that V1-> V2 ->... -> Vn -> <code>expr</code> , where "V1 -> V2" means: the expression in
	 *            VariableDefinition of V1 has a VariableReference to V2. This is used to detect exceeding depth of VariableReference reference in VariableDefinitions' expressions. Again,
	 *            <code>longestVarRefChain</code> may be null, if this expression is not used in a VariableDefinition.
	 * @return an <code>Expression</code> or null if the root node cannot be parsed as a valid Expression
	 * @throws IllegalArgumentException
	 *             invalid ExpressionType
	 */
	Expression<?> getInstance(ExpressionType expr, Deque<String> longestVarRefChain, Optional<XPathCompilerProxy> xPathCompiler) throws IllegalArgumentException;

	/**
	 * Parse/create an attribute value expression from XACML-schema-derived JAXB model
	 * 
	 * @param jaxbAttrVal
	 *            XACML-schema-derived JAXB AttributeValue
	 * @param xPathCompiler
	 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default XPath version specified in {@link DefaultsType} element. Undefined if no such XPath version defined or XPath support disabled ({@link #isXPathEnabled()} returns false).
	 * @return attribute value Expression
	 * @throws IllegalArgumentException
	 *             if value cannot be parsed into the value's defined datatype
	 */
	ConstantExpression<? extends AttributeValue> getInstance(AttributeValueType jaxbAttrVal, Optional<XPathCompilerProxy> xPathCompiler) throws IllegalArgumentException;

	/**
	 * Add VariableDefinition (variable assignment expression)
	 * 
	 * @param varDef
	 *            VariableDefinition
	 * @param xPathCompiler
	 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default XPath version specified in {@link DefaultsType} element. Undefined if no such XPath version defined or XPath support disabled ({@link #isXPathEnabled()} returns false).
	 * @param longestVarRefChain
	 *            Ignored if null else used as inout parameter to be filled by this method with the longest chain of VariableReference references in <code>varDef</code>. A VariableReference reference
	 *            chain is a list of VariableIds, such that V1-> V2 ->... -> Vn -> <code>expr</code> , where "V1 -> V2" means: the expression in VariableDefinition of V1 has a VariableReference to V2.
	 *            This is used to detect exceeding depth of VariableReference reference in VariableDefinitions' expressions. Again, <code>longestVarRefChain</code> may be null, if this expression is
	 *            not used in a VariableDefinition.
	 * @return The previous VariableReference if VariableId already used
	 * @throws IllegalArgumentException
	 *             invalid expression in {@code varDef}
	 */
	VariableReference<?> addVariable(VariableDefinition varDef, Deque<String> longestVarRefChain, Optional<XPathCompilerProxy> xPathCompiler) throws IllegalArgumentException;

	/**
	 * Get a given variable's assignment expression (definition)
	 * 
	 * @param varId variable identifier
	 * @return the VariableReference identified by <code>varId</code> , or null if there is no such variable.
	 */
	VariableReference<?> getVariableExpression(String varId);

	/**
	 * Get a snapshot list of current Variable definitions. Order matters as a variable X must be declared in the list after any variable that it depends on.
	 *
	 * One use for this is to be able to declare all possible variables that may occur in a AttributeSelector's XPath expression, therefore all need to be declared on the XPath compiler with {@link net.sf.saxon.s9api.XPathCompiler#declareVariable(QName, ItemType, OccurrenceIndicator)}.
	 *
	 * @return a new copy of the current list of Variable definitions, empty if there is no such declared variable.
	 */
	ImmutableList<VariableReference<?>> getVariableExpressions();

	/**
	 * Removes the VariableReference(Definition) from the manager
	 * 
	 * @param varId variable identifier
	 * @return the VariableReference previously identified by <code>varId</code> , or null if there was no such variable.
	 */
	VariableReference<?> removeVariable(String varId);

	/**
	 * Gets a non-generic function instance
	 * 
	 * @param functionId
	 *            function ID (XACML URI)
	 * @return function instance; or null if no such function with ID {@code functionId}
	 * 
	 */
	FunctionExpression getFunction(String functionId);

	/**
	 * Gets a function instance (generic or non-generic).
	 * 
	 * @param functionId
	 *            function ID (XACML URI)
	 * @param subFunctionReturnType
	 *            optional sub-function's return type required only if a generic higher-order function is expected as the result, of which the sub-function is expected to be the first parameter;
	 *            otherwise null (for first-order function). A generic higher-order function is a function whose return type depends on the sub-function ('s return type). Note: we only support
	 *            sub-functions with primitive return type here, for simplicity (see {@link org.ow2.authzforce.core.pdp.api.func.HigherOrderBagFunction} for more info).
	 * @return function instance; or null if no such function with ID {@code functionId}, or if non-null {@code subFunctionReturnTypeId} specified and no higher-order function compatible with
	 *         sub-function's return type {@code subFunctionReturnTypeId}
	 * @throws IllegalArgumentException
	 *             if datatype {@code subFunctionReturnType} is not supported
	 * 
	 */
	FunctionExpression getFunction(String functionId, Datatype<? extends AttributeValue> subFunctionReturnType) throws IllegalArgumentException;
}
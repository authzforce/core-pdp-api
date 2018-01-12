/**
 * Copyright 2012-2018 Thales Services SAS.
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

import java.io.Closeable;
import java.util.Deque;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition;

import org.ow2.authzforce.core.pdp.api.DesignatedAttributeProvider;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

/**
 * Expression factory for parsing XACML {@link ExpressionType}s in policies: AttributeDesignator, AttributeSelector, Apply, etc.
 * <p>
 * Extends {@link Closeable} because it may use {@link DesignatedAttributeProvider}s (implement {@link Closeable}) to resolve AttributeDesignators for attributes not provided in the request.
 */
public interface ExpressionFactory extends Closeable
{

	/**
	 * Parses an XACML Expression into internal model of expression (evaluable).
	 * 
	 * @param expr
	 *            the JAXB ExpressionType derived from XACML model
	 * @param xPathCompiler
	 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default XPath version specified in {@link DefaultsType} element; null if none specified
	 * @param longestVarRefChain
	 *            Longest chain of VariableReference references in the VariableDefinition's expression that is <code>expr</code> or contains <code>expr</code>, or null if <code>expr</code> is not in a
	 *            VariableDefinition. A VariableReference reference chain is a list of VariableIds, such that V1-> V2 ->... -> Vn -> <code>expr</code> , where "V1 -> V2" means: the expression in
	 *            VariableDefinition of V1 has a VariableReference to V2. This is used to detect exceeding depth of VariableReference reference in VariableDefinitions' expressions. Again,
	 *            <code>longestVarRefChain</code> may be null, if this expression is not used in a VariableDefinition.
	 * @return an <code>Expression</code> or null if the root node cannot be parsed as a valid Expression
	 * @throws IllegalArgumentException
	 *             invalid ExpressionType
	 */
	Expression<?> getInstance(ExpressionType expr, XPathCompiler xPathCompiler, Deque<String> longestVarRefChain) throws IllegalArgumentException;

	/**
	 * Parse/create an attribute value expression from XACML-schema-derived JAXB model
	 * 
	 * @param jaxbAttrVal
	 *            XACML-schema-derived JAXB AttributeValue
	 * @param xPathCompiler
	 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default XPath version specified in {@link DefaultsType} element; null if none specified
	 * @return attribute value Expression
	 * @throws IllegalArgumentException
	 *             if value cannot be parsed into the value's defined datatype
	 */
	ConstantExpression<? extends AttributeValue> getInstance(AttributeValueType jaxbAttrVal, XPathCompiler xPathCompiler) throws IllegalArgumentException;

	/**
	 * Add VariableDefinition to be managed
	 * 
	 * @param varDef
	 *            VariableDefinition
	 * @param xPathCompiler
	 *            Policy(Set) default XPath compiler, corresponding to the Policy(Set)'s default XPath version specified in {@link DefaultsType} element.
	 * @param longestVarRefChain
	 *            Ignored if null else used as inout parameter to be filled by this method with the longest chain of VariableReference references in <code>varDef</code>. A VariableReference reference
	 *            chain is a list of VariableIds, such that V1-> V2 ->... -> Vn -> <code>expr</code> , where "V1 -> V2" means: the expression in VariableDefinition of V1 has a VariableReference to V2.
	 *            This is used to detect exceeding depth of VariableReference reference in VariableDefinitions' expressions. Again, <code>longestVarRefChain</code> may be null, if this expression is
	 *            not used in a VariableDefinition.
	 * @return The previous VariableReference if VariableId already used
	 * @throws IllegalArgumentException
	 *             invalid expression in {@code varDef}
	 */
	VariableReference<?> addVariable(VariableDefinition varDef, XPathCompiler xPathCompiler, Deque<String> longestVarRefChain) throws IllegalArgumentException;

	/**
	 * Removes the VariableReference(Definition) from the manager
	 * 
	 * @param varId
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
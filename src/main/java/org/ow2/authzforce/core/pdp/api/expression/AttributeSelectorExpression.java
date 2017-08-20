/**
 * Copyright 2012-2017 Thales Services SAS.
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

import java.util.Optional;

import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XdmNode;

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeSelectorId;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.XPathValue;

/**
 * AttributeSelector evaluator, which uses XPath expressions to resolve values from the Request or elsewhere. The AttributeSelector feature in optional in the XACML core specification.
 *
 * @param <AV>
 *            AttributeSelector evaluation results' primitive returnType
 * 
 * @version $Id: $
 */
public interface AttributeSelectorExpression<AV extends AttributeValue> extends Expression<Bag<AV>>
{
	/**
	 * Get AttributeSelector identifier (category, contextSelectorId, path)
	 * 
	 * @return attribute selector identifier
	 */
	AttributeSelectorId getAttributeSelectorId();

	/**
	 * Get the fully qualified identifier corresponding to ContextSelectorId if present
	 * 
	 * @return context selector identifier
	 */
	Optional<AttributeFqn> getContextSelectorFQN();

	/**
	 * Indicates whether the expression must evaluate to a non-empty bag. Equivalent of XACML MustBePresent flag.
	 * 
	 * @return true iff the resulting bag must be non-empty, i.e. there must be at least one value in the resulting bag
	 */
	boolean isNonEmptyBagRequired();

	/**
	 * Evaluates with input &lt;Content&gt; element and optional XPath to be used directly as if it was coming from the value of the Content element and attribute referenced by ContextSelectorId
	 * directly. Used for instance when XPath expressions are pre-compiled in cache, i.e. in decision cache extensions.
	 * 
	 * @param contentElement
	 *            XML element used as (replacement for) this AttributeSelector's &lt;Content&gt
	 * @param contextPathEvaluator
	 *            (optional) XPath expression evaluator used as replacement for the XPath expression usually given by the attribute referenced by this AttributeSelector's ContextSelectorId, if it is
	 *            defined
	 * @param context
	 *            evaluation context
	 * @return value of this in the context of the arguments
	 * @throws IndeterminateEvaluationException
	 *             if evaluation is "Indeterminate" (some error occurred during evaluation, see XACML core specification)
	 */
	Bag<AV> evaluate(XdmNode contentElement, Optional<XPathExecutable> contextPathEvaluator, EvaluationContext context) throws IndeterminateEvaluationException;

	/**
	 * Create XPath evaluator from xPathExpression (XACML datatype) value in a bag, using this AttributeSelector's internal XPath compiler, so that it may be reused later as second argument to
	 * {@link #evaluate(XdmNode, Optional, EvaluationContext)}.
	 * 
	 * @param xpathExpressionBag
	 *            bag expected to contain a single value of xPathExpression datatype (XACML)
	 * 
	 * @return XPathExecutable based on the single XPath expression value in {@code  xpathExpressionBag}
	 * @throws IllegalArgumentException
	 *             iff {@code xpathExpressionBag} is null or empty or its single value is not a valid XPath expression
	 */
	XPathExecutable getXPath(Bag<XPathValue> xpathExpressionBag) throws IllegalArgumentException;

}
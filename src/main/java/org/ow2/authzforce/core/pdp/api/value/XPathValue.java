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
package org.ow2.authzforce.core.pdp.api.value;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ImmutableXacmlStatus;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XPathEvaluator;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import com.google.common.collect.ImmutableMap;

import net.sf.saxon.lib.StandardURIChecker;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

/**
 * Representation of XACML xpathExpression datatype. All objects of this class are immutable and all methods of the class are thread-safe.
 * <p>
 * XACML 3.0 Core spec, §A.3.15: "An XPath expression evaluates to a node-set, which is a set of XML nodes that match the expression. A node or node-set is not in the formal data-type system of XACML.
 * All comparison or other operations on node-sets are performed in isolation of the particular [XPATH-based] function specified. The context nodes and namespace mappings of the XPath expressions are
 * defined by the XPath data-type, see section B.3."
 * <p>
 * In short, the xpathExpression is evaluated in the context of calling XPath-based functions on a given evaluation context only. These functions typically use {@link #evaluate(EvaluationContext)} to
 * get the matching node-set.
 * <p>
 * WARNING: this class is not optimized for request-time evaluation but for policy initialization-time. Therefore, its use is not recommended for evaluating xpathExpressions in XACML Request. We
 * consider it not useful in the latter case, as the Requester (PEP) could evaluate the xpathExpressions in the first place, and does not need the PDP to do it.
 * <p>
 * NB (considerations for developers): the standard datatype 'xpathExpression' may seem like a special case because xpathExpression evaluation depends on the context; therefore it might seem like a
 * good idea to have 'xpathExpression' be implemented as a subclass of {@link org.ow2.authzforce.core.pdp.api.expression.Expression} instead. However, we prefer to avoid that for simplicity. Indeed,
 * if we need to evaluate a 'xpathExpression', in standard XACML, xpathExpressions are used only as parameters of XPath-based functions (A.3.15), and such functions just need to cast input values to
 * this {@link XPathValue} class and call {@link #evaluate(EvaluationContext)} for evaluation. Outside the context of XPath-based functions, we may consider xpathExpressions as simple literal
 * constants like other AttributeValues.
 * 
 * @version $Id: $
 */
public final class XPathValue extends SimpleValue<String>
{
	/**
	 * XML attribute local name that indicate the XACML attribute category of the Content to which the xpathExpression is applied: {@value} .
	 */
	public static final String XPATH_CATEGORY_ATTRIBUTE_LOCALNAME = "XPathCategory";

	/**
	 * QName of XPathCategory attribute in xpathExpression, using {@value #XPATH_CATEGORY_ATTRIBUTE_LOCALNAME} as local name. This is allowed by XACML schema as part of:
	 * 
	 * <pre>
	 * {@code
	 * <xs:anyAttribute namespace="##any" processContents="lax"/>
	 * }
	 * </pre>
	 * 
	 * ... therefore namespace returned by JAXB is empty "". More info: https://jaxb.java.net/tutorial/section_6_2_7_5 -Collecting-Unspecified-Attributes-XmlAnyAttribute
	 * .html#Collecting%20Unspecified%20Attributes:%20XmlAnyAttribute
	 */
	public static final QName XPATH_CATEGORY_ATTRIBUTE_QNAME = new QName("", XPATH_CATEGORY_ATTRIBUTE_LOCALNAME);

	private static final IllegalArgumentException NULL_XPATH_CATEGORY_EXCEPTION = new IllegalArgumentException("Undefined XPathCategory for XPath expression value");
	private static final IllegalArgumentException NULL_XPATH_COMPILER_EXCEPTION = new IllegalArgumentException(
	        "Undefined XPath version/compiler (possibly missing RequestDefaults/PolicyDefaults element)");

	private final String xpathCategory;

	/*
	 * Forced to be transient and non-final to comply with Serializable contract (inherits from JAXB AttributeValueType) but XPathEvaluator is not natively Serializable, therefore must be deserialized
	 * with readObject() which cannot assign final variable. Therefore, developers must make sure this is only assigned by readObject() or constructors once and for all.
	 */
	private final transient XPathEvaluator xpathEvaluator;

	private final IndeterminateEvaluationException missingAttributesContentException;

	private final ImmutableXacmlStatus xpathEvalExceptionStatus;

	private final IndeterminateEvaluationException missingContextException;

	private transient volatile int hashCode = 0; // Effective Java - Item 9
	private transient volatile ImmutableMap<QName, String> extraXmlAtts = null;

	/**
	 * Instantiates from XPath expression.
	 *
	 * @param xpath
	 *            XPath
	 * @param otherXmlAttributes
	 *            other XML attributes on the xpathExpression AttributeValue node, one of which is expected to be the attribute {@value #XPATH_CATEGORY_ATTRIBUTE_LOCALNAME}
	 * @param xPathCompiler
	 *            XPath compiler for compiling/evaluating {@code xpath}
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code value} is not a valid string representation for this value datatype or {code otherXmlAttributes == null} or {code otherXmlAttributes} does not contain any
	 *             {@value #XPATH_CATEGORY_ATTRIBUTE_LOCALNAME} attribute
	 */
	public XPathValue(final String xpath, final Map<QName, String> otherXmlAttributes, final XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		super(xpath);
		Objects.requireNonNull(otherXmlAttributes, "Undefined XML attributes (expected: " + XPATH_CATEGORY_ATTRIBUTE_QNAME + ")");
		this.xpathCategory = otherXmlAttributes.get(XPATH_CATEGORY_ATTRIBUTE_QNAME);
		if (xpathCategory == null)
		{
			throw NULL_XPATH_CATEGORY_EXCEPTION;
		}

		if (xPathCompiler == null)
		{
			throw NULL_XPATH_COMPILER_EXCEPTION;
		}

		this.xpathEvaluator = new XPathEvaluator(xpath, xPathCompiler);
		/*
		 * Please note that StandardURIChecker maintains a thread-local cache of validated URIs (cache size is 50 and eviction policy is LRU)
		 */
		if (!StandardURIChecker.getInstance().isValidURI(xpathCategory))
		{
			throw new IllegalArgumentException("Invalid value for XPathCategory (xs:anyURI): " + xpathCategory);
		}

		this.missingAttributesContentException = new IndeterminateEvaluationException(this + ": No <Content> element found in Attributes of Category=" + xpathCategory, XacmlStatusCode.SYNTAX_ERROR.value());
		this.xpathEvalExceptionStatus = new ImmutableXacmlStatus(XacmlStatusCode.SYNTAX_ERROR.value(), Optional.of(this + ": Error evaluating XPath against XML node from Content of Attributes Category='" + xpathCategory + "'"));
		this.missingContextException = new IndeterminateEvaluationException(new ImmutableXacmlStatus(XacmlStatusCode.PROCESSING_ERROR.value(), Optional.of(this + ":  undefined evaluation context: XPath value cannot be evaluated")));
	}

	/**
	 * Convenient method to get the XML nodes ("node-set") matching the XPath expression from the Content node of the XACML Attributes element with category <i>XPathCategory</i> in this
	 * {@code context}. <i>XPathCategory</i> is extracted from the attribute of the same name in {@code otherXmlAttributes} argument passed to {@link #XPathValue(String, Map, XPathCompiler)} when
	 * creating this instance. To be used by XPath-based functions defined in section A.3.15 of XACML 3.0 Core specification.
	 *
	 * @param context
	 *            current evaluation context
	 * @return node-set
	 * @throws org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException
	 *             error evaluating the XPath expression
	 */
	public XdmValue evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
	{
		if (context == null)
		{
			throw this.missingContextException;
		}

		final XdmNode contentNode = context.getAttributesContent(this.xpathCategory);
		if (contentNode == null)
		{
			throw this.missingAttributesContentException;
		}

		/*
		 * An XPathExecutable is immutable, and therefore thread-safe. It is simpler to load a new XPathSelector each time the expression is to be evaluated. However, the XPathSelector is serially
		 * reusable within a single thread. See Saxon Javadoc.
		 */
		final XPathSelector xpathSelector = xpathEvaluator.load();
		try
		{
			xpathSelector.setContextItem(contentNode);
			return xpathSelector.evaluate();
		} catch (final SaxonApiException e)
		{
			throw new IndeterminateEvaluationException(this.xpathEvalExceptionStatus, e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			// hash regardless of letter case
			hashCode = Objects.hash(xpathCategory, value);
		}

		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof XPathValue))
		{
			return false;
		}

		final XPathValue other = (XPathValue) obj;
		return this.xpathCategory.equals(other.xpathCategory) && this.value.equals(other.value);
	}

	/** {@inheritDoc} */
	@Override
	public String printXML()
	{
		return this.value;
	}

	@Override
	public Map<QName, String> getXmlAttributes()
	{
		if (this.extraXmlAtts == null)
		{
			this.extraXmlAtts = ImmutableMap.of(XPATH_CATEGORY_ATTRIBUTE_QNAME, this.xpathCategory);
		}

		return this.extraXmlAtts;
	}
}

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

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


import javax.xml.namespace.QName;

import com.google.common.base.Preconditions;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.XPathCompiler;

/**
 * Superclass of all "simple" Attribute Values, including values of any XACML standard datatype; "simple" as in "simple type" or "simple content" of XML schema. This means the value can be represented
 * as character data only (String) with no sub-elements (no XML elements) - but with possibly extra XML attributes - as opposed to structured values that have sub-elements. In this definition, all
 * XACML core standard primitive types are "simple" types, and their corresponding Java classes extend this class.
 * 
 * @param <V>
 *            underlying Java value type
 * 
 * @version $Id: $
 */
public abstract class SimpleValue<V> implements AttributeValue
{

	/**
	 * Datatype-specific Attribute Value Factory that supports values based on single {@link Serializable} element (i.e. no mixed XML content) with extra XML attributes.
	 * 
	 * @param <AV>
	 *            type of attribute values created by this factory
	 */
	public static abstract class BaseFactory<AV extends AttributeValue> extends BaseAttributeValueFactory<AV>
	{
		private static final IllegalArgumentException MORE_THAN_ONE_ELEMENT_IN_XACML_ATTRIBUTE_VALUE_CONTENT_EXCEPTION = new IllegalArgumentException(
		        "Invalid primitive AttributeValueType: content has more than one element. Expected: empty or single String element ");

		/**
		 * Creates a datatype factory from the Java datatype implementation class and datatype identifier
		 */
		protected BaseFactory(final AttributeDatatype<AV> datatype)
		{
			super(datatype);
		}

		/**
		 * Get the list of input types supported by this factory, i.e. all types of values from which this factory can create AttributeValues
		 * 
		 * @return supported input types
		 */
		public abstract Set<Class<? extends Serializable>> getSupportedInputTypes();

		/**
		 * Creates IllegalArgumentException saying the type of input {@code value} is not valid for this factory
		 * 
		 * @param value
		 *            input value with invalid type
		 * @return IllegalArgumentException the created exception
		 */
		protected final IllegalArgumentException newInvalidInputTypeException(final Serializable value)
		{
			if (value == null)
			{
				throw new IllegalArgumentException(this + ": invalid input: null/empty. Expected one of: " + getSupportedInputTypes());
			}

			throw new IllegalArgumentException(this + ": invalid input type: " + value.getClass() + ". Expected one of: " + getSupportedInputTypes());
		}

		/**
		 * Creates attribute value from a singleton value and possibly extra XML attributes
		 * 
		 * @param input
		 *            input raw value, null if original content is empty (e.g. list of JAXB (mixed) content elements is empty)
		 * @param otherXmlAttributes
		 *            other XML attributes (mandatory); if always empty, use {@link StringContentOnlyValueFactory} instead
		 * @param xPathCompiler
		 *            (optional) XPath compiler for compiling any XPath expression in the value, e.g. xpathExpression datatype
		 * @return instance of {@code F_AV}
		 */
		public abstract AV getInstance(Serializable input, Map<QName, String> otherXmlAttributes, XPathCompiler xPathCompiler);

		/**
		 * Creates an instance of {@code F_AV} from a XACML AttributeValue-originating content (e.g. {@code jaxbAttrVal.getContent()}) expected to be a singleton value (valid for this factory's
		 * supported {@code datatype}) and possibly other XML attributes (if original input is XML); or no value at all.
		 * 
		 * @param content
		 *            XACML AttributeValue content, e.g. if original input is XML/JAXB, a singleton list with an item of one of the following types: {@link String}, {@link Element} (see
		 *            {@link javax.xml.bind.annotation.XmlMixed}); or if input is JSON, a single JSONObject, Number, Boolean, or String.
		 * @throws IllegalArgumentException
		 *             if {@code datatype == null} or if there is more than one element in {@code content}, or first element in {@code content} is not a valid string representation for this datatype
		 */
		@Override
		public final AV getInstance(final List<Serializable> content, final Map<QName, String> otherXmlAttributes, final XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			/*
			 * content may be null in case of XML/JAXB when the tag is empty, although it represents the empty string! E.g. <AttributeValue .../>.
			 */
			final Serializable content0;
			if (content == null)
			{
				content0 = "";
			}
			else
			{

				final Iterator<Serializable> contentIterator = content.iterator();
				if (!contentIterator.hasNext())
				{
					content0 = "";
				}
				else
				{
					content0 = contentIterator.next();
					if (contentIterator.hasNext())
					{
						throw MORE_THAN_ONE_ELEMENT_IN_XACML_ATTRIBUTE_VALUE_CONTENT_EXCEPTION;
					}
				}
			}

			return getInstance(content0, otherXmlAttributes, xPathCompiler);
		}

	}

	/*
	 * Make it final to prevent unexpected value change resulting from some function side effects
	 */
	protected final V value;

	// cached method results (because class is immutable)
	private transient volatile String toString = null; // Effective Java - Item 71
	private transient volatile int hashCode = 0; // Effective Java - Item 9 // Effective Java - Item 9
	private transient volatile ImmutableList<Serializable> xmlString = null;

	/**
	 * Constructor from Java type of value. A Serializable JAXB-compatible form of the value must be provided to be used directly as first value in {@link #getContent()} The super field 'content' is
	 * set to an empty list, but it does not matter, since {@link #getContent()} is overridden here to return a singleton list with {@code rawVal} as single value.
	 *
	 * @param rawVal
	 *            internal Java native value. MUST NOT be null.
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code rawVal == null}
	 */
	protected SimpleValue(final V rawVal) throws IllegalArgumentException
	{
		Preconditions.checkArgument(rawVal != null, "Undefined raw value");
		value = rawVal;
	}

	/**
	 * Returns the internal low-level Java value on which this AttributeValue is based off. This method is provided mostly for convenience, especially for low-level operations. However, you should not
	 * use it unless there is no other way. Prefer the high-level methods provided by the concrete {@link SimpleValue} implementation if you need to do operations on it.
	 *
	 * @return the value
	 */
	public final V getUnderlyingValue()
	{
		return value;
	}

	/**
	 * Converts the internal value (accessible via {@link #getUnderlyingValue()}) to a valid lexical representation for XML marshalling. Equivalent to the 'printMethod' in JAXB 'javaType' binding
	 * customizations. Implementations of this typically call {@link javax.xml.bind.DatatypeConverter}. This method is called by {@link #getContent()} and its result cached by the same method for later use.
	 * Therefore, no need to cache the result in the implementation.
	 *
	 * @return XML-valid lexical representation.
	 */
	public abstract String printXML();

	/** {@inheritDoc} */
	@Override
	public final List<Serializable> getContent()
	{
		if (xmlString == null)
		{
			xmlString = ImmutableList.of(printXML());
		}

		return xmlString;
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
			toString = getContent().get(0).toString();
		}
		return toString;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(value);
		}

		return hashCode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof SimpleValue))
		{
			return false;
		}

		final SimpleValue<?> other = (SimpleValue<?>) obj;
		return this.value.equals(other.value);
	}

}

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
package org.ow2.authzforce.core.pdp.api.value;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.w3c.dom.Element;

/**
 * <p>
 * Superclass of all "simple" Attribute Values, including values of any XACML standard datatype; "simple" as in "simple type" or "simple content" of XML schema. This means the value can be represented
 * as character data only (String) with no sub-elements (no XML elements) - but with possibly extra XML attributes - as opposed to structured values that have sub-elements. In this definition, all
 * XACML core standard primitive types are "simple" types, and their corresponding Java classes extend this class.
 * </p>
 * <p>
 * Following JAXB fields (inherited from superclass {@link AttributeValueType}) are immutable:
 * <ul>
 * <li>content (also accessible via {@link #getContent()} )</li>
 * <li>dataType (also accessible via {@link #getDataType()})</li>
 * <li>otherAttributes (accessible via {@link #getOtherAttributes()})</li>
 * </ul>
 * </p>
 * <p>
 * For reasons of optimizations and in order to be an immutable value, the {@code content} field (from superclass {@link AttributeValueType} is never set here, and setting it in implementations will
 * have no effect, since this class overrides {@link #getContent()} (with 'final' modifier) with its own value returned by {@link #printXML()}. Therefore, implementations customize the result of
 * {@link #getContent()} in implementing {@link #printXML()}. As JAXB marshalls the content by using the annotated the {@code content} field directly, as this is not set in this class, DO NOT use it
 * for marshalling. It is expected that the content is used only when marshalling AttributeAssignments (e.g. in XACML response), in which case the class responsible for creating the
 * AttributeAssignments MUST call {@link #getContent()} to get/marshall the actual content.
 * </p>
 *
 * @param <V>
 *            underlying Java value type
 * 
 * @version $Id: $
 */
public abstract class SimpleValue<V> extends AttributeValue
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final IllegalArgumentException UNDEF_ATTR_CONTENT_EXCEPTION = new IllegalArgumentException("Undefined attribute value");

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
			throw new IllegalArgumentException(this + ": invalid input type: " + value.getClass() + ". Expected one of: " + getSupportedInputTypes());
		}

		/**
		 * Creates attribute value from a singleton value and possibly extra XML attributes
		 * 
		 * @param input
		 *            input raw value, null if original content is empty (e.g. list of JAXB (mixed) content elements is empty)
		 * @param otherXmlAttributes
		 *            other XML attributes (mandatory); if always empty, use {@link SimpleValue.StringContentOnlyFactory} instead)
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
		 *            XACML AttributeValue content, e.g. if original input is XML/JAXB, a singleton list with an element of one of the following types: {@link String}, {@link Element}; or if input is
		 *            JSON, an single JSONObject, Number, Boolean, or String.
		 * @throws IllegalArgumentException
		 *             if {@code datatype == null || content == null} or if there is more than one element in {@code content}, or first element in {@code content} is not a valid string representation
		 *             for this datatype
		 */
		@Override
		public final AV getInstance(final List<Serializable> content, final Map<QName, String> otherXmlAttributes, final XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			if (content == null)
			{
				throw UNDEF_ATTR_CONTENT_EXCEPTION;
			}

			final Serializable content0;
			final Iterator<Serializable> contentIterator = content.iterator();
			if (!contentIterator.hasNext())
			{
				content0 = null;
			}
			else
			{
				content0 = contentIterator.next();
				if (contentIterator.hasNext())
				{
					throw MORE_THAN_ONE_ELEMENT_IN_XACML_ATTRIBUTE_VALUE_CONTENT_EXCEPTION;
				}
			}

			return getInstance(content0, otherXmlAttributes, xPathCompiler);
		}

	}

	/**
	 * Datatype-specific Attribute Value Factory that supports values based on string and possibly other type of {@link Serializable} content without any extra XML attributes.
	 * 
	 * @param <AV>
	 *            type of attribute values created by this factory
	 */
	public static abstract class StringParseableValueFactory<AV extends AttributeValue> extends BaseFactory<AV>
	{
		private static final IllegalArgumentException NON_NULL_OTHER_XML_ATTRIBUTES_ARG_EXCEPTION = new IllegalArgumentException(
				"Invalid value content: extra XML attributes are not supported by this primitive datatype, only string content.");

		/**
		 * Creates a datatype factory from the Java datatype implementation class and datatype identifier
		 */
		protected StringParseableValueFactory(final AttributeDatatype<AV> datatype)
		{
			super(datatype);
		}

		/**
		 * Creates attribute value from string representation
		 * 
		 * @param val
		 *            string representation
		 * @return instance of {@code SCOF_AV}
		 * @throws IllegalArgumentException
		 *             val not valid for this factory
		 */
		public abstract AV parse(String val) throws IllegalArgumentException;

		/**
		 * Creates attribute value from a singleton value and possibly extra XML attributes
		 * 
		 * @param value
		 *            attribute value, null if original content is empty (e.g. list of JAXB (mixed) content elements is empty)
		 * @return instance of {@code F_AV}
		 * @throws IllegalArgumentException
		 *             if value is not valid/parseable for this factory
		 */
		public abstract AV getInstance(Serializable value) throws IllegalArgumentException;

		@Override
		public final AV getInstance(final Serializable content, final Map<QName, String> otherXmlAttributes, final XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			if (otherXmlAttributes != null && !otherXmlAttributes.isEmpty())
			{
				throw NON_NULL_OTHER_XML_ATTRIBUTES_ARG_EXCEPTION;
			}

			return getInstance(content);
		}

	}

	/**
	 * Datatype-specific Attribute Value Factory that supports values only based on string content, without any XML attributes, and independent from the context, i.e. constant values.
	 * 
	 * @param <AV>
	 *            type of attribute values created by this factory
	 */
	public static abstract class StringContentOnlyFactory<AV extends AttributeValue> extends StringParseableValueFactory<AV>
	{
		private static final Set<Class<? extends Serializable>> SUPPORTED_STRING_FACTORY_INPUT_TYPES = HashCollections.newImmutableSet(String.class);

		/**
		 * Creates a datatype factory from the Java datatype implementation class and datatype identifier
		 */
		protected StringContentOnlyFactory(final AttributeDatatype<AV> datatype)
		{
			super(datatype);
		}

		@Override
		public final Set<Class<? extends Serializable>> getSupportedInputTypes()
		{
			return SUPPORTED_STRING_FACTORY_INPUT_TYPES;
		}

		@Override
		public final AV getInstance(final Serializable value)
		{
			final String inputStrVal;
			if (value == null)
			{
				/*
				 * Original content is empty, e.g. empty JAXB content list if <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string"/>, value is considered empty string.
				 */
				inputStrVal = "";
			}
			else
			{
				if (!(value instanceof String))
				{
					throw newInvalidInputTypeException(value);
				}

				inputStrVal = (String) value;
			}

			return parse(inputStrVal);
		}
	}

	/*
	 * Make it final to prevent unexpected value change resulting from some function side-effects
	 */
	protected final V value;

	// cached method results (because class is immutable)
	private transient volatile String toString = null; // Effective Java - Item 71
	private transient volatile int hashCode = 0; // Effective Java - Item 9 // Effective Java - Item 9
	private transient volatile List<Serializable> xmlString = null;

	/**
	 * Constructor from Java type of value. A Serializable JAXB-compatible form of the value must be provided to be used directly as first value in {@link #getContent()} The super field 'content' is
	 * set to an empty list but it does not matter, since {@link #getContent()} is overridden here to return a singleton list with {@code rawVal} as single value.
	 *
	 * @param datatypeId
	 *            attribute datatype ID. MUST NOT be null.
	 * @param rawVal
	 *            internal Java native value. MUST NOT be null.
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code rawVal == null}
	 * @throws NullPointerException
	 *             if {@code datatypeId == null}
	 */
	protected SimpleValue(final String datatypeId, final V rawVal) throws IllegalArgumentException, NullPointerException
	{
		super(datatypeId, Collections.emptyList(), Optional.empty());
		assert rawVal != null;
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
	 * Converts the internal value (accessible via {@link #getUnderlyingValue()} to a valid lexical representation for XML marshalling. Equivalent to the 'printMethod' in JAXB 'javaType' binding
	 * customizations. Implementations of this typically call {@link DatatypeConverter}. This method is called by {@link #getContent()} and its result cached by the same method for later use.
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
			xmlString = Collections.<Serializable> singletonList(printXML());
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
			hashCode = Objects.hash(this.dataType, value);
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
		return this.dataType.equals(other.dataType) && this.value.equals(other.value);
	}

}

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

import org.ow2.authzforce.core.pdp.api.expression.XPathCompilerProxy;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Attribute Value based on string or possibly other type of {@link Serializable} content, without any extra XML attributes.
 *
 * @param <V>
 *            underlying Java data type
 */
public abstract class StringParseableValue<V> extends SimpleValue<V>
{

	protected StringParseableValue(final V rawVal) throws IllegalArgumentException, NullPointerException
	{
		super(rawVal);
	}

	@Override
	public Map<QName, String> getXmlAttributes()
	{
		return Collections.emptyMap();
	}

	/**
	 * Datatype-specific Attribute Value Factory that supports values based on string and possibly other type of {@link Serializable} content without any extra XML attributes.
	 * 
	 * @param <AV>
	 *            type of attribute values created by this factory
	 */
	public static abstract class Factory<AV extends AttributeValue> extends BaseFactory<AV>
	{
		private static final IllegalArgumentException NON_NULL_OTHER_XML_ATTRIBUTES_ARG_EXCEPTION = new IllegalArgumentException(
		        "Invalid value content: extra XML attributes are not supported by this primitive datatype, only string content.");

		/**
		 * Creates a datatype factory from the Java datatype implementation class and datatype identifier
		 */
		protected Factory(final AttributeDatatype<AV> datatype)
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
		public final AV getInstance(final Serializable content, final Map<QName, String> otherXmlAttributes, final Optional<XPathCompilerProxy> xPathCompiler) throws IllegalArgumentException
		{
			if (otherXmlAttributes != null && !otherXmlAttributes.isEmpty())
			{
				throw NON_NULL_OTHER_XML_ATTRIBUTES_ARG_EXCEPTION;
			}

			return getInstance(content);
		}

	}
}

/**
 * Copyright 2012-2021 THALES.
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
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.HashCollections;

/**
 * Datatype-specific Attribute Value Factory that supports values only based on string content, without any XML attributes, and independent from the context, i.e. constant values.
 * 
 * @param <AV>
 *            type of attribute values created by this factory
 */
public abstract class StringContentOnlyValueFactory<AV extends AttributeValue> extends StringParseableValue.Factory<AV>
{
	private static final Set<Class<? extends Serializable>> SUPPORTED_STRING_FACTORY_INPUT_TYPES = HashCollections.newImmutableSet(String.class);

	/**
	 * Creates a datatype factory from the Java datatype implementation class and datatype identifier
	 */
	protected StringContentOnlyValueFactory(final AttributeDatatype<AV> datatype)
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
		} else
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
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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

/**
 * /** Static utility methods pertaining to {@link AttributeSource} instances.
 *
 */
public final class AttributeSources
{
	private static final IllegalArgumentException NULL_SOURCE_ID_EXCEPTION = new IllegalArgumentException("Attribute source ID undefined");

	private AttributeSources()
	{
		// prevent instantiation
	}

	private static final class CoreAttributeSource implements AttributeSource
	{
		private final AttributeSource.Type type;

		private CoreAttributeSource(final AttributeSource.Type sourceType)
		{
			assert sourceType != null && sourceType != AttributeSource.Type.OTHER;
			this.type = sourceType;
		}

		@Override
		public Type getType()
		{
			return type;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return type.toString();
		}

	}

	/**
	 * Attribute source that is the original decision request (received by PDP from client/PEP)
	 */
	public static final AttributeSource REQUEST = new CoreAttributeSource(AttributeSource.Type.REQUEST);

	/**
	 * Attribute source that is the PDP itself (typically when the attribute is the current date/time)
	 */
	public static final AttributeSource PDP = new CoreAttributeSource(AttributeSource.Type.PDP);

	private static final class CustomAttributeSource implements AttributeSource
	{

		private final String id;

		/**
		 * Creates instance
		 * 
		 * @param id
		 *            source identifier
		 */
		public CustomAttributeSource(final String id)
		{
			assert id != null;
			this.id = id;
		}

		@Override
		public Type getType()
		{
			return AttributeSource.Type.OTHER;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return id;
		}

	}

	/**
	 * Creates custom attribute source, e;g. attribute provider module/extension
	 * 
	 * @param id
	 *            source identifier
	 * @return new Attribute source
	 * @throws IllegalArgumentException
	 *             iff {@code id == null}
	 *
	 */
	public static AttributeSource newCustomSource(final String id) throws IllegalArgumentException
	{
		if (id == null)
		{
			throw NULL_SOURCE_ID_EXCEPTION;
		}

		return new CustomAttributeSource(id);
	}
}

/*
 * Copyright 2012-2023 THALES.
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

import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Preconditions;
import net.sf.saxon.s9api.ItemType;
import org.ow2.authzforce.core.pdp.api.PdpExtension;

import com.google.common.reflect.TypeToken;

/**
 * Expression evaluation return type (private/package-visible-only constructors are there to make sure only derived classes {@link PrimitiveDatatype} and {@link BagDatatype} may be instantiated.)
 * 
 * @param <V>
 *            Java value type
 */
public abstract class Datatype<V extends Value>
{
	/**
	 * Prefix used by AuthZForce non-standard datatypes (PDP extensions). Third-party (outside AuthZForce project) contributions must use a different prefix to avoid conflicts.
	 */
	public static final String AUTHZFORCE_EXTENSION_PREFIX = PdpExtension.AUTHZFORCE_EXTENSION_PREFIX + "datatype:";

	private final String id;
	private final String funcIdPrefix;
	private final TypeToken<V> typeToken;
	private final ItemType xpathDatatype;

	// derived member fields
	private final transient int hashCode;

	/**
	 * Instantiates a datatype
	 * 
	 * @param genericJavaType
	 *            Basic/raw Java (implementation) class of all instances of this datatype
	 * @param typeParam
	 *            datatype parameter, present iff the datatype is generic, like Java Generics, but more like Java Collection since there is only one type parameter in this case. E.g. if this is a bag
	 *            datatype, {@code typeParam} is the type of bag elements
	 * @param id
	 *            datatype ID
	 * @param functionIdPrefix
	 *            prefix of ID of any standard generic (e.g. bag/set) function built on this datatype, e.g. 'urn:oasis:names:tc:xacml:1.0:function:string' for string datatype
	 * @param xpathDatatype equivalent XPath Item Type to be used in XPath expressions (especially when using XACML Variables as XPath variables)
	 * @throws NullPointerException
	 *             if {@code genericJavaType == null ||  typeParam == null || id == null || functionIdPrefix == null}.
	 */
	Datatype(final TypeToken<V> genericJavaType, final Optional<Datatype<?>> typeParam, final String id, final String functionIdPrefix, final ItemType xpathDatatype) throws IllegalArgumentException
	{
		Preconditions.checkArgument(genericJavaType != null, "Undefined genericJavaType arg (value class of this expression datatype)");
		Preconditions.checkArgument(id != null && !id.isBlank(), "Undefined/invalid id (datatype ID) arg");
		Preconditions.checkArgument(functionIdPrefix != null && !functionIdPrefix.isBlank(), "Undefined/invalid functionIdPrefix (datatype-based function ID prefix) arg");
		Preconditions.checkArgument(xpathDatatype != null, "Undefined xpathDatatype (equivalent XPath Item Type) arg");
		this.typeToken = genericJavaType;
		this.id = id;
		this.funcIdPrefix = functionIdPrefix;
		this.xpathDatatype = xpathDatatype;

		// derived member fields
		this.hashCode = Objects.hash(genericJavaType, typeParam);
	}

	/**
	 * Get ID (URI) of this datatype
	 * 
	 * @return datatype ID
	 */
	public final String getId()
	{
		return this.id;
	}

	/**
	 * Gets prefix of ID of any standard generic (e.g. bag/set) function built on this datatype, e.g. 'urn:oasis:names:tc:xacml:1.0:function:string' for string datatype
	 * 
	 * @return ID prefix for functions of this datatype
	 */
	public final String getFunctionIdPrefix()
	{
		return funcIdPrefix;
	}

	@Override
	public final String toString()
	{
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode()
	{
		return hashCode;
	}

	@Override
	public final boolean equals(final Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof Datatype))
		{
			return false;
		}

		final Datatype<?> other = (Datatype<?>) obj;
		/*
		 * there should be a one-to-one mapping between valueClass and id, so only checking one of these two is necessary
		 */
		return this.typeToken.equals(other.typeToken);
	}

	/**
	 * Equivalent data-type to be used in XPath expressions (AttributeSelectors, etc.), especially for XPath variables
	 * @return XPath Item Type equivalent
	 */
	public final ItemType getXPathItemType() {
		return this.xpathDatatype;
	}

	/**
	 * Return type parameter e.g. the bag element datatype (datatype of every element in a bag of this datatype); null if this is a primitive type (no sub-elements)
	 * 
	 * @return datatype parameter, null for primitive datatypes
	 */
	public abstract Optional<? extends Datatype<?>> getTypeParameter();

	/**
	 * This method returns true if the specified value argument is an instance of the represented datatype; it returns false otherwise
	 * 
	 * @param val
	 *            value to be checked
	 * @return true iff {@code val} is an instance of this datatype
	 */
	public abstract boolean isInstance(final Value val);

	/**
	 * Casts a value to the class or interface represented by this datatype.
	 * 
	 * @param val
	 *            value to be cast
	 * @return the value after casting, or null if {@code val} is null
	 * @throws ClassCastException
	 *             if the value is not null and is not assignable to the type V.
	 */
	public abstract V cast(final Value val) throws ClassCastException;

	/**
	 * Creates a new array with this as component type
	 * 
	 * @param length
	 *            length of the new array
	 * @return the new array
	 */
	public abstract V[] newArray(final int length);


	// public static void main(final String[] args)
	// {
	// final TypeToken<List<String>> tt1 = new TypeToken<List<String>>()
	// {
	//
	// /**
	// *
	// */
	// private static final long serialVersionUID = 1L;
	// };
	//
	// final TypeToken<List<Integer>> tt2 = new TypeToken<List<Integer>>()
	// {
	//
	// /**
	// *
	// */
	// private static final long serialVersionUID = 1L;
	// };
	// System.out.println(tt1.equals(tt2));
	// }

}
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
package org.ow2.authzforce.core.pdp.api;

import java.util.Objects;
import java.util.Optional;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

/**
 * Static utility methods pertaining to {@link AttributeFqn} instances.
 *
 */
public final class AttributeFqns
{
	private static final IllegalArgumentException NULL_ID_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined AttributeId");
	private static final IllegalArgumentException NULL_CATEGORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Attribute category");

	private AttributeFqns()
	{
		// disable constructor
	}

	/**
	 * Extensible {@link AttributeFqn}, that may be extended to add issuer field in particular
	 */
	private static abstract class ExtensibleAttributeFQN implements AttributeFqn
	{
		private final String category;
		private final String id;

		// cached method results
		private transient volatile int hashCode = 0; // Effective Java - Item 9
		private transient volatile String toString = null; // Effective Java - Item 71

		@Override
		public final String getCategory()
		{
			return category;
		}

		@Override
		public final String getId()
		{
			return id;
		}

		private ExtensibleAttributeFQN(final String attrCat, final String attrId)
		{
			assert attrCat != null && attrId != null;
			category = attrCat;
			id = attrId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public final int hashCode()
		{
			if (hashCode == 0)
			{
				hashCode = Objects.hash(category, getIssuer(), id);
			}

			return hashCode;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public final boolean equals(final Object obj)
		{
			// Effective Java - Item 8
			if (this == obj)
			{
				return true;
			}

			if (!(obj instanceof ExtensibleAttributeFQN))
			{
				return false;
			}

			final ExtensibleAttributeFQN other = (ExtensibleAttributeFQN) obj;
			// category cannot be null (see constructor)
			// id cannot be null (see constructor)
			/*
			 * According to XACML Core spec, 7.3.4 Attribute Matching, if the Issuer is not supplied in the AttributeDesignator, ignore it in the match. But if the Issuer is supplied, it must match
			 * only an AttributeFQN with the same Issuer. So here we compare everything, including the Issuer, but in order to handle the first case (Issuer-less AttributeDesignator), we'll make sure
			 * that there is an Issuer-less version in the request context for each Issuer-full Attribute
			 */
			return category.equals(other.getCategory()) && getIssuer().equals(other.getIssuer()) && id.equals(other.getId());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public final String toString()
		{
			if (toString == null)
			{

				final Optional<String> optIssuer = getIssuer();
				toString = "[category='" + category + "', issuer=" + (optIssuer.map(s -> "'" + s + "'").orElse(null)) + ", id='" + id + "']";
			}

			return toString;
		}

		protected abstract int compareIssuers(Optional<String> otherIssuer);

		/**
		 * Compares using lexicographical ordering on Category, then Issuer, then finally the ID.
		 */
		@Override
		public int compareTo(final AttributeFqn other)
		{
			final int thisCatComparedToOtherCat = this.category.compareTo(other.getCategory());
			if (thisCatComparedToOtherCat != 0)
			{
				return thisCatComparedToOtherCat;
			}

			final int thisIssuerComparedToOtherIssuer = compareIssuers(other.getIssuer());
			return thisIssuerComparedToOtherIssuer != 0 ? thisIssuerComparedToOtherIssuer : this.id.compareTo(other.getId());
		}

	}

	/**
	 * Attribute identifier with undefined Issuer
	 */
	private static final class NonIssuedAttributeFQN extends ExtensibleAttributeFQN
	{

		@Override
		public Optional<String> getIssuer()
		{
			return Optional.empty();
		}

		/**
		 * Creates instance from attribute category and ID
		 * 
		 * @param attrCat
		 *            attribute category (non-null)
		 * @param attrId
		 *            (non-null)
		 * @throws IllegalArgumentException
		 *             if {@code attrCat == null || attrId == null}
		 */
		private NonIssuedAttributeFQN(final String attrCat, final String attrId)
		{
			super(attrCat, attrId);
		}

		@Override
		protected int compareIssuers(final Optional<String> otherIssuer)
		{
			return otherIssuer.isPresent() ? -1 : 0;
		}

	}

	/**
	 * Attribute identifier with a defined Issuer
	 */
	private static final class IssuedAttributeFQN extends ExtensibleAttributeFQN
	{
		private final Optional<String> alwaysPresentIssuer;

		@Override
		public Optional<String> getIssuer()
		{
			return alwaysPresentIssuer;
		}

		/**
		 * Creates instance from attribute category, issuer and ID
		 *
		 * @param attrCat
		 *            attribute category (non-null)
		 * @param attrIssuer
		 *            attribute issuer (may be null)
		 * @param attrId
		 *            (non-null)
		 * @throws IllegalArgumentException
		 *             if {@code attrCat == null || attrId == null}
		 */
		private IssuedAttributeFQN(final String attrCat, final String attrIssuer, final String attrId)
		{
			super(attrCat, attrId);
			assert attrIssuer != null;

			alwaysPresentIssuer = Optional.of(attrIssuer);
		}

		@Override
		protected int compareIssuers(final Optional<String> otherIssuer)
		{
			assert this.alwaysPresentIssuer.isPresent();
			return otherIssuer.map(s -> this.alwaysPresentIssuer.get().compareTo(s)).orElse(1);
		}

	}

	/**
	 * Creates instance from attribute category, issuer and ID
	 * 
	 * @param attributeCategory
	 *            attribute category (non-null)
	 * @param attributeIssuer
	 *            attribute issuer (optional)
	 * @param attributeId
	 *            (non-null)
	 * @return new instance of {@link AttributeFqn}
	 * @throws IllegalArgumentException
	 *             if {@code attributeCategory == null || attributeId == null}
	 */
	public static AttributeFqn newInstance(final String attributeCategory, final Optional<String> attributeIssuer, final String attributeId) throws IllegalArgumentException
	{
		if (attributeCategory == null)
		{
			throw NULL_CATEGORY_ARGUMENT_EXCEPTION;
		}

		assert attributeIssuer != null;

		if (attributeId == null)
		{
			throw NULL_ID_ARGUMENT_EXCEPTION;
		}

		return attributeIssuer.isPresent() ? new IssuedAttributeFQN(attributeCategory, attributeIssuer.get(), attributeId) : new NonIssuedAttributeFQN(attributeCategory, attributeId);
	}

	/**
	 * Creates {@link AttributeFqn} from XACML AttributeDesignator
	 * 
	 * @param attributeDesignator
	 *            attribute designator
	 * @return new instance of attribute identifier
	 * @throws IllegalArgumentException
	 *             if {@code attrDes.getCategory() == null || attrDes.getAttributeId() == null}
	 */
	public static AttributeFqn newInstance(final AttributeDesignatorType attributeDesignator) throws IllegalArgumentException
	{
		final String issuer = attributeDesignator.getIssuer();
		return newInstance(attributeDesignator.getCategory(), issuer == null ? Optional.empty() : Optional.of(issuer), attributeDesignator.getAttributeId());
	}

}

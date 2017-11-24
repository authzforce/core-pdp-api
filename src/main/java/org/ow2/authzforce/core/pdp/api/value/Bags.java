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
package org.ow2.authzforce.core.pdp.api.value;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.AttributeSource;
import org.ow2.authzforce.core.pdp.api.AttributeSources;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.value.Bag.Validator;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import com.google.common.collect.ImmutableMultiset;

/**
 * This class consists exclusively of static methods that operate on or return {@link Bag}s. NOTE: do not merge this into {@link Bag} at risk of violating the Acyclic Dependencies principle.
 *
 */
public final class Bags
{
	private static final IllegalArgumentException NULL_DATATYPE_EXCEPTION = new IllegalArgumentException("Undefined bag datatype argument");
	private static final IllegalArgumentException NULL_BAG_ELEMENT_EXCEPTION = new IllegalArgumentException("Null value in bag");
	private static final IllegalArgumentException NULL_BAG_SOURCE_EXCEPTION = new IllegalArgumentException("Undefined source of attribute bag");

	/**
	 * Empty bag
	 * 
	 * @param <AV>
	 *            element datatype
	 */
	private static final class Empty<AV extends AttributeValue> extends Bag<AV>
	{
		private final IndeterminateEvaluationException causeForEmpty;

		private Empty(final Datatype<AV> elementDatatype, final IndeterminateEvaluationException causeForEmpty)
		{
			super(elementDatatype, ImmutableMultiset.<AV> of());
			this.causeForEmpty = causeForEmpty;
		}

		@Override
		public IndeterminateEvaluationException getReasonWhyEmpty()
		{
			return this.causeForEmpty;
		}

		@Override
		public AV getSingleElement()
		{
			return null;
		}

	}

	/**
	 * Empty attribute bag
	 * 
	 * @param <AV>
	 *            element datatype
	 */
	private static final class EmptyAttributeBag<AV extends AttributeValue> extends AttributeBag<AV>
	{
		private final IndeterminateEvaluationException causeForEmpty;

		private EmptyAttributeBag(final Datatype<AV> elementDatatype, final IndeterminateEvaluationException causeForEmpty)
		{
			super(elementDatatype, ImmutableMultiset.<AV> of(), Optional.empty());
			this.causeForEmpty = causeForEmpty;
		}

		@Override
		public IndeterminateEvaluationException getReasonWhyEmpty()
		{
			return this.causeForEmpty;
		}

		@Override
		public AV getSingleElement()
		{
			return null;
		}

	}

	/**
	 * Single-valued bag
	 * 
	 * @param <AV>
	 *            single value datatype
	 */
	private static final class Singleton<AV extends AttributeValue> extends Bag<AV>
	{
		private final AV singleVal;

		private Singleton(final Datatype<AV> elementDatatype, final AV val)
		{
			super(elementDatatype, ImmutableMultiset.of(val));
			this.singleVal = val;
		}

		@Override
		public IndeterminateEvaluationException getReasonWhyEmpty()
		{
			return null;
		}

		@Override
		public AV getSingleElement()
		{
			return this.singleVal;
		}
	}

	/**
	 * Single-valued attribute bag
	 * 
	 * @param <AV>
	 *            single value datatype
	 */
	private static final class SingletonAttributeBag<AV extends AttributeValue> extends AttributeBag<AV>
	{
		private final AV singleVal;

		private SingletonAttributeBag(final Datatype<AV> elementDatatype, final AV val, final AttributeSource attributeBagSource)
		{
			super(elementDatatype, ImmutableMultiset.of(val), Optional.of(attributeBagSource));
			this.singleVal = val;
		}

		@Override
		public IndeterminateEvaluationException getReasonWhyEmpty()
		{
			return null;
		}

		@Override
		public AV getSingleElement()
		{
			return this.singleVal;
		}
	}

	/**
	 * Multi-valued bag
	 * 
	 * @param <AV>
	 *            element datatype
	 */
	private static final class Multi<AV extends AttributeValue> extends Bag<AV>
	{
		/**
		 * Constructor specifying bag datatype. On the contrary to {@link #Bag(Datatype)}, this constructor allows to reuse an existing bag Datatype object, saving the allocation of such object.
		 * 
		 * @param elementDatatype
		 *            element datatype
		 * 
		 * @param values
		 *            bag values (content).
		 * @param causeForEmpty
		 *            reason why this bag is empty if it is; null if it isn't
		 * @throws IllegalArgumentException
		 *             if {@code elementDatatype == null}
		 */
		private Multi(final Datatype<AV> elementDatatype, final Collection<? extends AV> values)
		{
			super(elementDatatype, ImmutableMultiset.copyOf(values));
			assert values.size() > 1;
		}

		@Override
		public IndeterminateEvaluationException getReasonWhyEmpty()
		{
			return null;
		}

		@Override
		public AV getSingleElement()
		{
			return size() == 1 ? elements().iterator().next() : null;
		}

	}

	/**
	 * Multi-valued attribute bag
	 * 
	 * @param <AV>
	 *            element datatype
	 */
	private static final class MultiAttributeBag<AV extends AttributeValue> extends AttributeBag<AV>
	{
		/**
		 * Constructor specifying bag datatype. On the contrary to {@link #Bag(Datatype)}, this constructor allows to reuse an existing bag Datatype object, saving the allocation of such object.
		 * 
		 * @param elementDatatype
		 *            element datatype
		 * 
		 * @param values
		 *            bag values (content).
		 * @param causeForEmpty
		 *            reason why this bag is empty if it is; null if it isn't
		 * @throws IllegalArgumentException
		 *             if {@code elementDatatype == null}
		 */
		private MultiAttributeBag(final Datatype<AV> elementDatatype, final Collection<? extends AV> values, final AttributeSource attributeBagSource)
		{
			super(elementDatatype, ImmutableMultiset.copyOf(values), Optional.of(attributeBagSource));
			assert values.size() > 1;
		}

		@Override
		public IndeterminateEvaluationException getReasonWhyEmpty()
		{
			return null;
		}

		@Override
		public AV getSingleElement()
		{
			return size() == 1 ? elements().iterator().next() : null;
		}

	}

	/**
	 * Creates instance of immutable empty bag with given exception as reason for bag being empty (no attribute value), e.g. error occurred during evaluation
	 * 
	 * @param causeForEmpty
	 *            reason for empty bag (optional but should be specified whenever possible, to help troubleshoot)
	 * @param elementDatatype
	 *            bag element datatype
	 * @return bag
	 * @throws IllegalArgumentException
	 *             if {@code elementDatatype == null}
	 */
	public static <AV extends AttributeValue> Bag<AV> empty(final Datatype<AV> elementDatatype, final IndeterminateEvaluationException causeForEmpty) throws IllegalArgumentException
	{
		if (elementDatatype == null)
		{
			throw NULL_DATATYPE_EXCEPTION;
		}

		return new Empty<>(elementDatatype, causeForEmpty);
	}

	/**
	 * Creates instance of immutable empty attribute bag with given exception as reason for bag being empty (no attribute value), e.g. error occurred during evaluation
	 * 
	 * @param causeForEmpty
	 *            reason for empty bag (optional but should be specified whenever possible, to help troubleshoot)
	 * @param elementDatatype
	 *            bag element datatype
	 * @return bag
	 * @throws IllegalArgumentException
	 *             if {@code elementDatatype == null}
	 */
	public static <AV extends AttributeValue> AttributeBag<AV> emptyAttributeBag(final Datatype<AV> elementDatatype, final IndeterminateEvaluationException causeForEmpty)
			throws IllegalArgumentException
	{
		if (elementDatatype == null)
		{
			throw NULL_DATATYPE_EXCEPTION;
		}

		return new EmptyAttributeBag<>(elementDatatype, causeForEmpty);
	}

	/**
	 * Creates instance of immutable bag containing val and only val value
	 * 
	 * @param elementDatatype
	 *            bag element datatype
	 * @param val
	 *            the val and only val value in the bag
	 * @return bag
	 * @throws IllegalArgumentException
	 *             if {@code val == null || elementDatatype == null}
	 */
	public static <AV extends AttributeValue> Bag<AV> singleton(final Datatype<AV> elementDatatype, final AV val) throws IllegalArgumentException
	{
		if (elementDatatype == null)
		{
			throw NULL_DATATYPE_EXCEPTION;
		}

		if (val == null)
		{
			throw NULL_BAG_ELEMENT_EXCEPTION;
		}

		return new Singleton<>(elementDatatype, val);
	}

	/**
	 * Creates instance of immutable attribute bag containing val and only val value
	 * 
	 * @param elementDatatype
	 *            bag element datatype
	 * @param val
	 *            the val and only val value in the bag
	 * @param attributeValueSource
	 *            attribute value source
	 * @return bag
	 * @throws IllegalArgumentException
	 *             if {@code val == null || elementDatatype == null}
	 */
	public static <AV extends AttributeValue> AttributeBag<AV> singletonAttributeBag(final Datatype<AV> elementDatatype, final AV val, final AttributeSource attributeValueSource)
			throws IllegalArgumentException
	{
		if (elementDatatype == null)
		{
			throw NULL_DATATYPE_EXCEPTION;
		}

		if (val == null)
		{
			throw NULL_BAG_ELEMENT_EXCEPTION;
		}

		if (attributeValueSource == null)
		{
			throw NULL_BAG_SOURCE_EXCEPTION;
		}

		return new SingletonAttributeBag<>(elementDatatype, val, attributeValueSource);
	}

	/**
	 * Creates instance of immutable attribute bag containing val and only val value with {@value AttributeSources#REQUEST} as attribute source
	 * 
	 * @param elementDatatype
	 *            bag element datatype
	 * @param val
	 *            the val and only val value in the bag
	 * @return bag
	 * @throws IllegalArgumentException
	 *             if {@code val == null || elementDatatype == null}
	 */
	public static <AV extends AttributeValue> AttributeBag<AV> singletonAttributeBag(final Datatype<AV> elementDatatype, final AV val) throws IllegalArgumentException
	{
		return singletonAttributeBag(elementDatatype, val, AttributeSources.REQUEST);
	}

	/**
	 * Creates instance of immutable bag of values.
	 * 
	 * @param values
	 *            bag values, typically a List for ordered results, e.g. attribute values for which order matters; or it may be a Set for result of bag/Set functions (intersection, union...)
	 * @param elementDatatype
	 *            bag element datatype
	 * @return bag
	 * @throws IllegalArgumentException
	 *             if {@code elementDatatype == null } or {@code values} has at least one element which is null: {@code values != null && !values.isEmpty() && values.iterator().next() == null}
	 */
	public static <AV extends AttributeValue> Bag<AV> newBag(final Datatype<AV> elementDatatype, final Collection<AV> values) throws IllegalArgumentException
	{
		if (elementDatatype == null)
		{
			throw NULL_DATATYPE_EXCEPTION;
		}

		if (values == null || values.isEmpty())
		{
			return new Empty<>(elementDatatype, null);
		}

		final Iterator<AV> valueIterator = values.iterator();
		final AV val0 = valueIterator.next();
		if (val0 == null)
		{
			throw NULL_BAG_ELEMENT_EXCEPTION;
		}

		if (!valueIterator.hasNext())
		{
			// only one value
			return new Singleton<>(elementDatatype, val0);
		}

		// more than one value
		return new Multi<>(elementDatatype, values);
	}

	/**
	 * Creates instance of immutable attribtue bag.
	 * 
	 * @param values
	 *            bag values, typically a List for ordered results, e.g. attribute values for which order matters; or it may be a Set for result of bag/Set functions (intersection, union...)
	 * @param elementDatatype
	 *            bag element datatype
	 * @param attributeBagSource
	 *            source of the attribute values
	 * @return bag attribute bag
	 * @throws IllegalArgumentException
	 *             if {@code elementDatatype == null } or {@code values} has at least one element which is null: {@code values != null && !values.isEmpty() && values.iterator().next() == null}
	 */
	public static <AV extends AttributeValue> AttributeBag<AV> newAttributeBag(final Datatype<AV> elementDatatype, final Collection<AV> values, final AttributeSource attributeBagSource)
			throws IllegalArgumentException
	{
		if (elementDatatype == null)
		{
			throw NULL_DATATYPE_EXCEPTION;
		}

		if (values == null || values.isEmpty())
		{
			return new EmptyAttributeBag<>(elementDatatype, null);
		}

		final Iterator<AV> valueIterator = values.iterator();
		final AV val0 = valueIterator.next();
		if (val0 == null)
		{
			throw NULL_BAG_ELEMENT_EXCEPTION;
		}

		if (attributeBagSource == null)
		{
			throw NULL_BAG_SOURCE_EXCEPTION;
		}

		if (!valueIterator.hasNext())
		{
			// only one value
			return new SingletonAttributeBag<>(elementDatatype, val0, attributeBagSource);
		}

		// more than one value
		return new MultiAttributeBag<>(elementDatatype, values, attributeBagSource);
	}

	/**
	 * Creates instance of immutable attribute bag with {@link AttributeSources#REQUEST} as attribute source.
	 * 
	 * @param values
	 *            bag values, typically a List for ordered results, e.g. attribute values for which order matters; or it may be a Set for result of bag/Set functions (intersection, union...)
	 * @param elementDatatype
	 *            bag element datatype
	 * @return bag attribute bag
	 * @throws IllegalArgumentException
	 *             if {@code elementDatatype == null } or {@code values} has at least one element which is null: {@code values != null && !values.isEmpty() && values.iterator().next() == null}
	 */
	public static <AV extends AttributeValue> AttributeBag<AV> newAttributeBag(final Datatype<AV> elementDatatype, final Collection<AV> values) throws IllegalArgumentException
	{
		return newAttributeBag(elementDatatype, values, AttributeSources.REQUEST);
	}

	/**
	 * Checks the bag is not empty, typically used to enforce MustBePresent=True on XACML AttributeDesignator/AttributeSelector elements
	 */
	public static final class NonEmptinessValidator implements Validator
	{

		private final String messageIfEmpty;

		/**
		 * Creates validator
		 * 
		 * @param messageIfEmpty
		 *            message used as exception message if bag is empty
		 */
		public NonEmptinessValidator(final String messageIfEmpty)
		{
			this.messageIfEmpty = messageIfEmpty;
		}

		@Override
		public void validate(final Bag<?> bag) throws IndeterminateEvaluationException
		{
			if (bag == null || bag.isEmpty())
			{
				throw new IndeterminateEvaluationException(messageIfEmpty, XacmlStatusCode.MISSING_ATTRIBUTE.value());
			}

			if (bag.isEmpty())
			{
				throw new IndeterminateEvaluationException(messageIfEmpty, XacmlStatusCode.MISSING_ATTRIBUTE.value(), bag.getReasonWhyEmpty());
			}

		}

	}

	/**
	 * Dumb validator that does nothing, typically used for MustBePresent=False on XACML AttributeDesignator/AttributeSelector elements
	 */
	public static final Validator DUMB_VALIDATOR = new Validator()
	{

		@Override
		public void validate(final Bag<?> bag)
		{
			// do nothing since the flag is disabled
		}

	};

	private Bags()
	{
	}
}

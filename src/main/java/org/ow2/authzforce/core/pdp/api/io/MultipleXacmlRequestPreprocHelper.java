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
package org.ow2.authzforce.core.pdp.api.io;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.DecisionRequest;
import org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.ImmutableDecisionRequest;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;

/**
 * Multiple Decision Request preprocessing helper, for supporting the Multiple Decision Profile scheme "Repeated attribute categories".
 *
 * @param <R>
 *            type of output Individual XACML decision request from the preprocessing
 * @param <VALIDATOR_INPUT_ATTRIBUTE_CATEGORY_OBJECT>
 *            raw input object representing category-specific XACML attributes, e.g. from XACML/XML Attributes element, or equivalent XACML/JSON (JSON Profile) Object
 * @param <VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT>
 *            type of properly-validated XACML attribute category object representation. For example, when parsing raw JSON array from XACML request's Attribute value formatted according to JSON
 *            profile of XACML, the type of each element in the array, as returned by a generic JSON parser, is a generic supertype of all JSON value types (String, JSONObject, JSONArray...); but
 *            after passing each element through proper validation, we should get a JSONObject-specific representation type as expected from the JSON Profile specification.
 */
public abstract class MultipleXacmlRequestPreprocHelper<R extends DecisionRequest, VALIDATOR_INPUT_ATTRIBUTE_CATEGORY_OBJECT, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT>
{
	/**
	 * (Mutable) {@link IndividualXacmlJaxbRequest} builder. Allows to update attribute categories and rebuild (immutable) Individual Decision Requests over and over again. Useful especially for
	 * {@link DecisionRequestPreprocessor} implementations supporting the Multiple Decision Profile.
	 *
	 * @version $Id: $
	 * @param <R>
	 *            type of @link {@link DecisionRequest} built by this builder in the context of Multiple Decision Request processing for a specific type of input/output, e.g. XACML/XML, XACML/JSON...
	 * @param <INPUT_ATTRIBUTE_CATEGORY>
	 *            type of input attribute category in original format, e.g. JAXB Attributes class for XACML/XML input, or JSON object for XACML/JSON input.
	 */
	private static final class UpdatableIndividualXacmlRequestBuilder<R extends DecisionRequest, INPUT_ATTRIBUTE_CATEGORY>
	{
		private static final IllegalArgumentException UNDEF_ATTRIBUTES_EXCEPTION = new IllegalArgumentException("Undefined attributes");
		private static final IllegalArgumentException UNDEF_ATTRIBUTE_CATEGORY_EXCEPTION = new IllegalArgumentException("Undefined attribute category");

		/*
		 * Initialized not null by constructors
		 */
		private final Map<AttributeFqn, AttributeBag<?>> namedAttributes;
		private final Map<String, XdmNode> contentNodesByCategory;
		private final List<INPUT_ATTRIBUTE_CATEGORY> attributesToIncludeInResult;
		private final boolean isApplicablePolicyIdListReturned;
		private final IndividualXacmlRequestFactory<R, INPUT_ATTRIBUTE_CATEGORY> decisionReqFactory;

		/**
		 * Creates empty request (no attribute)
		 *
		 * @param returnPolicyIdList
		 *            equivalent of XACML ReturnPolicyIdList
		 * @param individualIoDecisionRequestFactory
		 *            I/O-specific Individual Decision Request factory
		 */
		private UpdatableIndividualXacmlRequestBuilder(final boolean returnPolicyIdList, final IndividualXacmlRequestFactory<R, INPUT_ATTRIBUTE_CATEGORY> individualIoDecisionRequestFactory)
		{
			// these maps/lists may be updated later by put(...) method defined in this class
			namedAttributes = HashCollections.newUpdatableMap();
			contentNodesByCategory = HashCollections.newUpdatableMap();
			attributesToIncludeInResult = new ArrayList<>();
			isApplicablePolicyIdListReturned = returnPolicyIdList;
			this.decisionReqFactory = individualIoDecisionRequestFactory;
		}

		/**
		 * Create new instance as a clone of an existing request.
		 *
		 * @param baseRequest
		 *            replicated existing request. Further changes to it are not reflected back to this new instance.
		 */
		public UpdatableIndividualXacmlRequestBuilder(final UpdatableIndividualXacmlRequestBuilder<R, INPUT_ATTRIBUTE_CATEGORY> baseRequest)
		{
			assert baseRequest != null;

			// these maps/lists may be updated later by put(...) method defined in this class
			namedAttributes = HashCollections.newUpdatableMap(baseRequest.namedAttributes);
			contentNodesByCategory = HashCollections.newUpdatableMap(baseRequest.contentNodesByCategory);
			isApplicablePolicyIdListReturned = baseRequest.isApplicablePolicyIdListReturned;
			attributesToIncludeInResult = new ArrayList<>(baseRequest.attributesToIncludeInResult);
			this.decisionReqFactory = baseRequest.decisionReqFactory;
		}

		/**
		 * Put attributes of a specific category in request.
		 *
		 * @param categoryName
		 *            category URI
		 * @param categorySpecificAttributes
		 *            attributes in category {@code categoryName}
		 * @throws java.lang.IllegalArgumentException
		 *             if {@code categoryName == null || categorySpecificAttributes == null} or duplicate attribute category (this method was already called with same {@code categoryName})
		 */
		public void put(final String categoryName, final SingleCategoryAttributes<?, INPUT_ATTRIBUTE_CATEGORY> categorySpecificAttributes) throws IllegalArgumentException
		{
			if (categoryName == null)
			{
				throw UNDEF_ATTRIBUTE_CATEGORY_EXCEPTION;
			}

			if (categorySpecificAttributes == null)
			{
				throw UNDEF_ATTRIBUTES_EXCEPTION;
			}

			// extraContentsByCategory initialized not null by constructors
			assert contentNodesByCategory != null;
			final XdmNode newContentNode = categorySpecificAttributes.getExtraContent();
			if (newContentNode != null)
			{
				final XdmNode duplicate = contentNodesByCategory.putIfAbsent(categoryName, newContentNode);
				if (duplicate != null)
				{
					throw new IllegalArgumentException("Duplicate Attributes[@Category] in Individual Decision Request (not allowed): " + categoryName);
				}
			}

			/*
			 * Convert growable (therefore mutable) bag of attribute values to immutable ones. Indeed, we must guarantee that attribute values remain constant during the evaluation of the request, as
			 * mandated by the XACML spec, section 7.3.5: <p> <i>
			 * "Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated in the context before it is first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of that attribute shall use the same bag of values that was initially tested.)"
			 * </i></p>
			 */
			for (final Entry<AttributeFqn, AttributeBag<?>> attrEntry : categorySpecificAttributes)
			{
				namedAttributes.put(attrEntry.getKey(), attrEntry.getValue());
			}

			final INPUT_ATTRIBUTE_CATEGORY catSpecificAttrsToIncludeInResult = categorySpecificAttributes.getAttributesToIncludeInResult();
			if (catSpecificAttrsToIncludeInResult != null)
			{
				attributesToIncludeInResult.add(catSpecificAttrsToIncludeInResult);
			}

		}

		/**
		 * Builds an immutable Individual Decision Request based on current content
		 * 
		 * @return instance of R
		 */
		public R build()
		{
			return this.decisionReqFactory.newInstance(ImmutableDecisionRequest.getInstance(this.namedAttributes, this.contentNodesByCategory, this.isApplicablePolicyIdListReturned),
			        ImmutableList.copyOf(this.attributesToIncludeInResult));
		}

	}

	private final IndividualXacmlRequestFactory<R, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT> individualXacmlReqFactory;

	/**
	 * Constructor
	 * 
	 * @param individualXacmlRequestFactory
	 *            individual XACML request factory
	 */
	public MultipleXacmlRequestPreprocHelper(final IndividualXacmlRequestFactory<R, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT> individualXacmlRequestFactory)
	{
		this.individualXacmlReqFactory = individualXacmlRequestFactory;
	}

	protected abstract VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT validate(VALIDATOR_INPUT_ATTRIBUTE_CATEGORY_OBJECT inputRawAttributeCategoryObject) throws IndeterminateEvaluationException;

	/**
	 * Pre-processes (validates and/or transforms) a Request, may result in multiple individual decision requests, e.g. if implementing the Multiple Decision Profile or Hierarchical Resource profile
	 * 
	 * @param inputRequestAttributeCategoryObjects
	 *            XACML Attribute Category objects (e.g. XACML/XML Attributes elements or XACML/JSON objects from 'Attribute' array), null if none
	 * @param xacmlAttrsParser
	 *            XACML Attributes element Parser instance, used to parse each Attributes in {@code attributesList}.
	 * @param isApplicablePolicyIdListReturned
	 *            XACML Request's property {@code returnPolicyIdList}.
	 * @param combinedDecision
	 *            XACML Request's property {@code isCombinedDecision}
	 * @param xPathCompiler
	 *            xpathExpression compiler, corresponding to the XACML RequestDefaults element, or null if no RequestDefaults element.
	 * 
	 * @param namespaceURIsByPrefix
	 *            namespace prefix-URI mappings (e.g. "... xmlns:prefix=uri") in the original XACML Request bound to {@code req}, used as part of the context for XPath evaluation
	 * 
	 * @return individual decision requests, as defined in Multiple Decision Profile, e.g. a singleton list if no multiple decision requested or supported by the pre-processor
	 *         <p>
	 *         Return a Collection and not array to make it easy for the implementer to create a defensive copy with Collections#unmodifiableList() and alike.
	 *         </p>
	 * @throws IndeterminateEvaluationException
	 *             if some feature requested in the Request is not supported by this pre-processor
	 */
	public final List<R> process(final Iterable<VALIDATOR_INPUT_ATTRIBUTE_CATEGORY_OBJECT> inputRequestAttributeCategoryObjects,
	        final SingleCategoryXacmlAttributesParser<VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT> xacmlAttrsParser, final boolean isApplicablePolicyIdListReturned, final boolean combinedDecision,
	        final XPathCompiler xPathCompiler, final Map<String, String> namespaceURIsByPrefix) throws IndeterminateEvaluationException
	{
		/*
		 * Parse Request attributes and group possibly repeated categories to implement Multiple Decision Profile, §2.3.
		 */
		/*
		 * We would like that the order of attributes (more particularly attribute categories) included in the result be in the same order as in the request (more particularly, attribute categories in
		 * order of first occurrence in the case of a Multiple Decision Request); because "Clients generally appreciate having things returned in the same order they were presented." (See Java
		 * LinkedHashMap javadoc description.) Therefore, we use a LinkedHashMap for the Map<CategoryName,Attributes> below. If the impact on performance proves to be too negative, we might switch to
		 * a simpler Map implementation not preserving iteration order. Unfortunately, Koloboke - that we are using as HashMap alternative to JDK - does not support LinkedHashMap equivalent at the
		 * moment: https://github.com/leventov/Koloboke/issues/47 (we should keep an eye on it). So until this resolved, we use JDK LinkedHashMap.
		 */
		final Map<String, Queue<SingleCategoryAttributes<?, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT>>> multiReqAttrAlternativesByCategory = new LinkedHashMap<>();
		for (final VALIDATOR_INPUT_ATTRIBUTE_CATEGORY_OBJECT inputRequestAttributeCategoryObject : inputRequestAttributeCategoryObjects)
		{
			final VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT inputRequestAttCatObj = validate(inputRequestAttributeCategoryObject);
			final SingleCategoryAttributes<?, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT> categoryAttributesAlternative = xacmlAttrsParser.parseAttributes(inputRequestAttCatObj, xPathCompiler);
			if (categoryAttributesAlternative == null)
			{
				// skip this empty Attributes
				continue;
			}

			final String categoryId = categoryAttributesAlternative.getCategoryId();
			final Queue<SingleCategoryAttributes<?, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT>> oldAttrAlternatives = multiReqAttrAlternativesByCategory.get(categoryId);
			final Queue<SingleCategoryAttributes<?, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT>> newAttrAlternatives;
			if (oldAttrAlternatives == null)
			{
				newAttrAlternatives = new ArrayDeque<>();
				multiReqAttrAlternativesByCategory.put(categoryId, newAttrAlternatives);
			}
			else
			{
				newAttrAlternatives = oldAttrAlternatives;
			}

			newAttrAlternatives.add(categoryAttributesAlternative);
		}

		/*
		 * Create mutable initial individual request from which all others will be created/cloned
		 */
		final UpdatableIndividualXacmlRequestBuilder<R, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT> initialIndividualReqBuilder;
		try
		{
			initialIndividualReqBuilder = new UpdatableIndividualXacmlRequestBuilder<>(isApplicablePolicyIdListReturned, individualXacmlReqFactory);
		}
		catch (final IllegalArgumentException e)
		{
			throw new IndeterminateEvaluationException("Invalid RequestDefaults/XPathVersion", XacmlStatusCode.SYNTAX_ERROR.value(), e);
		}
		/*
		 * Generate the Multiple Individual Decision Requests starting with initialIndividualReq and cloning/adding new attributes/content for each new attribute category's Attributes alternative in
		 * requestAttrAlternativesByCategory
		 */
		/*
		 * XACML Multiple Decision Profile, § 2.3.3: "For each combination of repeated <Attributes> elements, one Individual Decision Request SHALL be created. This Individual Request SHALL be
		 * identical to the original request context with one exception: only one <Attributes> element of each repeated category SHALL be present." In JSON Profile, the <Attributes> element is
		 * represented by a JSON object.
		 */
		final List<UpdatableIndividualXacmlRequestBuilder<R, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT>> individualRequestBuilders = new ArrayList<>();
		individualRequestBuilders.add(initialIndividualReqBuilder);
		/*
		 * In order to create the final individual decision requests, for each attribute category, add each alternative to individual request builders.
		 */
		final List<R> finalIndividualRequests = new ArrayList<>();
		/*
		 * As explained at the beginning of the method, at this point, we want to make sure that entries are returned in the same order (of first occurrence in the case of Multiple Decision Request)
		 * as the categories in the request, where each category matches the key in the entry; because "Clients generally appreciate having things returned in the same order they were presented." So
		 * the map should guarantee that the iteration order is the same as insertion order used previously (e.g. LinkedHashMap).
		 */
		final Iterator<Entry<String, Queue<SingleCategoryAttributes<?, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT>>>> multiReqAttrAlternativesByCategoryIterator = multiReqAttrAlternativesByCategory
		        .entrySet().iterator();
		boolean isLastCategory = !multiReqAttrAlternativesByCategoryIterator.hasNext();
		while (!isLastCategory)
		{
			final Entry<String, Queue<SingleCategoryAttributes<?, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT>>> multiReqAttrAlternativesByCategoryEntry = multiReqAttrAlternativesByCategoryIterator
			        .next();
			final String categoryName = multiReqAttrAlternativesByCategoryEntry.getKey();
			final Queue<SingleCategoryAttributes<?, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT>> categorySpecificAlternatives = multiReqAttrAlternativesByCategoryEntry.getValue();
			isLastCategory = !multiReqAttrAlternativesByCategoryIterator.hasNext();
			final ListIterator<UpdatableIndividualXacmlRequestBuilder<R, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT>> individualRequestsIterator = individualRequestBuilders.listIterator();
			while (individualRequestsIterator.hasNext())
			{
				final UpdatableIndividualXacmlRequestBuilder<R, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT> oldIndividualReqBuilder = individualRequestsIterator.next();
				/*
				 * New newIndividualReqBuilders created below from this $oldIndividualReqBuilder will replace it in the list of $individualRequestBuilders (and will be used in their turn as
				 * $oldIndividualReqBuilders). So remove current $oldIndividualReqBuilder from the list
				 */
				individualRequestsIterator.remove();

				/*
				 * Before we add the first category alternative (categoryAlternative0) to the oldReq already created (the "old" one), we clone it for every other alternative, then add this other
				 * alternative to the new clone. Note that we called categoryAlternatives.poll() before, removing the first alternative, so categoryAlternatives only contains the other alternatives
				 * now.
				 */
				for (final SingleCategoryAttributes<?, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT> otherCategoryAlternative : categorySpecificAlternatives)
				{
					// clone the request
					final UpdatableIndividualXacmlRequestBuilder<R, VALIDATOR_OUTPUT_ATTRIBUTE_CATEGORY_OBJECT> newIndividualReqBuilder = new UpdatableIndividualXacmlRequestBuilder<>(
					        oldIndividualReqBuilder);
					newIndividualReqBuilder.put(categoryName, otherCategoryAlternative);
					if (isLastCategory)
					{
						// we can finalize the request build
						finalIndividualRequests.add(newIndividualReqBuilder.build());
					}
					else
					{
						/*
						 * add the new request builder to the list of builders for the next round
						 */
						individualRequestsIterator.add(newIndividualReqBuilder);
					}
				}
			}

		}

		return finalIndividualRequests;
	}

}

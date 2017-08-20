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
package org.ow2.authzforce.core.pdp.api.policy;

import java.io.Closeable;
import java.io.IOException;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbXacmlUtils.XACMLParserFactory;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

/**
 * This class is used by the PDP to find policies referenced by Policy(Set)IdReference.
 * <p>
 * Implements {@link Closeable} because it has a reference to a {@link RefPolicyProviderModule} which is {@link Closeable}.
 * <p>
 * Currently it has only one module ({@link RefPolicyProviderModule}) but may have multiple ones in the future. Therefore, this class should not be merged with {@link RefPolicyProviderModule} since it
 * may be a one-to-many relationship later, and not a one-to-one as currently.
 */
public final class BaseStaticRefPolicyProvider implements CloseableStaticRefPolicyProvider
{
	private static final IllegalArgumentException NULL_REF_POLICY_PROVIDER_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined RefPolicyProvider module");

	private final StaticRefPolicyProviderModule refPolicyProviderMod;

	/**
	 * Creates RefPolicyProvider instance
	 * 
	 * @param refPolicyProviderMod
	 *            referenced Policy Provider module (supports Policy(Set)IdReferences)
	 * @throws IllegalArgumentException
	 *             if {@code refPolicyProviderMod} is null
	 */
	public BaseStaticRefPolicyProvider(final StaticRefPolicyProviderModule refPolicyProviderMod) throws IllegalArgumentException
	{
		if (refPolicyProviderMod == null)
		{
			throw NULL_REF_POLICY_PROVIDER_ARGUMENT_EXCEPTION;
		}

		this.refPolicyProviderMod = refPolicyProviderMod;
	}

	/**
	 * Creates RefPolicyProvider instance
	 * 
	 * @param refPolicyProviderModFactory
	 *            refPolicyProvider module factory creating instance of {@link StaticRefPolicyProviderModule} from configuration defined by {@code jaxbRefPolicyProvider}
	 * @param jaxbRefPolicyProvider
	 *            XML/JAXB configuration of RefPolicyProvider module
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference: PolicySet1 -> PolicySet2 -> ...; a strictly negative value means no limit
	 * @param expressionFactory
	 *            Expression factory for parsing XACML Expressions in the policies
	 * @param combiningAlgRegistry
	 *            Combining algorithm registry for getting implementations of algorithms used in the policies
	 * @param xacmlParserFactory
	 *            XACML parser factory
	 * @param environmentProperties
	 *            PDP configuration environment properties
	 * @throws IllegalArgumentException
	 *             if {@code refPolicyProviderModFactory != null} and
	 *             {@code refPolicyProviderModFactory.getInstance(jaxbRefPolicyProvider, xacmlParserFactory, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry, environmentProperties)} does
	 *             not implement {@link StaticRefPolicyProviderModule}
	 */
	public <CONF extends AbstractPolicyProvider> BaseStaticRefPolicyProvider(final CONF jaxbRefPolicyProvider, final RefPolicyProviderModule.Factory<CONF> refPolicyProviderModFactory,
			final XACMLParserFactory xacmlParserFactory, final ExpressionFactory expressionFactory, final CombiningAlgRegistry combiningAlgRegistry, final int maxPolicySetRefDepth,
			final EnvironmentProperties environmentProperties) throws IllegalArgumentException
	{
		this(refPolicyProviderModFactory == null ? null : validate(refPolicyProviderModFactory.getInstance(jaxbRefPolicyProvider, xacmlParserFactory, maxPolicySetRefDepth, expressionFactory,
				combiningAlgRegistry, environmentProperties)));
	}

	private static StaticRefPolicyProviderModule validate(final RefPolicyProviderModule refPolicyProviderModule) throws IllegalArgumentException
	{
		if (!(refPolicyProviderModule instanceof StaticRefPolicyProviderModule))
		{
			throw new IllegalArgumentException("RefPolicyProviderModule arg '" + refPolicyProviderModule + "'  is not compatible with " + BaseStaticRefPolicyProvider.class
					+ ". Expected: instance of " + StaticRefPolicyProviderModule.class + ". Make sure the PDP extension of type " + RefPolicyProviderModule.Factory.class
					+ " corresponding to the refPolicyProvider in PDP configuration can create such instances.");
		}

		return (StaticRefPolicyProviderModule) refPolicyProviderModule;
	}

	@Override
	public Deque<String> checkJoinedPolicyRefChain(final Deque<String> policyRefChain1, final List<String> policyRefChain2)
	{
		return this.refPolicyProviderMod.checkJoinedPolicyRefChain(policyRefChain1, policyRefChain2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.policy.RefPolicyProvider#findPolicy(java. lang. String, com.sun.xacml.VersionConstraints, java.lang.Class, java.util.Deque)
	 */
	@Override
	public TopLevelPolicyElementEvaluator get(final TopLevelPolicyElementType refPolicyType, final String idRef, final Optional<VersionPatterns> constraints, final Deque<String> policySetRefChain,
			final EvaluationContext evaluationCtx) throws IndeterminateEvaluationException
	{
		/*
		 * It is the responsability of the refPolicyProviderMod to update policySetRefChain and check against maxPolicySetRefDepth - using RefPolicyProvider.Utils class - whenever a
		 * PolicySetIdReference is found in resolved PolicySets
		 */
		return refPolicyProviderMod.get(refPolicyType, idRef, constraints, policySetRefChain, evaluationCtx);
	}

	@Override
	public StaticTopLevelPolicyElementEvaluator get(final TopLevelPolicyElementType refPolicyType, final String idRef, final Optional<VersionPatterns> constraints,
			final Deque<String> policySetRefChain) throws IndeterminateEvaluationException
	{
		/*
		 * It is the responsability of the refPolicyProviderMod to update policySetRefChain and check against maxPolicySetRefDepth - using RefPolicyProvider.Utils class - whenever a
		 * PolicySetIdReference is found in resolved PolicySets
		 */
		return refPolicyProviderMod.get(refPolicyType, idRef, constraints, policySetRefChain);
	}

	@Override
	public void close() throws IOException
	{
		this.refPolicyProviderMod.close();
	}
}
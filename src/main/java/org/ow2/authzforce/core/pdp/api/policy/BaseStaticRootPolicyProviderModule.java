/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.api.policy;

import java.io.Closeable;
import java.io.IOException;
import java.util.Deque;

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.XACMLParserFactory;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

/**
 * Base class for static {@link RootPolicyProviderModule}s
 */
public abstract class BaseStaticRootPolicyProviderModule implements StaticRootPolicyProviderModule
{
	/**
	 * This class is used by the PDP to find policies referenced by Policy(Set)IdReference.
	 * <p>
	 * Implements {@link Closeable} because it has a reference to a {@link RefPolicyProviderModule} which is {@link Closeable}.
	 * <p>
	 * Currently it has only one module ({@link RefPolicyProviderModule}) but may have multiple ones in the future. Therefore, this class should not be merged with {@link RefPolicyProviderModule}
	 * since it may be a one-to-many relationship later, and not a one-to-one as currently.
	 */
	private static final class BaseStaticRefPolicyProvider implements CloseableStaticRefPolicyProvider
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
		private BaseStaticRefPolicyProvider(StaticRefPolicyProviderModule refPolicyProviderMod) throws IllegalArgumentException
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
		 *             {@code refPolicyProviderModFactory.getInstance(jaxbRefPolicyProvider, xacmlParserFactory, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry, environmentProperties)}
		 *             does not implement {@link StaticRefPolicyProviderModule}
		 */
		private <CONF extends AbstractPolicyProvider> BaseStaticRefPolicyProvider(CONF jaxbRefPolicyProvider, RefPolicyProviderModule.Factory<CONF> refPolicyProviderModFactory,
				XACMLParserFactory xacmlParserFactory, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry, int maxPolicySetRefDepth,
				EnvironmentProperties environmentProperties) throws IllegalArgumentException
		{
			this(refPolicyProviderModFactory == null ? null : validate(refPolicyProviderModFactory.getInstance(jaxbRefPolicyProvider, xacmlParserFactory, maxPolicySetRefDepth, expressionFactory,
					combiningAlgRegistry, environmentProperties)));
		}

		private static StaticRefPolicyProviderModule validate(RefPolicyProviderModule refPolicyProviderModule) throws IllegalArgumentException
		{
			if (!(refPolicyProviderModule instanceof StaticRefPolicyProviderModule))
			{
				throw new IllegalArgumentException("RefPolicyProviderModule arg '" + refPolicyProviderModule + "'  is not compatible with " + BaseStaticRefPolicyProvider.class
						+ ". Expected: instance of " + StaticRefPolicyProviderModule.class + ". Make sure the PDP extension of type " + RefPolicyProviderModule.Factory.class
						+ " corresponding to the refPolicyProvider in PDP configuration can create such instances.");
			}

			return (StaticRefPolicyProviderModule) refPolicyProviderModule;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ow2.authzforce.core.policy.RefPolicyProvider#findPolicy(java. lang. String, com.sun.xacml.VersionConstraints, java.lang.Class, java.util.Deque)
		 */
		@Override
		public TopLevelPolicyElementEvaluator get(TopLevelPolicyElementType refPolicyType, String idRef, VersionPatterns constraints, Deque<String> policySetRefChain, EvaluationContext evaluationCtx)
				throws IndeterminateEvaluationException
		{
			/*
			 * It is the responsability of the refPolicyProviderMod to update policySetRefChain and check against maxPolicySetRefDepth - using RefPolicyProvider.Utils class - whenever a
			 * PolicySetIdReference is found in resolved PolicySets
			 */
			return refPolicyProviderMod.get(refPolicyType, idRef, constraints, policySetRefChain, evaluationCtx);
		}

		@Override
		public StaticTopLevelPolicyElementEvaluator get(TopLevelPolicyElementType refPolicyType, String idRef, VersionPatterns constraints, Deque<String> policySetRefChain)
				throws IndeterminateEvaluationException
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

	protected final CloseableStaticRefPolicyProvider refPolicyProvider;

	/**
	 * Creates instance
	 * 
	 * @param expressionFactory
	 *            (mandatory) Expression factory
	 * @param combiningAlgRegistry
	 *            (mandatory) registry of policy/rule combining algorithms
	 * @param jaxbRefPolicyProviderConf
	 *            (optional) XML/JAXB configuration of RefPolicyProvider module used for resolving Policy(Set)(Id)References in root policy; may be null if support of PolicyReferences is disabled or
	 *            this RootPolicyProvider module already supports these.
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference that is allowed in RefPolicyProvider derived from {@code jaxbRefPolicyProviderConf}: PolicySet1 -> PolicySet2
	 *            -> ...; a strictly negative value means no limit. If and only if {@code jaxbRefPolicyProviderConf == null}, this parameter is ignored.
	 * @param xacmlParserFactory
	 *            XACML Parser Factory
	 * @param refPolicyProviderModFactory
	 *            refPolicyProvider module factory for creating a module instance from configuration defined by {@code jaxbRefPolicyProviderConf}
	 * @throws IllegalArgumentException
	 *             if {@code refPolicyProviderModFactory != null} and
	 *             {@code refPolicyProviderModFactory.getInstance(jaxbRefPolicyProvider, xacmlParserFactory, maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry, environmentProperties)} does
	 *             not implement {@link StaticRefPolicyProviderModule}
	 */
	protected <CONF extends AbstractPolicyProvider> BaseStaticRootPolicyProviderModule(ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry,
			XACMLParserFactory xacmlParserFactory, CONF jaxbRefPolicyProviderConf, RefPolicyProviderModule.Factory<CONF> refPolicyProviderModFactory, int maxPolicySetRefDepth,
			EnvironmentProperties envProps) throws IllegalArgumentException
	{
		// create ref-policy Provider
		if (jaxbRefPolicyProviderConf == null)
		{
			this.refPolicyProvider = null;
		} else
		{
			/*
			 * The refPolicyProviderModule is not instantiated here but in the BaseRefPolicyProvider since it is the one using this resource (refPolicyProviderModule), therefore responsible for
			 * closing it (call Closeable#close()) when it is done using them. We apply the basic principle that is the class creating the resource, that manages/closes it.
			 */
			this.refPolicyProvider = new BaseStaticRefPolicyProvider(jaxbRefPolicyProviderConf, refPolicyProviderModFactory, xacmlParserFactory, expressionFactory, combiningAlgRegistry,
					maxPolicySetRefDepth, envProps);
		}
	}

	@Override
	public final void close() throws IOException
	{
		if (refPolicyProvider != null)
		{
			refPolicyProvider.close();
		}
	}

}

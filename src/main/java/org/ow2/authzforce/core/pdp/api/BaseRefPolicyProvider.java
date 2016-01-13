/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.api;

import java.io.Closeable;
import java.io.IOException;
import java.util.Deque;

import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.XACMLParserFactory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

/**
 * This class is used by the PDP to find policies referenced by Policy(Set)IdReference.
 * <p>
 * Implements {@link Closeable} because it has a reference to a {@link RefPolicyProviderModule} which is {@link Closeable}.
 * <p>
 * Currently it has only one module ({@link RefPolicyProviderModule}) but may have multiple ones in the future. Therefore, this class should not be merged with
 * {@link RefPolicyProviderModule} since it may be a one-to-many relationship later, and not a one-to-one as currently.
 */
public class BaseRefPolicyProvider implements Closeable, RefPolicyProvider
{
	private static final IllegalArgumentException NULL_REF_POLICY_PROVIDER_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Undefined RefPolicyProvider module");

	private final RefPolicyProviderModule refPolicyProviderMod;

	/**
	 * Creates RefPolicyProvider instance
	 * 
	 * @param refPolicyProviderMod
	 *            referenced Policy Provider module (supports Policy(Set)IdReferences)
	 * @throws IllegalArgumentException
	 *             if {@code refPolicyProviderMod} is null
	 */
	private BaseRefPolicyProvider(RefPolicyProviderModule refPolicyProviderMod) throws IllegalArgumentException
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
	 *            refPolicyProvider module factory for creating a module instance from configuration defined by {@code jaxbRefPolicyProvider}
	 * @param jaxbRefPolicyProvider
	 *            XML/JAXB configuration of RefPolicyProvider module
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference: PolicySet1 -> PolicySet2 -> ...
	 * @param expressionFactory
	 *            Expression factory for parsing XACML Expressions in the policies
	 * @param combiningAlgRegistry
	 *            Combining algorithm registry for getting implementations of algorithms used in the policies
	 * @param xacmlParserFactory
	 *            XACML parser factory
	 * @param environmentProperties
	 *            PDP configuration environment properties
	 * @throws IllegalArgumentException
	 *             if {@code jaxbRefPolicyProvider} or {@code refPolicyProviderModFactory} or {@code xacmlParserFactory} or {@code expressionFactory} or
	 *             {@code combiningAlgRegistry} is null
	 */
	public <CONF extends AbstractPolicyProvider> BaseRefPolicyProvider(CONF jaxbRefPolicyProvider,
			RefPolicyProviderModule.Factory<CONF> refPolicyProviderModFactory, XACMLParserFactory xacmlParserFactory, ExpressionFactory expressionFactory,
			CombiningAlgRegistry combiningAlgRegistry, int maxPolicySetRefDepth, EnvironmentProperties environmentProperties) throws IllegalArgumentException
	{
		this(refPolicyProviderModFactory == null ? null : refPolicyProviderModFactory.getInstance(jaxbRefPolicyProvider, xacmlParserFactory,
				maxPolicySetRefDepth, expressionFactory, combiningAlgRegistry, environmentProperties));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.policy.RefPolicyProvider#isStatic()
	 */
	@Override
	public boolean isStatic()
	{
		return refPolicyProviderMod.isStatic();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.policy.RefPolicyProvider#findPolicy(java.lang.String, com.sun.xacml.VersionConstraints, java.lang.Class, java.util.Deque)
	 */
	@Override
	public <P extends IPolicyEvaluator> P get(Class<P> refPolicyType, String idRef, VersionPatterns constraints, Deque<String> policySetRefChain)
			throws IndeterminateEvaluationException
	{
		/*
		 * It is the responsability of the refPolicyProviderMod to update policySetRefChain and check against maxPolicySetRefDepth - using
		 * RefPolicyProvider.Utils class - whenever a PolicySetIdReference is found in resolved PolicySets
		 */
		return refPolicyProviderMod.get(refPolicyType, idRef, constraints, policySetRefChain);
	}

	@Override
	public void close() throws IOException
	{
		this.refPolicyProviderMod.close();
	}
}

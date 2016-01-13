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

import java.io.IOException;

import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.XACMLParserFactory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

/**
 * Base class for static {@link RootPolicyProviderModule}s
 */
public abstract class BaseStaticRootPolicyProviderModule extends RootPolicyProviderModule.Static
{
	protected final BaseRefPolicyProvider refPolicyProvider;

	/**
	 * Creates instance
	 * 
	 * @param expressionFactory
	 *            (mandatory) Expression factory
	 * @param refPolicyProvider
	 *            referenced policy Provider; null iff Policy references not supported
	 * @param combiningAlgRegistry
	 *            (mandatory) registry of policy/rule combining algorithms
	 * @param jaxbRefPolicyProviderConf
	 *            (optional) XML/JAXB configuration of RefPolicyProvider module used for resolving Policy(Set)(Id)References in root policy; may be null if
	 *            support of PolicyReferences is disabled or this RootPolicyProvider module already supports these.
	 * @param maxPolicySetRefDepth
	 *            maximum depth of PolicySet reference chaining via PolicySetIdReference that is allowed in RefPolicyProvider derived from
	 *            {@code jaxbRefPolicyProviderConf}: PolicySet1 -> PolicySet2 -> ...; iff {@code jaxbRefPolicyProviderConf == null}, this parameter is ignored.
	 * @param xacmlParserFactory
	 *            XACML Parser Factory
	 * @param refPolicyProviderModFactory
	 *            refPolicyProvider module factory for creating a module instance from configuration defined by {@code jaxbRefPolicyProviderConf}
	 * @throws IllegalArgumentException
	 *             if {@code jaxbRefPolicyProviderConf != null && (expressionFactory == null || combiningAlgRegistry == null || xacmlParserFactory == null)}
	 */
	protected <CONF extends AbstractPolicyProvider> BaseStaticRootPolicyProviderModule(ExpressionFactory expressionFactory,
			CombiningAlgRegistry combiningAlgRegistry, XACMLParserFactory xacmlParserFactory, CONF jaxbRefPolicyProviderConf,
			RefPolicyProviderModule.Factory<CONF> refPolicyProviderModFactory, int maxPolicySetRefDepth, EnvironmentProperties envProps)
			throws IllegalArgumentException
	{
		// create ref-policy Provider
		if (jaxbRefPolicyProviderConf == null)
		{
			this.refPolicyProvider = null;
		} else
		{
			/*
			 * The refPolicyProviderModule is not instantiated here but in the BaseRefPolicyProvider since it is the one using this resource
			 * (refPolicyProviderModule), therefore responsible for closing it (call Closeable#close()) when it is done using them. We apply the basic principle
			 * that is the class creating the resource, that manages/closes it.
			 */
			this.refPolicyProvider = new BaseRefPolicyProvider(jaxbRefPolicyProviderConf, refPolicyProviderModFactory, xacmlParserFactory, expressionFactory,
					combiningAlgRegistry, maxPolicySetRefDepth, envProps);
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

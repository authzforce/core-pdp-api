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

import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils.XACMLParserFactory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

/**
 * This is the interface for all modules responsible for finding Policy(Set)s by
 * their Policy(Set)IdReference in a specific way (e.g. from a specific policy
 * repository).
 * <p>
 * Implements {@link Closeable} because it may may use resources external to the
 * JVM such as a cache, a disk, a connection to a remote server, etc. for
 * retrieving the policies. Therefore, these resources must be release by
 * calling {@link #close()} when it is no longer needed.
 * 
 */
public interface RefPolicyProviderModule extends RefPolicyProvider, Closeable
{
	/**
	 * RefPolicyProviderModule factory
	 * 
	 * @param <CONF_T>
	 *            type of configuration (XML-schema-derived) of the module
	 *            (initialization parameter)
	 * 
	 * 
	 */
	abstract class Factory<CONF_T extends AbstractPolicyProvider> extends JaxbBoundPdpExtension<CONF_T>
	{
		/**
		 * Create RefPolicyProviderModule instance
		 * 
		 * @param conf
		 *            module configuration
		 * @param xacmlParserFactory
		 *            XACML parser factory for parsing any XACML Policy(Set)
		 * @param maxPolicySetRefDepth
		 *            maximum allowed depth of PolicySet reference chain (via
		 *            PolicySetIdReference): PolicySet1 -> PolicySet2 -> ...; to
		 *            be enforced by any instance created by this factory. A
		 *            strictly negative value means no limit
		 * @param expressionFactory
		 *            Expression factory for parsing XACML Expressions in the
		 *            policies
		 * @param combiningAlgRegistry
		 *            Combining algorithm registry for getting implementations
		 *            of algorithms used in the policies
		 * @param environmentProperties
		 *            global PDP configuration environment properties
		 * 
		 * @return the module instance
		 * @throws IllegalArgumentException
		 *             if {@code conf} required but null
		 */
		public abstract RefPolicyProviderModule getInstance(CONF_T conf, XACMLParserFactory xacmlParserFactory, int maxPolicySetRefDepth, ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry, EnvironmentProperties environmentProperties) throws IllegalArgumentException;
	}
}

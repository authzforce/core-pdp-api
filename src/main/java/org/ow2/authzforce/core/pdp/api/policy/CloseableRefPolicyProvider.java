/**
 * Copyright 2012-2019 THALES.
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

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.JaxbBoundPdpExtension;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParserFactory;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

/**
 * {@link PolicyProvider} that implements {@link Closeable} because it may may use resources external to the JVM such as a cache, a disk, a connection to a remote server, etc. for retrieving the
 * policies. Therefore, these resources must be release by calling {@link #close()} when it is no longer needed.
 * <p>
 * PDP extensions of this type (to support new ways of providing policies by reference) must implement the {@link Factory} class
 */
public interface CloseableRefPolicyProvider extends PolicyProvider, Closeable
{
	/**
	 * RefPolicyProviderModule factory
	 * 
	 * @param <CONF_T>
	 *            type of configuration (XML-schema-derived) of the module (initialization parameter)
	 * 
	 * 
	 */
	abstract class Factory<CONF_T extends AbstractPolicyProvider> extends JaxbBoundPdpExtension<CONF_T>
	{
		/**
		 * Create RefPolicyProvider instance
		 * 
		 * @param conf
		 *            configuration
		 * @param xacmlParserFactory
		 *            XACML parser factory for parsing any XACML Policy(Set)
		 * @param maxPolicySetRefDepth
		 *            maximum allowed depth of PolicySet reference chain (via PolicySetIdReference): PolicySet1 -> PolicySet2 -> ...; to be enforced by any instance created by this factory. A strictly
		 *            negative value means no limit
		 * @param expressionFactory
		 *            Expression factory for parsing XACML Expressions in the policies
		 * @param combiningAlgRegistry
		 *            Combining algorithm registry for getting implementations of algorithms used in the policies
		 * @param environmentProperties
		 *            global PDP configuration environment properties
		 * 
		 * @return the instance
		 * @throws IllegalArgumentException
		 *             if {@code conf} required but null
		 */
		public abstract CloseableRefPolicyProvider getInstance(CONF_T conf, XmlnsFilteringParserFactory xacmlParserFactory, int maxPolicySetRefDepth, ExpressionFactory expressionFactory,
				CombiningAlgRegistry combiningAlgRegistry, EnvironmentProperties environmentProperties) throws IllegalArgumentException;
	}
}

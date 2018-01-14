/**
 * Copyright 2012-2018 Thales Services SAS.
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
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbBoundPdpExtension;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParserFactory;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

/**
 * This is the interface that all policy providers responsible for providing the root/top-level policy to evaluate must implement.
 * <p>
 * Implements {@link Closeable} because it may use resources external to the JVM such as a cache, a disk, a connection to a remote server, etc. for retrieving the root policy and any policy referenced
 * by it. Therefore, these resources must be released by calling {@link #close()} when it is no longer needed.
 * <p>
 * PDP extensions of this type (to support new ways of providing root policies) must implement the {@link Factory} class
 * 
 */
public interface RootPolicyProvider extends Closeable
{

	/**
	 * Tries to find one and only one matching policy given the request represented by the context data. If no policy is found, null must be returned.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of looking for a matching policy, null if none found matching the request; the returned root policy may have policy references (using a {@link RefPolicyProvider}) that are
	 *         not static
	 * @throws IllegalArgumentException
	 *             Error parsing a policy before matching. The Policy Provider implementation may parse policies lazily or on the fly, i.e. only when the policies are requested/looked for.
	 * @throws IndeterminateEvaluationException
	 *             if an error occurs determining the one policy matching the {@code context}, e.g. if more than one policy is found
	 */
	TopLevelPolicyElementEvaluator getPolicy(EvaluationContext context) throws IndeterminateEvaluationException, IllegalArgumentException;

	/**
	 * RootPolicyProvider factory
	 * 
	 * @param <ROOT_POLICY_PROVIDER_CONF>
	 *            type of configuration (XML-schema-derived)
	 * 
	 */
	abstract class Factory<ROOT_POLICY_PROVIDER_CONF extends AbstractPolicyProvider> extends JaxbBoundPdpExtension<ROOT_POLICY_PROVIDER_CONF>
	{
		/**
		 * Create RootPolicyProvider instance
		 * 
		 * @param conf
		 *            configuration
		 * @param xacmlParserFactory
		 *            XACML parser factory for parsing any XACML Policy(Set)
		 * @param expressionFactory
		 *            Expression factory for parsing Expressions in the root policy(set)
		 * @param combiningAlgRegistry
		 *            registry of combining algorithms for instantiating algorithms used in the root policy(set)
		 * @param refPolicyProvider
		 *            (optional) Policy-by-reference provider. It is the responsibility of the Root Policy Provider implementation to use this to resolve policy references, and close it with (@link
		 *            RefPolicyProvider#close()) when it is done using it, in particular when closing the root policy provider with {@link RootPolicyProvider#close()}. If not present, Policy
		 *            references are not supported.
		 * @param environmentProperties
		 *            PDP configuration environment properties
		 * 
		 * @return the root policy provider instance
		 * @throws IllegalArgumentException
		 *             invalid {@code conf}
		 */
		public abstract RootPolicyProvider getInstance(ROOT_POLICY_PROVIDER_CONF conf, XmlnsFilteringParserFactory xacmlParserFactory, ExpressionFactory expressionFactory,
				CombiningAlgRegistry combiningAlgRegistry, Optional<CloseableRefPolicyProvider> refPolicyProvider, EnvironmentProperties environmentProperties) throws IllegalArgumentException;
	}

}
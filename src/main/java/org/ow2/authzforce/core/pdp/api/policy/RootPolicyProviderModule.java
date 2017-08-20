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

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbBoundPdpExtension;
import org.ow2.authzforce.core.pdp.api.JaxbXacmlUtils.XACMLParserFactory;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

/**
 * This is the interface that all modules responsible for providing the root/top-level policy to evaluate.
 * <p>
 * Implements {@link Closeable} because it may may use resources external to the JVM such as a cache, a disk, a connection to a remote server, etc. for retrieving the root policy and any policy
 * referenced by it. Therefore, these resources must be released by calling {@link #close()} when it is no longer needed.
 * 
 */
public interface RootPolicyProviderModule extends Closeable
{

	/**
	 * Tries to find one and only one matching policy given the request represented by the context data. If no policies are found, null must be returned.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of looking for a matching policy, null if none found matching the request; the returned root policy may have policy references (using {@link RefPolicyProvider} that are not
	 *         static
	 * @throws IllegalArgumentException
	 *             Error parsing a policy before matching. The policy Provider module may parse policies lazily or on the fly, i.e. only when the policies are requested/looked for.
	 * @throws IndeterminateEvaluationException
	 *             if error determining the one policy matching the {@code context}, e.g. if more than one policy is found
	 */
	TopLevelPolicyElementEvaluator getPolicy(EvaluationContext context) throws IndeterminateEvaluationException, IllegalArgumentException;

	/**
	 * RootPolicyProviderModule factory
	 * 
	 * @param <ROOT_POLICY_PROVIDER_CONF>
	 *            type of configuration (XML-schema-derived) of the module (initialization parameter)
	 * 
	 */
	abstract class Factory<ROOT_POLICY_PROVIDER_CONF extends AbstractPolicyProvider> extends JaxbBoundPdpExtension<ROOT_POLICY_PROVIDER_CONF>
	{
		/**
		 * Create RootPolicyProviderModule instance
		 * 
		 * @param conf
		 *            module configuration
		 * @param xacmlParserFactory
		 *            XACML parser factory for parsing any XACML Policy(Set)
		 * @param expressionFactory
		 *            Expression factory for parsing Expressions in the root policy(set)
		 * @param combiningAlgRegistry
		 *            registry of combining algorithms for instantiating algorithms used in the root policy(set) *
		 * @param jaxbRefPolicyProviderConf
		 *            (optional) XML/JAXB configuration of RefPolicyProvider module used for resolving Policy(Set)(Id)References in root policy; may be null if support of PolicyReferences is disabled
		 *            or this RootPolicyProvider module already supports these. Used as argument to {@code refPolicyProviderModuleFactory.getInstance(REF_POLICY_PROVIDER_CONF)}
		 * @param maxPolicySetRefDepth
		 *            maximum depth of PolicySet reference chaining via PolicySetIdReference that is allowed in RefPolicyProvider derived from {@code jaxbRefPolicyProviderConf}: PolicySet1 ->
		 *            PolicySet2 -> ...; a strictly negative value means no limit. If and only if {@code jaxbRefPolicyProviderConf == null}, this parameter is ignored.
		 * @param refPolicyProviderModuleFactory
		 *            (optional) refPolicyProvider module factory for creating a module instance from configuration defined by {@code jaxbRefPolicyProviderConf} . May be null iff
		 *            {@code jaxbRefPolicyProviderConf == null}. If not null, it is the responsibility of the root Policy Provider implementation to use this and {@code jaxbRefPolicyProviderConf} as
		 *            argument to instantiate the ref Policy Provider, and close it with (@link RefPolicyProviderModule#close()) when it is done using it, in particular when closing the root policy
		 *            provider created by this factory (with {@link RootPolicyProviderModule#close()}).
		 * @param environmentProperties
		 *            PDP configuration environment properties
		 * 
		 * @return the root policy provider module instance
		 */
		public abstract <REF_POLICY_PROVIDER_CONF extends AbstractPolicyProvider> RootPolicyProviderModule getInstance(ROOT_POLICY_PROVIDER_CONF conf, XACMLParserFactory xacmlParserFactory,
				ExpressionFactory expressionFactory, CombiningAlgRegistry combiningAlgRegistry, REF_POLICY_PROVIDER_CONF jaxbRefPolicyProviderConf,
				RefPolicyProviderModule.Factory<REF_POLICY_PROVIDER_CONF> refPolicyProviderModuleFactory, int maxPolicySetRefDepth, EnvironmentProperties environmentProperties);
	}

}
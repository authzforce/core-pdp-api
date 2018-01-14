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
package org.ow2.authzforce.core.pdp.api;

import java.io.Closeable;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractAttributeProvider;

/**
 * {@link DesignatedAttributeProvider} that extends {@link Closeable} because it may may use resources external to the JVM such as a cache, a disk, a connection to a remote server, etc. for retrieving
 * the attribute values. Therefore, these resources must be released by calling {@link #close()} when it is no longer needed.
 * <p>
 * PDP extensions of this type (to support new ways of providing attributes) must implement the {@link FactoryBuilder} class.
 */
public interface CloseableDesignatedAttributeProvider extends DesignatedAttributeProvider, Closeable
{
	/**
	 * Intermediate dependency-aware {@link CloseableDesignatedAttributeProvider} factory that can create instances of Attribute Providers from a XML/JAXB configuration, and also provides the
	 * dependencies (required attributes) (based on this configuration), that any such instance (created by it) will need. Providing the dependencies helps to optimize the {@code depAttrProvider}
	 * argument to {@link #getInstance(AttributeValueFactoryRegistry, AttributeProvider)} and therefore optimize the created provider's job of finding its own supported attribute values based on other
	 * attributes in the evaluation context.
	 * 
	 */
	interface DependencyAwareFactory
	{

		/**
		 * Returns non-null <code>Set</code> of <code>AttributeDesignator</code> s required as runtime inputs to the Attribute Provider instance created by this builder. The PDP framework calls this
		 * method to know what input attributes the Provider will require (dependencies) before {@link #getInstance(AttributeValueFactoryRegistry, AttributeProvider)} , and based on this, creates a
		 * specific dependency Attribute Provider that will enable the providers created by this factory to find their dependency attributes. So when the PDP framework calls
		 * {@link #getInstance(AttributeValueFactoryRegistry, AttributeProvider)} subsequently to instantiate one those Providers, the last argument is this the special dependency Attribute Provider.
		 * 
		 * @return a <code>Set</code> of required <code>AttributeDesignatorType</code>s. Null or empty if none required.
		 */
		Set<AttributeDesignatorType> getDependencies();

		/**
		 * Create Attribute Provider instance
		 * 
		 * @param attributeValueFactoryRegistry
		 *            registry of Attribute value factories for the Provider to be able to create attribute values
		 * @param depAttrProvider
		 *            Existing Attribute Provider supplying the possibly required attributes that new Providers instantiated here will depend on
		 * 
		 * @return attribute value in internal model
		 */
		CloseableDesignatedAttributeProvider getInstance(AttributeValueFactoryRegistry attributeValueFactoryRegistry, AttributeProvider depAttrProvider);
	}

	/**
	 * Builder that creates a dependency-aware AttributeProvider factory from parsing the attribute dependencies (attributes on which the Providers created by this factory will depend on to find their
	 * own supported attributes) declared in the XML configuration (possibly dynamic).
	 * 
	 * @param <CONF_T>
	 *            type of configuration (XML-schema-derived)
	 * 
	 *            This class follows the Step Factory Pattern to guide clients through the creation of the object in a particular sequence of method calls:
	 *            <p>
	 *            http://rdafbn.blogspot.fr/2012/07/step-builder-pattern_28.html
	 *            </p>
	 */
	abstract class FactoryBuilder<CONF_T extends AbstractAttributeProvider> extends JaxbBoundPdpExtension<CONF_T>
	{

		/**
		 * Creates an attribute-dependency-aware AttributeProvider factory by inferring attribute dependencies (required attributes) from {@code conf}.
		 * 
		 * @param conf
		 *            configuration, that may define what attributes are required (dependency attributes)
		 * @param environmentProperties
		 *            global PDP configuration environment properties
		 * @return a factory aware of dependencies (required attributes) possibly inferred from input {@code conf}
		 */
		public abstract DependencyAwareFactory getInstance(CONF_T conf, EnvironmentProperties environmentProperties);
	}

}

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
package org.ow2.authzforce.core.pdp.api;

import java.io.Closeable;
import java.util.Set;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractAttributeProvider;

/**
 * Attribute provider module that implements {@link Closeable} because it may
 * may use resources external to the JVM such as a cache, a disk, a connection
 * to a remote server, etc. for retrieving the attribute values. Therefore,
 * these resources must be released by calling {@link #close()} when it is no
 * longer needed.
 */
public interface CloseableAttributeProviderModule extends AttributeProviderModule, Closeable {
	/**
	 * Intermediate dependency-aware {@link BaseAttributeProviderModule} factory
	 * that can create instances of modules from a XML/JAXB configuration, and
	 * also provides the dependencies (required attributes) (based on this
	 * configuration), that any such instance (created by it) will need.
	 * Providing the dependencies helps to optimize the {@code depAttrProvider}
	 * argument to
	 * {@link #getInstance(DatatypeFactoryRegistry, AttributeProvider)} and
	 * therefore optimize the created module's job of finding its own supported
	 * attribute values based on other attributes in the evaluation context.
	 * 
	 */
	interface DependencyAwareFactory {

		/**
		 * Returns non-null <code>Set</code> of <code>AttributeDesignator</code>
		 * s required as runtime inputs to the attribute Provider module
		 * instance created by this builder. The PDP framework calls this method
		 * to know what input attributes the module will require (dependencies)
		 * before
		 * {@link #getInstance(DatatypeFactoryRegistry, AttributeProvider)} ,
		 * and based on this, creates a specific dependency attribute Provider
		 * that will enable the module to find its dependency attributes. So
		 * when the PDP framework calls
		 * {@link #getInstance(DatatypeFactoryRegistry, AttributeProvider)}
		 * subsequently to instantiate the module, the last argument is this
		 * dependency attribute Provider.
		 * 
		 * @return a <code>Set</code> of required
		 *         <code>AttributeDesignatorType</code>s. Null or empty if none
		 *         required.
		 */
		Set<AttributeDesignatorType> getDependencies();

		/**
		 * Create AttributeProviderModule instance
		 * 
		 * @param attrDatatypeFactory
		 *            Attribute datatype factory for the module to be able to
		 *            create attribute values
		 * @param depAttrProvider
		 *            Attribute Provider for the module to find
		 *            dependency/required attributes
		 * 
		 * @return attribute value in internal model
		 */
		CloseableAttributeProviderModule getInstance(DatatypeFactoryRegistry attrDatatypeFactory,
				AttributeProvider depAttrProvider);
	}

	/**
	 * Builder that creates a dependency-aware AttributeProviderModule factory
	 * from parsing the attribute dependencies (attributes on which the module
	 * depends to find its own supported attributes) declared in the XML
	 * configuration (possibly dynamic).
	 * 
	 * @param <CONF_T>
	 *            type of configuration (XML-schema-derived) of the module
	 *            (initialization parameter)
	 * 
	 *            This class follows the Step Factory Pattern to guide clients
	 *            through the creation of the object in a particular sequence of
	 *            method calls:
	 *            <p>
	 *            http://rdafbn.blogspot.fr/2012/07/step-builder-pattern_28.html
	 *            </p>
	 */
	abstract class FactoryBuilder<CONF_T extends AbstractAttributeProvider> extends JaxbBoundPdpExtension<CONF_T> {

		/**
		 * Creates an attribute-dependency-aware module factory by inferring
		 * attribute dependencies (required attributes) from {@code conf}.
		 * 
		 * @param conf
		 *            module configuration, that may define what attributes are
		 *            required (dependency attributes)
		 * @param environmentProperties
		 *            global PDP configuration environment properties
		 * @return a factory aware of dependencies (required attributes)
		 *         possibly inferred from input {@code conf}
		 */
		public abstract DependencyAwareFactory getInstance(CONF_T conf, EnvironmentProperties environmentProperties);
	}

}

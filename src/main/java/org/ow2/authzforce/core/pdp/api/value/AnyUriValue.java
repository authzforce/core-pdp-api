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
package org.ow2.authzforce.core.pdp.api.value;

import net.sf.saxon.lib.StandardURIChecker;

/**
 * Represent the URI value that this class represents
 * <p>
 * WARNING: java.net.URI cannot be used here for this XACML datatype, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
 * </p>
 * <p>
 * [1] http://www.w3.org/TR/xmlschema-2/#anyURI That's why we use String instead.
 * </p>
 * <p>
 * See also:
 * </p>
 * <p>
 * https://java.net/projects/jaxb/lists/users/archive/2011-07/message/16
 * </p>
 * <p>
 * From the JAXB spec: "xs:anyURI is not bound to java.net.URI by default since not all possible values of xs:anyURI can be passed to the java.net.URI constructor. Using a global JAXB customization
 * described in Section 7.9".
 * </p>
 * <p>
 * Last but not least, we now refer to the definition of anyURI datatype given in XSD 1.1, which has the same value space as the string datatype. More info in the XSD 1.1 datatypes document and SAXON
 * documentation: http://www.saxonica.com/html/documentation9.4/changes/intro93/xsd11-93.html. Also confirmed on the mailing list:
 * https://sourceforge.net/p/saxon/mailman/saxon-help/thread/4F9E683E.8060001@saxonica.com/. Although XACML 3.0 still refers to XSD 1.0 and its stricter definition of anyURI, we prefer to anticipate
 * and use the definition from XSD 1.1 for XACML AttributeValues of datatype anyURI. However, this does not affect XACML schema validation of Policy/PolicySet/Request documents, where the XSD 1.0
 * definition of anyURI still applies.
 * </p>
 * <p>
 * With the new anyURI definition of XSD 1.1, we also avoid using {@link StandardURIChecker} which maintains a thread-local cache of validated URIs (cache size is 50 and eviction policy is LRU) that
 * may be spotted as a possible memory leak by servlet containers such as Tomcat, as confirmed on the mailing list: https://sourceforge.net/p/saxon/mailman/message/27043134/ ,
 * https://sourceforge.net/p/saxon/mailman/saxon-help/thread/4F9E683E.8060001@saxonica.com/ .
 * </p>
 *
 * 
 * @version $Id: $
 */
public final class AnyUriValue extends StringParseableValue<String>
{

	/**
	 * Creates a new <code>AnyURIAttributeValue</code> that represents the URI value supplied.
	 *
	 * @param value
	 *            the URI to be represented
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here for XACML datatype, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI. [1]
	 *            http://www.w3.org/TR/xmlschema-2/#anyURI So we use String instead.
	 *            </p>
	 * @throws java.lang.IllegalArgumentException
	 *             if {@code value} is not a valid string representation for xs:anyURI
	 */
	public AnyUriValue(final String value) throws IllegalArgumentException
	{
		super(value);
	}

	/** {@inheritDoc} */
	@Override
	public String printXML()
	{
		return this.value;
	}

}

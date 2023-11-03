/*
 * Copyright 2012-2023 THALES.
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

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The base type for all primitive/non-bag attribute values used in a policy or request/response. It is similar to {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType} except it is
 * turned into an interface for more flexibility, and it is not JAXB-annotated. Yet, it is designed to be mappable to an {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType}. Values
 * of all standard XACML primitive data-types extend this. Contrary to {@link PrimitiveValue}, this does not represent Functions which are not attribute values. If you want to provide a new type of
 * AttributeValue, i.e. new datatype, extend {@link AttributeValueFactory} to provide a factory for it.
 * <p>
 * <b>All implementations must implement/override {@link  Object#equals(Object)} and {@link Object#hashCode()} properly.</b>
 * </p>
 */
public interface AttributeValue extends PrimitiveValue
{

	/**
	 * Get the value content as specified by {@link jakarta.xml.bind.annotation.XmlMixed}, i.e. a list, each item of which may be a String, a {@link jakarta.xml.bind.JAXBElement}, an instance of a class
	 * annotated with @XmlRootElement, or a {@link org.w3c.dom.Element}. In addition, in the two latter cases, the item must also be {@link Serializable}.
	 * 
	 * @return (possibly mixed) content; <b>not null</b> (must be empty if no content)
	 */
	List<Serializable> getContent();

	/**
	 * Get the attributes attached to the value as specified by {@link jakarta.xml.bind.annotation.XmlAnyAttribute}, or any kind of attributes/metadata defined by some markup language that this value
	 * type is designed to be (de)serialized (from) to. The primary use of this is (de)serialization of XML attributes. Each key is an attribute's QName and the associated value is the attribute's
	 * string value. One example of such XML attribute in XACML standard is the xpathExpression value which has an XPathCategory attribute.
	 * 
	 * @return (possibly mixed) content ; <b>not null</b> (must be empty if no attribute)
	 */
	Map<QName, String> getXmlAttributes();
}

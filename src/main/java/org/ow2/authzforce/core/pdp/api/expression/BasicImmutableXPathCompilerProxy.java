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
package org.ow2.authzforce.core.pdp.api.expression;

import com.google.common.collect.ImmutableList;
import org.ow2.authzforce.xacml.identifiers.XPathVersion;

import java.util.Map;

/**
Basic Immutable XPathCompilerProxy that does not support XPath variables
 */
public final class BasicImmutableXPathCompilerProxy extends BaseXPathCompilerProxy
{
    /**
     * Constructor
     * @param xPathVersion XPath version
     * @param namespaceURIsByPrefix XML namespace URIs, indexed by prefix, usable in XPath expressions for the newly created instance
     */
    public BasicImmutableXPathCompilerProxy(XPathVersion xPathVersion, Map<String, String> namespaceURIsByPrefix)
    {
        super(xPathVersion, namespaceURIsByPrefix);
    }

    @Override
    public ImmutableList<VariableReference<?>> getAllowedVariables()
    {
        return ImmutableList.of();
    }
}

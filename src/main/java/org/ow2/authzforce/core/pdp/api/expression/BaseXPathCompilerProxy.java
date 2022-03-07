/*
 * Copyright 2012-2022 THALES.
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

import com.google.common.collect.ImmutableMap;
import net.sf.saxon.s9api.*;
import org.ow2.authzforce.core.pdp.api.XmlUtils;
import org.ow2.authzforce.xacml.identifiers.XPathVersion;

import java.util.Map;

/**
 * Base implementation of {@link XPathCompilerProxy}
 */
public abstract class BaseXPathCompilerProxy implements XPathCompilerProxy
{
    private final ImmutableMap<String, String> nsPrefixToUriMap;
    protected final XPathCompiler delegate;

    private final transient XPathVersion xPathVersion;

    /**
     * Creates namespace-aware XPathCompiler for a given XPath version
     * @param xpathVersion XPath version
     * @param namespacePrefixToUriMap namespace-prefix-to-URI mappings
     * @throws IllegalArgumentException if XPath version not supported
     */
    public BaseXPathCompilerProxy(final XPathVersion xpathVersion, final Map<String, String> namespacePrefixToUriMap) throws IllegalArgumentException
    {
        assert xpathVersion != null;
        this.xPathVersion = xpathVersion;
        this.nsPrefixToUriMap = namespacePrefixToUriMap == null ? ImmutableMap.of() : ImmutableMap.copyOf(namespacePrefixToUriMap);
        this.delegate = XmlUtils.newXPathCompiler(xPathVersion, namespacePrefixToUriMap);
        this.delegate.setCaching(true);
    }

    @Override
    public XPathVersion getXPathVersion()
    {
        return this.xPathVersion;
    }

    @Override
    public ImmutableMap<String, String> getDeclaredNamespacePrefixToUriMap()
    {
        return this.nsPrefixToUriMap;
    }

    @Override
    public XPathExecutable compile(String source) throws SaxonApiException
    {
        return delegate.compile(source);
    }

    @Override
    public XdmValue evaluate(String expression, XdmItem contextItem) throws SaxonApiException
    {
        return delegate.evaluate(expression, contextItem);
    }

    @Override
    public XdmItem evaluateSingle(String expression, XdmItem contextItem) throws SaxonApiException
    {
        return delegate.evaluateSingle(expression, contextItem);
    }

    @Override
    public XPathExecutable compilePattern(String source) throws SaxonApiException
    {
        return delegate.compilePattern(source);
    }
}

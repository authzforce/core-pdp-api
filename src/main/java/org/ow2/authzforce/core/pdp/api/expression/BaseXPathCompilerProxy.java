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
    private static final UnsupportedOperationException UNSUPPORTED_EVALUATE_OPERATION_EXCEPTION = new UnsupportedOperationException("XPathCompiler#evaluate(String, XdmItem) not supported");
    private static final UnsupportedOperationException UNSUPPORTED_EVALUATE_SINGLE_OPERATION_EXCEPTION = new UnsupportedOperationException("XPathCompiler#evaluateSingle(String, XdmItem) not supported");
    private static final UnsupportedOperationException UNSUPPORTED_COMPILE_PATTERN_OPERATION_EXCEPTION = new UnsupportedOperationException("XPathCompiler#compilePattern(String) not supported");

    /**
     * XML Namespace prefix-to-URI mapping
     */
    protected final ImmutableMap<String, String> nsPrefixToUriMap;

    /**
     * XPath version
     */
    protected final transient XPathVersion xPathVersion;

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
        /*
         * Why not reuse the same XPathCompiler over and over (make it a class member)? Because it is not immutable, calling XPathCompiler#compile(String) may change the internal state each time, e.g. if there are XPath variables in multiple sources, it is like calling XPathCompiler#declareVariables(...) without reinitializing, i.e. variables add up.
         */
        final XPathCompiler compiler = XmlUtils.newXPathCompiler(xPathVersion, nsPrefixToUriMap);
        return compiler.compile(source);
    }

    @Override
    public final XdmValue evaluate(String expression, XdmItem contextItem) throws SaxonApiException
    {
        throw UNSUPPORTED_EVALUATE_OPERATION_EXCEPTION;
    }

    @Override
    public final XdmItem evaluateSingle(String expression, XdmItem contextItem)
    {
        throw UNSUPPORTED_EVALUATE_SINGLE_OPERATION_EXCEPTION;
    }

    @Override
    public final XPathExecutable compilePattern(String source)
    {
        throw UNSUPPORTED_COMPILE_PATTERN_OPERATION_EXCEPTION;
    }
}

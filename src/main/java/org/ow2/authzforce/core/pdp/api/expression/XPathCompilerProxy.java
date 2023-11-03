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

import net.sf.saxon.s9api.*;
import org.ow2.authzforce.xacml.identifiers.XPathVersion;

import java.util.List;
import java.util.Map;

/**
 * Read-only view of {@link XPathCompiler}
 *
 */
public interface XPathCompilerProxy
{
    /**
     * @see XPathCompiler#getLanguageVersion()
     * @return XPath version supported
     */
    XPathVersion getXPathVersion();

    /**
     * Get namespace prefix-URI mappings to be part of the static context for XPath expressions compiled with this XPath compiler,
     * i.e. declared with {@link XPathCompiler#declareNamespace(String, String)}.
     * @return namespace prefix-URI bindings declared for this XPathCompiler
     */
    Map<String, String> getDeclaredNamespacePrefixToUriMap();

    /**
     * Get the list of XACML Policy Variables allowed as XPath variables in compiled XPath expressions
     * @return allowed variables
     */
    List<VariableReference<?>> getAllowedVariables();

    /**
     * @see XPathCompiler#compile(String)
     * @param source XPath expression
     * @return compiled XPath expression ready for evaluation
     * @throws SaxonApiException error compiling the expression (e.g. invalid XPath expression)
     */
    XPathExecutable compile(String source) throws SaxonApiException;

    /**
     * 
     * @see XPathCompiler#evaluate(String, XdmItem)
     * @param expression XPath expression
     * @param contextItem context item (e.g. XML node) on which to evaluate the expression
     * @return the result of the evaluation
     * @throws SaxonApiException evaluation error
     */
    XdmValue evaluate(String expression, XdmItem contextItem) throws SaxonApiException;

    /**
     *
     * @see XPathCompiler#evaluateSingle(String, XdmItem)
     * @param expression XPath expression
     * @param contextItem context item (e.g. XML node) on which to evaluate the expression
     * @return the result of the evaluation
     * @throws SaxonApiException evaluation error
     */
    XdmItem evaluateSingle(String expression, XdmItem contextItem) throws SaxonApiException;

    /**
     *
     * @see XPathCompiler#compilePattern(String)
     * @see XPathCompiler#compile(String)
     * @param source XPath expression
     * @return compiled XPath expression ready for evaluation
     * @throws SaxonApiException error compiling the expression (e.g. invalid XPath expression)
     */
    XPathExecutable compilePattern(String source) throws SaxonApiException;
}

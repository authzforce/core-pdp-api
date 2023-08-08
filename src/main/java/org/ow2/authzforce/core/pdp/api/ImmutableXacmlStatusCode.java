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
package org.ow2.authzforce.core.pdp.api;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusCode;

import javax.annotation.concurrent.Immutable;
import java.util.Optional;

/**
 * Immutable {@link StatusCode}
 */
@Immutable
public final class ImmutableXacmlStatusCode extends StatusCode
{
    private static final long serialVersionUID = 1L;
    /**
     * Creates status code with code value and optional nested status code
     * @param nextStatusCode nested status code
     * @param codeVal code value
     */
    public ImmutableXacmlStatusCode(String codeVal, Optional<ImmutableXacmlStatusCode> nextStatusCode)
    {
        super(nextStatusCode.orElse(null), codeVal);
    }
}

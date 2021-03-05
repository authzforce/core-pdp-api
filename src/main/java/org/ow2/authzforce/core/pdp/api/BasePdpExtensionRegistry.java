/*
 * Copyright 2012-2021 THALES.
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

import com.google.common.base.Preconditions;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is a base implementation of <code>PdpExtensionRegistry</code>. This should be used as basis to implement (in a final class) an immutable PDP extension registry of a specific type. If you need
 * a generic immutable PDP extension registry, see {
 *
 * @param <T>
 *            type of extension in this registry
 * @version $Id: $
 */
public abstract class BasePdpExtensionRegistry<T extends PdpExtension> implements PdpExtensionRegistry<T>
{
	private final Map<String, T> extensionsById;
	private final transient String toString;

	/**
	 * Instantiates immutable registry from a map.
	 *
	 * @param extensionClass
	 *            extension class
	 * @param extensionsById
	 *            extensions input map; the registry actually creates and uses an immutable copy of this map internally to avoid external modifications on the internal map
	 */
	protected BasePdpExtensionRegistry(final Class<? super T> extensionClass, final Map<String, ? extends T> extensionsById)
	{
		assert extensionClass != null && extensionsById != null;

		this.extensionsById = HashCollections.newImmutableMap(extensionsById);
		this.toString = this + "( extensionClass= " + extensionClass.getCanonicalName() + " )";
	}

	/** {@inheritDoc} */
	@Override
	public final T getExtension(final String identity)
	{
		return extensionsById.get(identity);
	}

	/** {@inheritDoc} */
	@Override
	public final Set<T> getExtensions()
	{
		return HashCollections.newImmutableSet(extensionsById.values());
	}

	@SuppressWarnings("unchecked")
	private static <E extends PdpExtension> Map<String, E> newImmutableMap(final Set<E> extensions)
	{
		return extensions.stream().collect(Collectors.toUnmodifiableMap(ext -> {assert ext != null; return Preconditions.checkNotNull(ext, "One of the input extensions is invalid (null)").getId();}, Function.identity()));
	}

	/**
	 * Instantiates immutable registry from a set of extensions
	 *
	 * @param extensionClass
	 *            extension class (required not null)
	 * @param extensions
	 *            extensions (required not null)
	 */
	protected BasePdpExtensionRegistry(final Class<? super T> extensionClass, final Set<? extends T> extensions)
	{
		this(extensionClass, newImmutableMap(extensions));
	}

	@Override
	public String toString()
	{
		return toString;
	}

}

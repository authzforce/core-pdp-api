package org.ow2.authzforce.core.pdp.api.func;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.ow2.authzforce.core.pdp.api.value.BagDatatype;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * Generic first-order function signatures (name, return type, arity, parameter types)
 */
public final class FirstOrderFunctionSignatures
{
	/**
	 * First-order function signature whose every parameters has the same datatype
	 * 
	 * @param <RETURN>
	 *            function's return type
	 * @param <PARAM>
	 *            common parameter type
	 */
	public static class SingleParameterTyped<RETURN extends Value, PARAM extends Value> extends FirstOrderFunctionSignature<RETURN>
	{
		private transient volatile int hashCode = 0; // Effective Java - Item 9

		private final List<? extends Datatype<PARAM>> paramTypes;

		/**
		 * Creates function signature
		 * 
		 * @param name
		 *            function name (e.g. XACML-defined URI)
		 * 
		 * @param returnType
		 *            function's return type
		 * @param parameterTypes
		 *            function parameter types. Note: the "? extends" allows to use {@link BagDatatype} as parameterType
		 * @param varArgs
		 *            true iff the function takes a variable number of arguments (like Java {@link Method#isVarArgs()}, i.e. the final type in <code>paramTypes</code> can be repeated 0 or more times
		 *            to match a variable-length argument
		 *            <p>
		 *            Examples with varargs=true ('...' means varargs like in Java):
		 *            </p>
		 *            <p>
		 *            Example 1: string-concat(string, string, string...) -> paramTypes={string, string, string}
		 *            </p>
		 *            <p>
		 *            Example 2: or(boolean...) -> paramTypes={boolean} (As you can see, isVarargs=true really means 0 or more args; indeed, the or function can take 0 parameter according to spec)
		 *            </p>
		 *            <p>
		 *            Example 3: n-of(integer, boolean...) -> paramTypes={integer, boolean}
		 *            </p>
		 * @throws IllegalArgumentException
		 *             if ( {@code name == null || returnType == null || parameterTypes == null || parameterTypes.isEmpty()})
		 */
		public SingleParameterTyped(String name, Datatype<RETURN> returnType, boolean varArgs, List<? extends Datatype<PARAM>> parameterTypes) throws IllegalArgumentException
		{
			super(name, returnType, varArgs);
			if (parameterTypes == null)
			{
				throw UNDEF_PARAMETER_TYPES_EXCEPTION;
			}

			if (parameterTypes.isEmpty())
			{
				throw new IllegalArgumentException("Invalid number of function parameters (" + parameterTypes.size() + ") for first-order function (" + name + "). Required: >= 1.");
			}

			this.paramTypes = Collections.unmodifiableList(parameterTypes);
		}

		/**
		 * Get single/common parameter datatype
		 * 
		 * @return parameter datatype
		 */
		public Datatype<PARAM> getParameterType()
		{
			// the constructor made sure that paramTypes is not empty
			return this.paramTypes.get(0);
		}

		/**
		 * Get function parameter types
		 * 
		 * @return function parameter types
		 */
		@Override
		public List<? extends Datatype<?>> getParameterTypes()
		{
			return this.paramTypes;
		}

		@Override
		public int hashCode()
		{
			// immutable class -> cache hashCode
			if (hashCode == 0)
			{
				hashCode = Objects.hash(name, returnType, isVarArgs, paramTypes.get(0));
			}

			return hashCode;
		}

		@Override
		public boolean equals(Object obj)
		{
			// Effective Java - Item 8
			if (this == obj)
			{
				return true;
			}

			if (!(obj instanceof SingleParameterTyped))
			{
				return false;
			}

			final SingleParameterTyped<?, ?> other = (SingleParameterTyped<?, ?>) obj;
			return isVarArgs == other.isVarArgs && name.equals(other.name) && returnType.equals(other.returnType) && this.paramTypes.get(0).equals(other.paramTypes.get(0));
		}
	}

	/**
	 * First-order function signature whose parameters have (at least two) different datatypes
	 * 
	 * @param <RETURN>
	 *            function's return type
	 */
	public static class MultiParameterTyped<RETURN extends Value> extends FirstOrderFunctionSignature<RETURN>
	{

		private transient volatile int hashCode = 0; // Effective Java - Item 9

		private final List<? extends Datatype<?>> paramTypes;

		/**
		 * 
		 * @param name
		 * @param returnType
		 * @param varArgs
		 * @param parameterTypes
		 * @throws IllegalArgumentException
		 *             if ( {@code name == null || returnType == null || parameterTypes == null || parameterTypes.size() < 2 })
		 */
		MultiParameterTyped(String name, Datatype<RETURN> returnType, boolean varArgs, List<? extends Datatype<?>> parameterTypes) throws IllegalArgumentException
		{
			super(name, returnType, varArgs);
			if (parameterTypes == null)
			{
				throw UNDEF_PARAMETER_TYPES_EXCEPTION;
			}

			if (parameterTypes.size() < 2)
			{
				throw new IllegalArgumentException("Invalid number of function parameters (" + parameterTypes.size() + ") for multi-parameter-typed function (" + name + "). Required: >= " + 2 + ".");
			}
			this.paramTypes = Collections.unmodifiableList(parameterTypes);
		}

		/**
		 * Get function parameter types
		 * 
		 * @return function parameter types
		 */
		@Override
		public List<? extends Datatype<?>> getParameterTypes()
		{
			return this.paramTypes;
		}

		@Override
		public int hashCode()
		{
			// immutable class -> cache hashCode
			if (hashCode == 0)
			{
				hashCode = Objects.hash(name, returnType, isVarArgs, paramTypes);
			}

			return hashCode;
		}

		@Override
		public boolean equals(Object obj)
		{
			// Effective Java - Item 8
			if (this == obj)
			{
				return true;
			}

			if (!(obj instanceof MultiParameterTyped))
			{
				return false;
			}

			final MultiParameterTyped<?> other = (MultiParameterTyped<?>) obj;
			return isVarArgs == other.isVarArgs && name.equals(other.name) && returnType.equals(other.returnType) && this.paramTypes.equals(other.paramTypes);
		}
	}

	private FirstOrderFunctionSignatures()
	{
		// constructor is private to prevent instantiation
	}

}

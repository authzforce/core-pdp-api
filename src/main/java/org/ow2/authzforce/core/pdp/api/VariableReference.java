/**
 * Copyright (C) 2012-2015 Thales Services SAS.
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

/**
 * Variable reference, i.e. reference to a variable definition. Variables are simply Expressions identified by an ID (VariableId) and replace original XACML
 * VariableReferences for actual evaluation.
 * 
 * @param <V>
 *            reference evaluation's return type
 */
public interface VariableReference<V extends Value> extends Expression<V>
{

	/**
	 * Get the referenced variable's definition's expression. For example, used to check whether the actual expression type behind a variable reference is a
	 * Function in Higher-order function's arguments
	 * 
	 * @return the expression
	 */
	Expression<?> getReferencedExpression();

	/**
	 * Returns referenced variable ID
	 * 
	 * @return referenced variable ID
	 */
	String getVariableId();

}
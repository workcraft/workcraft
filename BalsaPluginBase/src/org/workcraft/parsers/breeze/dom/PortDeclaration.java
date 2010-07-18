/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.parsers.breeze.dom;

import org.workcraft.parsers.breeze.expressions.Constant;
import org.workcraft.parsers.breeze.expressions.Expression;


public abstract class PortDeclaration
{
	public PortDeclaration(String name, boolean isActive) {
		this.name = name;
		this.isActive = isActive;
	}

	private final String name;
	private final boolean isActive;

	public String getName() {
		return name;
	}

	public final Expression<Integer> count()
	{
		return accept(new PortVisitor<Expression<Integer>>()
		{
			@Override public Expression<Integer> visit(ArrayedDataPortDeclaration port) {
				return port.getCount();
			}

			@Override public Expression<Integer> visit(ArrayedSyncPortDeclaration port) {
				return port.getCount();
			}

			@Override public Expression<Integer> visit(SyncPortDeclaration port) {
				return new Constant<Integer>(1);
			}

			@Override public Expression<Integer> visit(DataPortDeclaration port) {
				return new Constant<Integer>(1);
			}

			@Override public Expression<Integer> visit(FullDataPortDeclaration port) {
				return new Constant<Integer>(1);
			}
		}
		);
	}

	public abstract <T> T accept(PortVisitor<T> visitor);

	public final boolean isArrayed() {
		return accept(new PortVisitor<Boolean>()
				{
					@Override public Boolean visit(ArrayedDataPortDeclaration port) {
						return true;
					}

					@Override public Boolean visit(ArrayedSyncPortDeclaration port) {
						return true;
					}

					@Override public Boolean visit(SyncPortDeclaration port) {
						return false;
					}

					@Override public Boolean visit(DataPortDeclaration port) {
						return false;
					}

					@Override public Boolean visit(FullDataPortDeclaration port) {
						return false;
					}
				}
				);
	}

	public boolean isActive() {
		return isActive;
	}

	public static PortDeclaration createSync(String portName, boolean active) {
		return new SyncPortDeclaration(portName, active);
	}

	public static PortDeclaration createData(String portName, boolean active, boolean input, Expression<Integer> width) {
		return new DataPortDeclaration(portName, active, input, width);
	}

	public static PortDeclaration createArrayedSync(String portName, boolean active, Expression<Integer> count) {
		return new ArrayedSyncPortDeclaration(portName, active, count);
	}

	public static PortDeclaration createArrayedData(String portName, boolean active, Expression<Integer> count, boolean input, Expression<Integer[]> width) {
		return new ArrayedDataPortDeclaration(portName, active, count, input, width);
	}
}

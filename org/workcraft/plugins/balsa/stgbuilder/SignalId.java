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

package org.workcraft.plugins.balsa.stgbuilder;

public class SignalId {
	private final Object owner;
	private final String name;
	public SignalId(Object owner, String name)
	{
		if(owner == null || name == null)
			throw new NullPointerException("Argument is null");
		this.owner = owner;
		this.name = name;
	}
	public Object getOwner()
	{
		return owner;
	}
	public String getName()
	{
		return name;
	}

	@Override
	public int hashCode()
	{
		return owner.hashCode() * 31 + name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof SignalId))
			return false;
		SignalId other = (SignalId)obj;

		return
			owner.equals(other.owner) &&
			name.equals(other.name);
	}
}

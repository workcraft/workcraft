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

package org.workcraft.parsers.breeze;

import java.util.List;

public class LispNode {
	private List<LispNode> subNodes;
	private String value;

	@Override
	public String toString()
	{
		if(value != null)
			return "\"" + value + "\"";
		else
		{
			String s = "(";
			for(LispNode node : subNodes)
				s += node.toString() + " ";
			return s + ")";
		}
	}

	public LispNode(String value)
	{
		if(value == null)
			throw new NullPointerException("value == null");
		this.value = value;
	}

	public LispNode(List<LispNode> list)
	{
		if(list == null)
			throw new NullPointerException("list == null");
		this.subNodes = list;
	}

	public List<LispNode> getSubNodes() {
		return subNodes;
	}

	public String getValue()
	{
		return value;
	}
}

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

import java.util.ArrayList;
import java.util.Arrays;


public interface ParameterValueList
{
	public int size();
	public String get(int index);

	static class StringList extends ArrayList<String> implements ParameterValueList
	{
		public StringList(String... values)
		{
			super(Arrays.asList(values));
		}
		private static final long serialVersionUID = 1L;
	}
}

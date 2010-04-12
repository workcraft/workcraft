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

package org.workcraft.util;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class ConstructorParametersMatcher
{
	private static class ConstructorInfo implements MethodParametersMatcher.MethodInfo
	{
		public ConstructorInfo (Constructor<?> constructor)
		{
			this.constructor = constructor;
			this.parameterTypes = constructor.getParameterTypes();
		}
		public final Constructor<?> constructor;
		private final Class<?>[] parameterTypes;

		public Class<?>[] getParameterTypes() {
			return parameterTypes;
		}
	}

	public Constructor<?> match(Class<?> c, Class<?>... parameters) throws NoSuchMethodException
	{
		ArrayList<ConstructorInfo> constructors = new ArrayList<ConstructorInfo>();
		for(Constructor<?> constructor : c.getConstructors())
			constructors.add(new ConstructorInfo(constructor));

		try
		{
			return MethodParametersMatcher.match(constructors, parameters).constructor;
		}
		catch(NoSuchMethodException e)
		{
			String s = "";
			for(Class<?> parameter : parameters)
			{
				if(s.length()>0)
					s+=", ";
				s += parameter.getCanonicalName();
			}
			throw new NoSuchMethodException("Unable to find a constructor for class " + c.getCanonicalName() + " with parameters (" + s + ")");
		}
	}
}

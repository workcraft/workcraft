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

		return MethodParametersMatcher.match(constructors, parameters).constructor;
	}
}

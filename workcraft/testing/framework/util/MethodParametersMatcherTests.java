package org.workcraft.testing.framework.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.util.AmbiguousMethodException;
import org.workcraft.util.MethodParametersMatcher;
import org.workcraft.util.MethodParametersMatcher.MethodInfo;

public class MethodParametersMatcherTests {
	class A
	{

	}

	class AB extends A
	{

	}

	class AC extends A
	{

	}

	class ABq extends AB
	{

	}

	class ABp extends AB
	{

	}

	int match(Class<?> type, Class<?>... parameters) throws Exception
	{
		TestMethodInfo match;
		try
		{
			match = MethodParametersMatcher.match(getMethods(type), parameters);
		}
		catch(NoSuchMethodException e)
		{
			return -1;
		}
		catch(AmbiguousMethodException e)
		{
			return -2;
		}
		return match.execute();
	}


	static class simple{
		public static int qq(){return 1;};
	}

	@Test
	public void TestSimple() throws Exception
	{
		Assert.assertEquals(1, match(simple.class));
		Assert.assertEquals(-1, match(simple.class, Object.class));
	}

	static class advanced{
		public static int qq(){return 1;};
		public static int qq(A a){return 2;};
		public static int qq(ABq abq){return 3;};
		public static int qq(AB ab){return 4;};
		public static int qq(AC ac){return 5;};
	}

	@Test
	public void TestMostSpecific() throws Exception
	{
		Assert.assertEquals(1, match(advanced.class));
		Assert.assertEquals(2, match(advanced.class, A.class));
		Assert.assertEquals(3, match(advanced.class, ABq.class));
		Assert.assertEquals(4, match(advanced.class, ABp.class));
		Assert.assertEquals(4, match(advanced.class, AB.class));
		Assert.assertEquals(5, match(advanced.class, AC.class));
	}

	static class ambiguous{
		public static int qq(A a, AC b){return 1;};
		public static int qq(ABq a, A b){return 2;};
	}


	@Test
	public void TestAmbiguous() throws Exception
	{
		Assert.assertEquals(1, match(ambiguous.class, A.class, AC.class));
		Assert.assertEquals(-1, match(ambiguous.class, AB.class, A.class));
		Assert.assertEquals(2, match(ambiguous.class, ABq.class, A.class));
		Assert.assertEquals(1, match(ambiguous.class, AB.class, AC.class));
		Assert.assertEquals(-2, match(ambiguous.class, ABq.class, AC.class));
	}

	class TestMethodInfo implements MethodInfo
	{
		private final Method method;

		public TestMethodInfo(Method method) {
			this.method = method;
		}

		public Class<?>[] getParameterTypes() {
			return method.getParameterTypes();
		}

		public int execute() throws Exception
		{
			Object [] args = new Object[getParameterTypes().length];
			return (Integer)method.invoke(null, args);
		}
	}

	private Collection<TestMethodInfo> getMethods(Class<?> clasz) {
		ArrayList<TestMethodInfo> result = new ArrayList<TestMethodInfo>();

		for(Method method : clasz.getMethods())
			if(method.getName() == "qq")
				result.add(new TestMethodInfo(method));

		return result;
	}
}

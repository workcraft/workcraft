package org.workcraft.testing.plugins.petri;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.DuplicateIDException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.plugins.stg.InstanceManager;
import org.workcraft.util.Func;
import org.workcraft.util.Pair;

public class InstanceManagerTests
{
	@Test(expected=NullPointerException.class)
	public void testConstructorNull()
	{
		new InstanceManager<Object>(null);
	}

	@Test
	public void testConstructor()
	{
		new InstanceManager<Object>(new Func<Object, String>()
				{
					@Override public String eval(Object arg) {
						throw new RuntimeException("this method should not be called");
					}
				});
	}

	InstanceManager<Object> make(final Map<Object, String> expectedRequests)
	{
		return new InstanceManager<Object>(new Func<Object, String>()
				{
			@Override public String eval(Object arg) {
				final String label = expectedRequests.get(arg);
				if(label==null)
					throw new RuntimeException("unexpected request: " + arg);
				return label;
			}
		});
	}

	@Test
	public void testGetReferenceUnknown()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		final InstanceManager<Object> mgr = make(expectedRequests);
		assertNull(mgr.getInstance(new Object()));
	}

	@Test
	public void testAssign()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		Object o1 = new Object();
		Object o2 = new Object();
		Object o3 = new Object();
		expectedRequests.put(o1, "abc");
		expectedRequests.put(o2, "abc");
		expectedRequests.put(o3, "qwe");
		final InstanceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1);
		mgr.assign(o2);
		mgr.assign(o3);
		assertEquals(Pair.of("abc",1), mgr.getInstance(o2));
		assertEquals(Pair.of("qwe",0), mgr.getInstance(o3));
		assertEquals(Pair.of("abc",0), mgr.getInstance(o1));
	}

	@Test
	public void testAssignAfterRemove()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		Object o1 = new Object();
		Object o2 = new Object();
		Object o3 = new Object();
		Object o4 = new Object();
		expectedRequests.put(o1, "abc");
		expectedRequests.put(o2, "abc");
		expectedRequests.put(o3, "qwe");
		expectedRequests.put(o4, "abc");
		final InstanceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1);
		mgr.assign(o2);
		mgr.assign(o3);
		assertEquals(Pair.of("abc",1), mgr.getInstance(o2));
		assertEquals(Pair.of("qwe",0), mgr.getInstance(o3));
		assertEquals(Pair.of("abc",0), mgr.getInstance(o1));

		mgr.assign(o2, 1);
		mgr.assign(o2, 2);
		mgr.assign(o4);

		assertEquals(Pair.of("abc",1), mgr.getInstance(o4));
	}

	@Test
	public void testRemove()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		Object o1 = new Object();
		Object o2 = new Object();
		expectedRequests.put(o1, "abc");
		expectedRequests.put(o2, "abc");
		final InstanceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1);
		mgr.remove(o1);
		mgr.assign(o2);
		assertEquals(Pair.of("abc",0), mgr.getInstance(o2));
		assertNull(mgr.getInstance(o1));
	}


	@Test(expected=ArgumentException.class)
	public void testDoubleAssign()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		Object o1 = new Object();
		expectedRequests.put(o1, "abc");
		final InstanceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1);
		mgr.assign(o1);
	}

	@Test
	public void testAssignForced()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		Object o1 = new Object();
		expectedRequests.put(o1, "abc");
		final InstanceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1, 8);
		assertEquals(Pair.of("abc",8), mgr.getInstance(o1));
	}

	@Test(expected=DuplicateIDException.class)
	public void testAssignForcedExistingId()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		Object o1 = new Object();
		Object o2 = new Object();
		expectedRequests.put(o1, "abc");
		expectedRequests.put(o2, "abc");
		final InstanceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1, 8);
		mgr.assign(o2, 8);
	}

	@Test
	public void testNotFound()
	{
		InstanceManager<Object> mgr = new InstanceManager<Object>(new Func<Object, String>() {
			@Override
			public String eval(Object arg) {
				return "O_O";
			} });

		assertNull(mgr.getObject(Pair.of("o_O", 8)));
	}

}

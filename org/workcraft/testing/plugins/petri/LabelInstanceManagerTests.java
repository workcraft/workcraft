package org.workcraft.testing.plugins.petri;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.DuplicateIDException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.plugins.petri.ReferenceManager;
import org.workcraft.util.Func;

public class LabelInstanceManagerTests
{
	@Test(expected=NullPointerException.class)
	public void testConstructorNull()
	{
		new ReferenceManager<Object>(null);
	}

	@Test
	public void testConstructor()
	{
		new ReferenceManager<Object>(new Func<Object, String>()
				{
					@Override public String eval(Object arg) {
						throw new RuntimeException("this method should not be called");
					}
				});
	}

	ReferenceManager<Object> make(final Map<Object, String> expectedRequests)
	{
		return new ReferenceManager<Object>(new Func<Object, String>()
				{
			@Override public String eval(Object arg) {
				final String label = expectedRequests.get(arg);
				if(label==null)
					throw new RuntimeException("unexpected request: " + arg);
				return label;
			}
		});
	}

	@Test(expected=NotFoundException.class)
	public void testGetReferenceUnknown()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		final ReferenceManager<Object> mgr = make(expectedRequests);
		mgr.getReference(new Object());
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
		final ReferenceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1);
		mgr.assign(o2);
		mgr.assign(o3);
		assertEquals("abc/1", mgr.getReference(o2));
		assertEquals("qwe/0", mgr.getReference(o3));
		assertEquals("abc/0", mgr.getReference(o1));
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
		final ReferenceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1);
		mgr.assign(o2);
		mgr.assign(o3);
		assertEquals("abc/1", mgr.getReference(o2));
		assertEquals("qwe/0", mgr.getReference(o3));
		assertEquals("abc/0", mgr.getReference(o1));

		mgr.assign(o2, "abc/1");
		mgr.assign(o2, "abc/2");
		mgr.assign(o4);

		assertEquals ("abc/1", mgr.getReference(o4));

	}

	@Test(expected=NotFoundException.class)
	public void testRemove()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		Object o1 = new Object();
		Object o2 = new Object();
		expectedRequests.put(o1, "abc");
		expectedRequests.put(o2, "abc");
		final ReferenceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1);
		mgr.remove(o1);
		mgr.assign(o2);
		assertEquals("abc/0", mgr.getReference(o2));
		mgr.getReference(o1);
	}


	@Test(expected=ArgumentException.class)
	public void testDoubleAssign()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		Object o1 = new Object();
		expectedRequests.put(o1, "abc");
		final ReferenceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1);
		mgr.assign(o1);
	}

	@Test(expected=NullPointerException.class)
	public void testAssignForcedNull()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		Object o1 = new Object();
		expectedRequests.put(o1, "abc");
		final ReferenceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1, null);
	}

	@Test(expected=ArgumentException.class)
	public void testAssignForcedWrongLabelFormat()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		Object o1 = new Object();
		expectedRequests.put(o1, "abc");
		final ReferenceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1, "kojozz");
	}

	@Test(expected=ArgumentException.class)
	public void testAssignForcedWrongLabel()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		Object o1 = new Object();
		expectedRequests.put(o1, "abc");
		final ReferenceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1, "kojozz/8");
	}

	@Test
	public void testAssignForced()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		Object o1 = new Object();
		expectedRequests.put(o1, "abc");
		final ReferenceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1, "abc/8");
		assertEquals("abc/8", mgr.getReference(o1));
	}

	@Test(expected=DuplicateIDException.class)
	public void testAssignForcedExistingId()
	{
		Map<Object, String> expectedRequests = new HashMap<Object, String>();
		Object o1 = new Object();
		Object o2 = new Object();
		expectedRequests.put(o1, "abc");
		expectedRequests.put(o2, "abc");
		final ReferenceManager<Object> mgr = make(expectedRequests);
		mgr.assign(o1, "abc/8");
		mgr.assign(o2, "abc/8");
	}

}

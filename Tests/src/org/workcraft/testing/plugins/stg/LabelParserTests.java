package org.workcraft.testing.plugins.stg;

import static org.junit.Assert.*;

import org.junit.Test;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.stg.LabelParser;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.Triple;

public class LabelParserTests {

	@Test
	public void testNoInstance() {
		Triple<String, Direction, Integer> result = LabelParser.parseFull("a+");

		assertEquals("a", result.getFirst());
		assertEquals(Direction.PLUS, result.getSecond());
		assertEquals(null, result.getThird());

	}

	@Test
	public void testInstance() {
		Triple<String, Direction, Integer> result = LabelParser.parseFull("a+/4");

		assertEquals("a", result.getFirst());
		assertEquals(Direction.PLUS, result.getSecond());
		assertEquals(new Integer(4), result.getThird());

	}

	@Test(expected = ArgumentException.class)
	public void testWrongFormat1() {
		LabelParser.parseFull("x/");
	}

	@Test(expected = ArgumentException.class)
	public void testWrongFormat2() {
		LabelParser.parseFull("x@/3");
	}

	@Test(expected = ArgumentException.class)
	public void testWrongFormat3() {
		LabelParser.parseFull("x-/fifty");
	}

}
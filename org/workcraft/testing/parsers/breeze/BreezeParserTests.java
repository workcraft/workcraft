package org.workcraft.testing.parsers.breeze;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.workcraft.parsers.breeze.dom.BreezeFile;
import org.workcraft.parsers.breeze.javacc.BreezeParser;
import org.workcraft.parsers.breeze.javacc.ParseException;

public class BreezeParserTests {
	@Test
	public void test1() throws ParseException
	{
		InputStream vdStream = BreezeParserTests.class.getResourceAsStream("data/VD.breeze");

		BreezeFile parsed = new BreezeParser(vdStream).breezeFile();

	}
}

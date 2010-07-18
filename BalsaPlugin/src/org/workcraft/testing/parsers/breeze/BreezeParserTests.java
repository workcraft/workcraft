package org.workcraft.testing.parsers.breeze;

import java.io.InputStream;

import org.junit.Test;
import org.workcraft.parsers.breeze.dom.BreezeFile;
import org.workcraft.parsers.breeze.javacc.generated.BreezeParser;
import org.workcraft.parsers.breeze.javacc.generated.ParseException;

public class BreezeParserTests {
	@Test
	public void test1() throws ParseException
	{
		InputStream vdStream = BreezeParserTests.class.getResourceAsStream("data/VD.breeze");

		BreezeFile parsed = new BreezeParser(vdStream).breezeFile();

	}
}

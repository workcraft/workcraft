package org.workcraft.testing.dom.hierarchy;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.Test;
import org.workcraft.dom.references.HierarchicalNames;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.util.Pair;



public class HierarchicalUniqueNameReferenceManagerTest {

	static HashMap<String, Pair<String, String>> headTails = new HashMap<String, Pair<String, String>>(){
		private static final long serialVersionUID = -2931077011392124649L;
	{
		put("/'abc'/'dfe'", new Pair<String, String>("abc","/'dfe'"));
		put("/'abc'/'dfe'/'asdf'", new Pair<String, String>("abc","/'dfe'/'asdf'"));
		put("/abc/dfe/asdf", new Pair<String, String>("abc","/dfe/asdf"));


		put("/abc/1/dfe/asdf", new Pair<String, String>("abc/1","/dfe/asdf"));

		put("a/b/c+/33", new Pair<String, String>("a","/b/c+/33"));

		put("/'abc/dfe'/asdf", new Pair<String, String>("abc/dfe","/asdf"));

	}};

	static HashMap<String, String> flatNames = new HashMap<String, String>(){
		private static final long serialVersionUID = -2931077011392124649L;
	{
		put("/abc/dfe", "__abc__dfe");
		put("/'abc'/'dfe'", "__abc__dfe");
		put("/'abc'/'dfe'/'asdf'", "__abc__dfe__asdf");
		put("/abc/dfe/asdf", "__abc__dfe__asdf");
		put("/abc/dfe/asdf/1", "__abc__dfe__asdf/1");
		put("/abc/dfe/asdf+", "__abc__dfe__asdf+");
		put("/abc/dfe/asdf+/12", "__abc__dfe__asdf+/12");
	}};

	@Test
	public void testFlatNames() {
		for (Entry<String, String> en: flatNames.entrySet()) {

			String hName = en.getKey();
			String fName = en.getValue();

			String answer = HierarchicalNames.getFlatName(hName, null);
			assertTrue(answer.equals(fName));

			hName = hName.replaceAll("'", "");
			answer = HierarchicalNames.flatToHierarchicalName(fName, null);
			assertTrue(answer.equals(hName));

		}
	}

	@Test
	public void testGetReferenceHead() {
		for (Entry<String, Pair<String, String>> en: headTails.entrySet()) {
			String reference = en.getKey();
			String head = en.getValue().getFirst();
			String answer = HierarchicalNames.getReferenceHead(reference);
			assertTrue(answer.equals(head));
		}
	}

	@Test
	public void testGetReferenceTail() {
		for (Entry<String, Pair<String, String>> en: headTails.entrySet()) {
			String reference = en.getKey();
			String tail = en.getValue().getSecond();
			String answer = HierarchicalNames.getReferenceTail(reference);
			assertTrue(answer.equals(tail));
		}
	}

}

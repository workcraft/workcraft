package org.workcraft.testing.dom.hierarchy;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.Test;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.util.Pair;



public class HierarchicalUniqueNameReferenceManagerTest {

	static HashMap<String, Pair<String, String>> headTails = new HashMap<String, Pair<String, String>>(){
		private static final long serialVersionUID = -2931077011392124649L;
	{
		put("/\"abc\"/\"dfe\"", new Pair<String, String>("abc","\"dfe\""));
		put("/\"\"/\"dfe\"", 	new Pair<String, String>("","dfe"));
		put("/\"abc\"", 		new Pair<String, String>("abc",""));
		put("abc", 				new Pair<String, String>("abc",""));
		put("", 				new Pair<String, String>("",""));

	}};


	@Test
	public void testGetReferenceHead() {
		for (Entry<String, Pair<String, String>> en: headTails.entrySet()) {
			String reference = en.getKey();
			String head = en.getValue().getFirst();
			assert(HierarchicalUniqueNameReferenceManager.getReferenceHead(reference).equals(head));
		}
	}

	@Test
	public void testGetReferenceTail() {
		for (Entry<String, Pair<String, String>> en: headTails.entrySet()) {
			String reference = en.getKey();
			String tail = en.getValue().getSecond();
			assert(HierarchicalUniqueNameReferenceManager.getReferenceHead(reference).equals(tail));
		}
	}

}

package org.workcraft.dom.hierarchy;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.Test;
import org.workcraft.util.Pair;

public class HierarchicalUniqueNameReferenceManagerTest {

    static HashMap<String, Pair<String, String>> headTails = new HashMap<String, Pair<String, String>>() {
        private static final long serialVersionUID = -2931077011392124649L;
        {
            put("abc", new Pair<String, String>("abc", ""));
            put("abc.def", new Pair<String, String>("abc", ".def"));
            put("abc.def.ghi", new Pair<String, String>("abc", ".def.ghi"));
            put("abc/123.def.ghi", new Pair<String, String>("abc/123", ".def.ghi"));
            put("a.b.c+/123", new Pair<String, String>("a", ".b.c+/123"));
        }
    };

    @Test
    public void testGetReferenceHead() {
        for (Entry<String, Pair<String, String>> en: headTails.entrySet()) {
            String reference = en.getKey();
            String head = en.getValue().getFirst();
            String answer = NamespaceHelper.getReferenceHead(reference);
            assertEquals(answer, head);
        }
    }

    @Test
    public void testGetReferenceTail() {
        for (Entry<String, Pair<String, String>> en: headTails.entrySet()) {
            String reference = en.getKey();
            String tail = en.getValue().getSecond();
            String answer = NamespaceHelper.getReferenceTail(reference);
            assertEquals(answer, tail);
        }
    }

    static HashMap<String, String> referencePaths = new HashMap<String, String>() {
        private static final long serialVersionUID = -2931077011392124649L;
        {
            put("abc", "");
            put("abc.def", "abc.");
            put("abc.def.ghi", "abc.def.");
            put("abc.def.ghi/123", "abc.def.");
            put("abc.def.ghi+", "abc.def.");
            put("abc.def.ghi+/123", "abc.def.");
        }
    };

    @Test
    public void testReferencePaths() {
        for (Entry<String, String> en: referencePaths.entrySet()) {
            String reference = en.getKey();
            String path = en.getValue();
            String answer = NamespaceHelper.getReferencePath(reference);
            assertEquals(answer, path);
        }
    }

}

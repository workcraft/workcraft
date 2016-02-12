package org.workcraft.testing.dom.hierarchy;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.Test;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.util.Pair;

public class HierarchicalUniqueNameReferenceManagerTest {

    static HashMap<String, Pair<String, String>> headTails = new HashMap<String, Pair<String, String>>(){
        private static final long serialVersionUID = -2931077011392124649L;
        {
            put("/'abc'/'dfe'", new Pair<String, String>("abc", "/'dfe'"));
            put("/'abc'/'dfe'/'asdf'", new Pair<String, String>("abc", "/'dfe'/'asdf'"));
            put("/abc/dfe/asdf", new Pair<String, String>("abc", "/dfe/asdf"));

            put("/abc/1/dfe/asdf", new Pair<String, String>("abc/1", "/dfe/asdf"));

            put("a/b/c+/33", new Pair<String, String>("a", "/b/c+/33"));

            put("/'abc/dfe'/asdf", new Pair<String, String>("abc/dfe", "/asdf"));
        }
    };

    @Test
    public void testGetReferenceHead() {
        for (Entry<String, Pair<String, String>> en: headTails.entrySet()) {
            String reference = en.getKey();
            String head = en.getValue().getFirst();
            String answer = NamespaceHelper.getReferenceHead(reference);
            assertTrue(answer.equals(head));
        }
    }

    @Test
    public void testGetReferenceTail() {
        for (Entry<String, Pair<String, String>> en: headTails.entrySet()) {
            String reference = en.getKey();
            String tail = en.getValue().getSecond();
            String answer = NamespaceHelper.getReferenceTail(reference);
            assertTrue(answer.equals(tail));
        }
    }

    static HashMap<String, String> flatNames = new HashMap<String, String>(){
        private static final long serialVersionUID = -2931077011392124649L;
        {
            put("abc/dfe", "abc__dfe");
            put("'abc'/'dfe'", "abc__dfe");
            put("'abc'/'dfe'/'asdf'", "abc__dfe__asdf");
            put("abc/dfe/asdf", "abc__dfe__asdf");
            put("abc/dfe/asdf/1", "abc__dfe__asdf/1");
            put("abc/dfe/asdf+", "abc__dfe__asdf+");
            put("abc/dfe/asdf+/12", "abc__dfe__asdf+/12");
        }
    };

    @Test
    public void testFlatNames() {
        for (Entry<String, String> en: flatNames.entrySet()) {

            String hName = en.getKey();
            String fName = en.getValue();

            String answer = NamespaceHelper.hierarchicalToFlatName(hName);
            assertTrue(answer.equals(fName));

            hName = hName.replaceAll("'", "");
            answer = NamespaceHelper.flatToHierarchicalName(fName);
            assertTrue(answer.equals(hName));

        }
    }

    static HashMap<String, String> referencePaths = new HashMap<String, String>(){
        private static final long serialVersionUID = -2931077011392124649L;
        {
            put("abc", "");
            put("/abc", "");
            put("abc/dfe", "abc/");
            put("'abc'/'dfe'", "abc/");
            put("'abc'/'dfe'/'asdf'", "abc/dfe/");
            put("abc/dfe/asdf", "abc/dfe/");
            put("abc/dfe/asdf/1", "abc/dfe/");
            put("abc/dfe/asdf+", "abc/dfe/");
            put("abc/dfe/asdf+/12", "abc/dfe/");
        }
    };

    @Test
    public void testReferencePaths() {
        for (Entry<String, String> en: referencePaths.entrySet()) {

            String reference = en.getKey();
            String path = en.getValue();

            String answer = NamespaceHelper.getReferencePath(reference);
            assertTrue(answer.equals(path));

        }
    }

}

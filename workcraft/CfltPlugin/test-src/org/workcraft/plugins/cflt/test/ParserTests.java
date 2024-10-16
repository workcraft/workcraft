package org.workcraft.plugins.cflt.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cflt.node.Node;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.plugins.cflt.node.Operator;
import org.workcraft.plugins.cflt.jj.petri.ParseException;
import org.workcraft.plugins.cflt.jj.petri.PetriStringParser;
import org.workcraft.plugins.cflt.jj.petri.TokenMgrError;
import org.workcraft.utils.DialogUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

class ParserTests {

    @Test
    void precedenceAndNodeGenerationTest() {
        String testExpression = "A|B#C;(D|{E})";
        InputStream is = new ByteArrayInputStream(testExpression.getBytes(StandardCharsets.UTF_8));
        PetriStringParser parser = new PetriStringParser(is);
        NodeCollection nodeCollection = null;

        try {
            nodeCollection = parser.parse();
        } catch (ParseException e) {
            DialogUtils.showError(e.getMessage(), "Parse Exception");
            e.printStackTrace();
        } catch (TokenMgrError e) {
            DialogUtils.showError(e.getMessage(), "Error");
            e.printStackTrace();
        }

        assert nodeCollection != null;
        List<Node> nodes = nodeCollection.getNodes();

        Assertions.assertEquals("E", nodeCollection.getNodeDetails(nodes.get(0).leftChildName()).getLabel());
        Assertions.assertEquals(Operator.ITERATION, nodes.get(0).operator());

        Assertions.assertEquals("D", nodeCollection.getNodeDetails(nodes.get(1).leftChildName()).getLabel());
        Assertions.assertEquals("E", nodeCollection.getNodeDetails(nodes.get(1).rightChildName()).getLabel());
        Assertions.assertEquals(Operator.CONCURRENCY, nodes.get(1).operator());

        Assertions.assertEquals("C", nodeCollection.getNodeDetails(nodes.get(2).leftChildName()).getLabel());
        Assertions.assertEquals("D", nodeCollection.getNodeDetails(nodes.get(2).rightChildName()).getLabel());
        Assertions.assertEquals(Operator.SEQUENCE, nodes.get(2).operator());

        Assertions.assertEquals("B", nodeCollection.getNodeDetails(nodes.get(3).leftChildName()).getLabel());
        Assertions.assertEquals("C", nodeCollection.getNodeDetails(nodes.get(3).rightChildName()).getLabel());
        Assertions.assertEquals(Operator.CHOICE, nodes.get(3).operator());

        Assertions.assertEquals("A", nodeCollection.getNodeDetails(nodes.get(4).leftChildName()).getLabel());
        Assertions.assertEquals("B", nodeCollection.getNodeDetails(nodes.get(4).rightChildName()).getLabel());
        Assertions.assertEquals(Operator.CONCURRENCY, nodes.get(4).operator());
    }
}
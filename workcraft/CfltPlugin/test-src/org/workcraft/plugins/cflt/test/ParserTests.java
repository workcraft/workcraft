package org.workcraft.plugins.cflt.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cflt.node.Node;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.plugins.cflt.node.NodeDetails;
import org.workcraft.plugins.cflt.node.Operator;
import org.workcraft.plugins.cflt.jj.petri.ParseException;
import org.workcraft.plugins.cflt.jj.petri.PetriStringParser;
import org.workcraft.plugins.cflt.jj.petri.TokenMgrError;

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
        } catch (ParseException | TokenMgrError e) {
            e.printStackTrace();
        }

        assert nodeCollection != null;
        List<Node> nodes = nodeCollection.getNodes();

        String[] leftLabels = {"E", "D", "C", "B", "A"};
        String[] rightLabels = {"E", "E", "D", "C", "B"};
        Operator[] operators = {
                Operator.ITERATION,
                Operator.CONCURRENCY,
                Operator.SEQUENCE,
                Operator.CHOICE,
                Operator.CONCURRENCY,
        };

        for (int i = 0; i < nodes.size(); i++) {
            String leftChildName = nodes.get(i).leftChildName();
            String rightChildName = nodes.get(i).rightChildName();

            NodeDetails leftChildNodeDetails = nodeCollection.getNodeDetails(leftChildName);
            NodeDetails rightChildNodeDetails = nodeCollection.getNodeDetails(rightChildName);

            Assertions.assertEquals(leftLabels[i], leftChildNodeDetails.getLabel());
            Assertions.assertEquals(rightLabels[i], rightChildNodeDetails.getLabel());
            Assertions.assertEquals(operators[i], nodes.get(i).operator());
        }

    }
}
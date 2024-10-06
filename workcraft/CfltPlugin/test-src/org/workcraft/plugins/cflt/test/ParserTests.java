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

        NodeCollection nodeCollection = NodeCollection.getInstance();

        try {
            parser.parse(testExpression);
        } catch (ParseException e) {
            DialogUtils.showError(e.getMessage(), "Parse Exception");
            e.printStackTrace();
        } catch (TokenMgrError e) {
            DialogUtils.showError(e.getMessage(), "Error");
            e.printStackTrace();
        }

        List<Node> nodes = nodeCollection.getNodes();

        Assertions.assertEquals(nodes.get(0).getLeftChildName(), "E");
        Assertions.assertEquals(nodes.get(0).getOperator(), Operator.ITERATION);

        Assertions.assertEquals(nodes.get(1).getLeftChildName(), "D");
        Assertions.assertEquals(nodes.get(1).getRightChildName(), "E");
        Assertions.assertEquals(nodes.get(1).getOperator(), Operator.CONCURRENCY);

        Assertions.assertEquals(nodes.get(2).getLeftChildName(), "C");
        Assertions.assertEquals(nodes.get(2).getRightChildName(), "D");
        Assertions.assertEquals(nodes.get(2).getOperator(), Operator.SEQUENCE);

        Assertions.assertEquals(nodes.get(3).getLeftChildName(), "B");
        Assertions.assertEquals(nodes.get(3).getRightChildName(), "C");
        Assertions.assertEquals(nodes.get(3).getOperator(), Operator.CHOICE);

        Assertions.assertEquals(nodes.get(4).getLeftChildName(), "A");
        Assertions.assertEquals(nodes.get(4).getRightChildName(), "B");
        Assertions.assertEquals(nodes.get(4).getOperator(), Operator.CONCURRENCY);

    }

}
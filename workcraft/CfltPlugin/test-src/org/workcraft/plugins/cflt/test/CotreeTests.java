package org.workcraft.plugins.cflt.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cflt.Operator;
import org.workcraft.plugins.cflt.jj.petri.ParseException;
import org.workcraft.plugins.cflt.jj.petri.PetriStringParser;
import org.workcraft.plugins.cflt.jj.petri.TokenMgrError;
import org.workcraft.plugins.cflt.tools.CotreeTool;
import org.workcraft.utils.DialogUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class CotreeTests {

    @Test
    void precedenceAndNodeGenerationTest() {
        String testExpression = "A|B#C;(D|{E})";
        InputStream is = new ByteArrayInputStream(testExpression.getBytes(StandardCharsets.UTF_8));
        PetriStringParser parser = new PetriStringParser(is);

        try {
            parser.parse(testExpression);
        } catch (ParseException e) {
            DialogUtils.showError(e.getMessage(), "Parse Exception");
            e.printStackTrace();
        } catch (TokenMgrError e) {
            DialogUtils.showError(e.getMessage(), "Error");
            e.printStackTrace();
        }
        Assertions.assertEquals(CotreeTool.nodes.get(0).getLeft(), "E");
        Assertions.assertEquals(CotreeTool.nodes.get(0).getRight(), "E");
        Assertions.assertEquals(CotreeTool.nodes.get(0).getOperator(), Operator.ITERATION);

        Assertions.assertEquals(CotreeTool.nodes.get(1).getLeft(), "D");
        Assertions.assertEquals(CotreeTool.nodes.get(1).getRight(), "E");
        Assertions.assertEquals(CotreeTool.nodes.get(1).getOperator(), Operator.CONCURRENCY);

        Assertions.assertEquals(CotreeTool.nodes.get(2).getLeft(), "C");
        Assertions.assertEquals(CotreeTool.nodes.get(2).getRight(), "D");
        Assertions.assertEquals(CotreeTool.nodes.get(2).getOperator(), Operator.SEQUENCE);

        Assertions.assertEquals(CotreeTool.nodes.get(3).getLeft(), "B");
        Assertions.assertEquals(CotreeTool.nodes.get(3).getRight(), "C");
        Assertions.assertEquals(CotreeTool.nodes.get(3).getOperator(), Operator.CHOICE);

        Assertions.assertEquals(CotreeTool.nodes.get(4).getLeft(), "A");
        Assertions.assertEquals(CotreeTool.nodes.get(4).getRight(), "B");
        Assertions.assertEquals(CotreeTool.nodes.get(4).getOperator(), Operator.CONCURRENCY);
    }

}
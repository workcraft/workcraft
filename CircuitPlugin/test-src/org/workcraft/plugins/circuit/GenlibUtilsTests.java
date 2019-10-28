package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.jj.BooleanFormulaParser;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.Library;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.types.Pair;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GenlibUtilsTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        CircuitSettings.setGateLibrary(TestUtils.getLibraryPath("workcraft.lib"));
    }

    @Test
    public void testReadLibrary() {
        Set<String> gateNames = new HashSet<>(Arrays.asList(
                "BUF", "INV",
                "AND2", "NAND2", "NAND2B", "AND3", "NAND3", "NAND3B", "AND4", "NAND4", "NAND4B", "NAND4BB",
                "OR2", "NOR2", "NOR2B", "OR3", "NOR3", "NOR3B", "OR4", "NOR4", "NOR4B", "NOR4BB",
                "AOI21", "AOI211", "AOI22", "AOI221", "AOI222", "AOI31", "AOI32", "AOI33", "AOI2BB1", "AOI2BB2", "AO21", "AO22",
                "OAI21", "OAI211", "OAI22", "OAI221", "OAI222", "OAI31", "OAI32", "OAI33", "OAI2BB1", "OAI2BB2", "OA21", "OA22",
                "LOGIC1", "LOGIC0",
                "C2", "NC2"));

        Assert.assertEquals(gateNames, LibraryManager.getLibrary().getNames());
    }

    @Test
    public void testMapping() throws ParseException {
        Library gateLibrary = LibraryManager.getLibrary();
        checkMapping(BooleanFormulaParser.parse("1"), gateLibrary, gateLibrary.get("LOGIC1"));
        checkMapping(BooleanFormulaParser.parse("0"), gateLibrary, gateLibrary.get("LOGIC0"));
        checkMapping(BooleanFormulaParser.parse("!a"), gateLibrary, gateLibrary.get("INV"));
        checkMapping(BooleanFormulaParser.parse("a"), gateLibrary, gateLibrary.get("BUF"));
        checkMapping(BooleanFormulaParser.parse("a*b"), gateLibrary, gateLibrary.get("AND2"));
        checkMapping(BooleanFormulaParser.parse("a+b"), gateLibrary, gateLibrary.get("OR2"));
        checkMapping(BooleanFormulaParser.parse("a^b"), gateLibrary, gateLibrary.get("XOR2"));
        checkMapping(BooleanFormulaParser.parse("!(a*b)"), gateLibrary, gateLibrary.get("NAND2"));
        checkMapping(BooleanFormulaParser.parse("!(a+b)"), gateLibrary, gateLibrary.get("NOR2"));
        checkMapping(BooleanFormulaParser.parse("a=>b"), gateLibrary, gateLibrary.get("NAND2B"));
        checkMapping(BooleanFormulaParser.parse("a*c+b*!c"), gateLibrary, null);
        checkMapping(BooleanFormulaParser.parse("a=b"), gateLibrary, null);
        checkMapping(BooleanFormulaParser.parse("(a+b)*(c+d)"), gateLibrary, gateLibrary.get("OA22"));
        checkMapping(BooleanFormulaParser.parse("!(a*b+!c*!d)"), gateLibrary, gateLibrary.get("AOI2BB2"));
    }

    private void checkMapping(BooleanFormula func, Library gateLibrary, Gate expGate) {
        Pair<Gate, Map<BooleanVariable, String>> mapping = GenlibUtils.findMapping(func, gateLibrary);
        if (expGate == null) {
            Assert.assertNull(mapping);
        } else {
            Assert.assertNotNull(mapping);
        }
        if (mapping == null) {
            System.out.println(StringGenerator.toString(func) + " == ?");
        } else {
            Gate gate = mapping.getFirst();
            Assert.assertEquals(expGate.name + "!=" + gate.name, expGate, gate);
            System.out.print(StringGenerator.toString(func) + " == " + gate.function.formula + " [");
            Map<BooleanVariable, String> assignments = mapping.getSecond();
            for (Map.Entry<BooleanVariable, String> entry : assignments.entrySet()) {
                System.out.print(" " + entry.getKey().getLabel() + " -> " + entry.getValue() + " ");
            }
            System.out.println("]");
        }
    }
}

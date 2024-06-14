package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.jj.BooleanFormulaParser;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.Library;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.types.Pair;
import org.workcraft.utils.BackendUtils;

import java.util.*;

class GenlibUtilsTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib"));
    }

    @Test
    void testReadLibrary() {
        Set<String> gateNames = new HashSet<>(Arrays.asList(
                "BUF", "INV",
                "AND2", "NAND2", "NAND2B", "AND3", "NAND3", "NAND3B", "AND4", "NAND4", "NAND4B", "NAND4BB",
                "OR2", "NOR2", "NOR2B", "OR3", "NOR3", "NOR3B", "OR4", "NOR4", "NOR4B", "NOR4BB",
                "AOI21", "AOI211", "AOI22", "AOI221", "AOI222", "AOI31", "AOI32", "AOI33", "AOI2BB1", "AOI2BB2", "AO21", "AO22",
                "OAI21", "OAI211", "OAI22", "OAI221", "OAI222", "OAI31", "OAI32", "OAI33", "OAI2BB1", "OAI2BB2", "OA21", "OA22",
                "LOGIC1", "LOGIC0",
                "C2", "NC2"));

        Assertions.assertEquals(gateNames, LibraryManager.getLibrary().getNames());
    }

    @Test
    void testMapping() throws ParseException {
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
        checkMapping(BooleanFormulaParser.parse("!(!a*!b+!c*!d)"), gateLibrary, gateLibrary.get("OA22"));
        checkMapping(BooleanFormulaParser.parse("!(a*b+!c*!d)"), gateLibrary, gateLibrary.get("AOI2BB2"));
    }

    private void checkMapping(BooleanFormula func, Library gateLibrary, Gate expGate) {
        Pair<Gate, Map<BooleanVariable, String>> mapping = GenlibUtils.findMapping(func, gateLibrary);
        if (expGate == null) {
            Assertions.assertNull(mapping);
        } else {
            Assertions.assertNotNull(mapping);
        }
        if (mapping == null) {
            System.out.println(StringGenerator.toString(func) + " == ?");
        } else {
            Gate gate = mapping.getFirst();
            Assertions.assertEquals(expGate, gate, expGate.name + "!=" + gate.name);
            System.out.print(StringGenerator.toString(func) + " == " + gate.function.formula + " [");
            Map<BooleanVariable, String> assignments = mapping.getSecond();
            for (Map.Entry<BooleanVariable, String> entry : assignments.entrySet()) {
                String varLabel = entry.getKey().getLabel();
                String gatePinName = entry.getValue();
                System.out.print(' ' + varLabel + " -> " + gatePinName + ' ');
            }
            System.out.println(']');
        }
    }

    @Test
    void testExtendedMapping() throws ParseException {
        Library gateLibrary = LibraryManager.getLibrary();
        checkExtendedMapping(BooleanFormulaParser.parse("1"), gateLibrary, gateLibrary.get("LOGIC1"), Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("0"), gateLibrary, gateLibrary.get("LOGIC0"), Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("!a"), gateLibrary, gateLibrary.get("INV"), Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("a"), gateLibrary, gateLibrary.get("BUF"), Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("a*b"), gateLibrary, gateLibrary.get("AND2"), Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("a+b"), gateLibrary, gateLibrary.get("OR2"), Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("a^b"), gateLibrary, gateLibrary.get("XOR2"), Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("!(a*b)"), gateLibrary, gateLibrary.get("NAND2"), Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("!(a+b)"), gateLibrary, gateLibrary.get("NOR2"), Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("a=>b"), gateLibrary, gateLibrary.get("NAND2B"), Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("a*c+b*!c"), gateLibrary, null, Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("a=b"), gateLibrary, null, Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("(a+b)*(c+d)"), gateLibrary, gateLibrary.get("OA22"), Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("!(a*b+!c*!d)"), gateLibrary, gateLibrary.get("AOI2BB2"), Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("!(!a*!b+!c*!d)"), gateLibrary, gateLibrary.get("OA22"), Set.of());
        checkExtendedMapping(BooleanFormulaParser.parse("!(!a*b+!c*!d)"), gateLibrary, gateLibrary.get("AOI22"), Set.of("A1", "B1", "B2"));
    }

    private void checkExtendedMapping(BooleanFormula func, Library gateLibrary, Gate expGate, Set<String> expInvertedPins) {
        Pair<Gate, Map<BooleanVariable, Pair<String, Boolean>>> mapping = GenlibUtils.findExtendedMapping(func, gateLibrary);
        String funcText = StringGenerator.toString(func);
        if (expGate == null) {
            Assertions.assertNull(mapping, "Unexpected mapping found " + funcText);
        } else {
            Assertions.assertNotNull(mapping, "No mapping found for " + funcText);
        }
        if (mapping == null) {
            System.out.println(funcText + " == ?");
        } else {
            Gate gate = mapping.getFirst();
            Assertions.assertEquals(expGate, gate, expGate.name + "!=" + gate.name);
            System.out.print(funcText + " == " + gate.function.formula + " [");
            Map<BooleanVariable, Pair<String, Boolean>> assignments = mapping.getSecond();
            Set<String> invertedPins = new HashSet<>();
            List<BooleanVariable> orderedVars = FormulaUtils.extractOrderedVariables(func);
            for (BooleanVariable var : orderedVars) {
                Pair<String, Boolean> extendedAssignment = assignments.get(var);
                String varLabel = var.getLabel();
                Assertions.assertNotNull(extendedAssignment, "Assignment is missing for variable" + varLabel);
                String gatePinName = extendedAssignment.getFirst();
                Boolean gatePinInversion = extendedAssignment.getSecond();
                System.out.print(' ' + varLabel + " -> " + gatePinName + (gatePinInversion ? "' " : ' '));
                if (gatePinInversion) {
                    invertedPins.add(gatePinName);
                }
            }
            System.out.println(']');
            Assertions.assertEquals(expInvertedPins, invertedPins);
        }
    }

    @Test
    void testPinCount() {
        Library gateLibrary = LibraryManager.getLibrary();
        Assertions.assertEquals(0, GenlibUtils.getPinCount(null));
        Assertions.assertEquals(2, GenlibUtils.getPinCount(gateLibrary.get("BUF")));
        Assertions.assertEquals(1, GenlibUtils.getPinCount(gateLibrary.get("LOGIC1")));
        Assertions.assertEquals(3, GenlibUtils.getPinCount(gateLibrary.get("C2")));
        Assertions.assertEquals(4, GenlibUtils.getPinCount(gateLibrary.get("OR3")));
        Assertions.assertEquals(5, GenlibUtils.getPinCount(gateLibrary.get("NAND4BB")));
        Assertions.assertEquals(7, GenlibUtils.getPinCount(gateLibrary.get("AOI222")));
    }

}

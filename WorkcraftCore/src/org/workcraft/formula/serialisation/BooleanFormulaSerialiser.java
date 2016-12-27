package org.workcraft.formula.serialisation;

import org.w3c.dom.Element;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.utils.FormulaToString;
import org.workcraft.formula.utils.FormulaToString.PrinterSuite;
import org.workcraft.formula.utils.FormulaToString.Void;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;
import org.workcraft.util.Identifier;

public abstract class BooleanFormulaSerialiser implements CustomXMLSerialiser {
    @Override
    public void serialise(Element element, Object object, final ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) throws SerialisationException {
        BooleanFormula formula = getFormula(object);
        String attributeName = "formula";

        writeFormulaAttribute(element, internalReferences, formula, attributeName);
    }

    public static void writeFormulaAttribute(Element element, final ReferenceProducer internalReferences, BooleanFormula formula, String attributeName) {

        if (formula == null) {
            element.setAttribute(attributeName, "");
            return;
        }

        PrinterSuite printers = new FormulaToString.PrinterSuite();
        printers.vars = new FormulaToString.VariablePrinter() {
            @Override
            public Void visit(BooleanVariable node) {

                String ref = internalReferences.getReference(node);
                // use full path to a flattened name
                String flat = NamespaceHelper.hierarchicalToFlatName(ref);

                // old style naming, if number is used as an ID for a contact
                if (Identifier.isNumber(ref)) {
                    append("var_" + ref);
                } else {
                    append(flat);
                }

                return null;
            }
        };

        printers.init();

        formula.accept(printers.iff);
        String string = printers.builder.toString();

        element.setAttribute(attributeName, string);
    }

    protected abstract BooleanFormula getFormula(Object serialisee);
}

package org.workcraft.formula.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.utils.StringGenerator;
import org.workcraft.formula.utils.StringGenerator.PrinterSuite;
import org.workcraft.formula.utils.StringGenerator.Void;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;
import org.workcraft.dom.references.Identifier;

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

        PrinterSuite printers = new StringGenerator.PrinterSuite();
        printers.vars = new StringGenerator.VariablePrinter() {
            @Override
            public Void visit(BooleanVariable node) {

                String ref = internalReferences.getReference(node);
                // old style naming, if number is used as an ID for a contact
                if (Identifier.isNumber(ref)) {
                    append("var_" + ref);
                } else {
                    append(ref);
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

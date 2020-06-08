package org.workcraft.formula.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.formula.visitors.StringGenerator.PrinterSuite;
import org.workcraft.formula.visitors.StringGenerator.Void;
import org.workcraft.serialisation.CustomXMLSerialiser;
import org.workcraft.serialisation.NodeSerialiser;
import org.workcraft.serialisation.ReferenceProducer;

public abstract class BooleanFormulaSerialiser<T> implements CustomXMLSerialiser<T> {

    @Override
    public void serialise(Element element, T object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) throws SerialisationException {

        BooleanFormula formula = getFormula(object);
        String attributeName = "formula";

        writeFormulaAttribute(element, internalReferences, formula, attributeName);
    }

    public static void writeFormulaAttribute(Element element, final ReferenceProducer internalReferences,
            BooleanFormula formula, String attributeName) {

        if (formula == null) {
            element.setAttribute(attributeName, "");
            return;
        }

        PrinterSuite printers = new StringGenerator.PrinterSuite();
        printers.vars = new StringGenerator.VariablePrinter() {
            @Override
            public Void visit(BooleanVariable node) {
                append(internalReferences.getReference(node));
                return null;
            }
        };

        printers.init();

        formula.accept(printers.iff);
        String string = printers.builder.toString();

        element.setAttribute(attributeName, string);
    }

    protected abstract BooleanFormula getFormula(T serialisee);

}

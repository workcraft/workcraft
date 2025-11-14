package org.workcraft.formula.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.jj.BooleanFormulaParser;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.serialisation.CustomXMLDeserialiser;
import org.workcraft.serialisation.NodeFinaliser;
import org.workcraft.serialisation.NodeInitialiser;
import org.workcraft.serialisation.ReferenceResolver;

public abstract class BooleanFormulaDeserialiser<T> implements CustomXMLDeserialiser<T> {

    @Override
    public void finaliseInstance(Element element, T instance, final ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) throws DeserialisationException {

        String string = element.getAttribute("formula");
        try {
            // FIXME: On copy-paste formula may miss the variables defined outside of the copied part
            BooleanFormula formula = parseFormula(string, internalReferenceResolver);
            setFormula(instance, formula);
        } catch (DeserialisationException ignored) {
        }
    }

    public static BooleanFormula parseFormula(String string, final ReferenceResolver internalReferenceResolver)
            throws DeserialisationException {

        try {
            return BooleanFormulaParser.parse(string, name ->
                    (internalReferenceResolver.getObject(name) instanceof BooleanVariable var) ? var : null);
        } catch (ParseException e) {
            throw new DeserialisationException(e);
        }
    }

    @Override
    public void initInstance(Element element, T instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) {
    }

    protected abstract void setFormula(T deserialisee, BooleanFormula formula);

}

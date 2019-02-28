package org.workcraft.formula.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.jj.BooleanFormulaParser;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.CustomXMLDeserialiser;
import org.workcraft.serialisation.NodeFinaliser;
import org.workcraft.serialisation.NodeInitialiser;
import org.workcraft.types.Func;

public abstract class BooleanFunctionDeserialiser<T> implements CustomXMLDeserialiser<T> {

    private static final class VariableResolver implements Func<String, BooleanVariable> {
        private final ReferenceResolver internalReferenceResolver;

        private VariableResolver(ReferenceResolver internalReferenceResolver) {
            this.internalReferenceResolver = internalReferenceResolver;
        }

        @Override
        public BooleanVariable eval(String ref) {
            // FIXME: get rid of the need for var_ prefix
            if (ref.startsWith("var_")) {
                ref = ref.substring("var_".length());
                BooleanVariable bv = (BooleanVariable) internalReferenceResolver.getObject(ref);
                if (bv != null) {
                    return bv;
                }
            }
            return (BooleanVariable) internalReferenceResolver.getObject(ref);
        }
    }

    @Override
    public void finaliseInstance(Element element, T instance, final ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) throws DeserialisationException {

        String string = element.getAttribute("formula");
        BooleanFormula formula = parseFormula(string, internalReferenceResolver);
        setFormula(instance, formula);
    }

    public static BooleanFormula parseFormula(String string, final ReferenceResolver internalReferenceResolver)
            throws DeserialisationException {
        if ((string == null) || string.isEmpty()) {
            return null;
        }
        try {
            VariableResolver vars = new VariableResolver(internalReferenceResolver);
            return BooleanFormulaParser.parse(string, vars);
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

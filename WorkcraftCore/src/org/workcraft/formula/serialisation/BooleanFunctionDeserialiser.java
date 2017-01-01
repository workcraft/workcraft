package org.workcraft.formula.serialisation;

import org.w3c.dom.Element;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.jj.BooleanFormulaParser;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;
import org.workcraft.util.Func;

public abstract class BooleanFunctionDeserialiser implements CustomXMLDeserialiser {
    private static final class VariableResolver implements
            Func<String, BooleanVariable> {
        private final ReferenceResolver internalReferenceResolver;

        private VariableResolver(ReferenceResolver internalReferenceResolver) {
            this.internalReferenceResolver = internalReferenceResolver;
        }

        @Override
        public BooleanVariable eval(String ref) {
            if (ref.startsWith("var_")) {
                ref = ref.substring("var_".length());
                BooleanVariable bv = (BooleanVariable) internalReferenceResolver.getObject(ref);
                if (bv != null) {
                    return bv;
                }
            }
            String hier = NamespaceHelper.flatToHierarchicalName(ref);
            return (BooleanVariable) internalReferenceResolver.getObject(hier);
        }
    }

    @Override
    public void finaliseInstance(Element element, Object instance, final ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) throws DeserialisationException {
        String attributeName = "formula";
        BooleanFormula formula = readFormulaFromAttribute(element, internalReferenceResolver, attributeName);
        setFormula(instance, formula);
    }

    public static BooleanFormula readFormulaFromAttribute(Element element, final ReferenceResolver internalReferenceResolver,
            String attributeName) throws DeserialisationException {
        String string = element.getAttribute(attributeName);
        BooleanFormula formula = null;
        try {
            if (!string.isEmpty()) {
                VariableResolver vars = new VariableResolver(internalReferenceResolver);
                formula = BooleanFormulaParser.parse(string, vars);
            }
        } catch (ParseException e) {
            throw new DeserialisationException(e);
        }
        return formula;
    }

    @Override
    public void initInstance(Element element, Object instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) throws DeserialisationException {
    }

    protected abstract void setFormula(Object deserialisee, BooleanFormula formula);

}

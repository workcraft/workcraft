package org.workcraft.plugins.cpog.properties;

import org.workcraft.dom.Node;
import org.workcraft.formula.jj.BooleanFormulaParser;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.utils.StringGenerator;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.cpog.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

public class BooleanFormulaPropertyDescriptor implements PropertyDescriptor {

    private final Cpog cpog;
    private final Node node;

    public BooleanFormulaPropertyDescriptor(Cpog cpog, Node node) {
        this.cpog = cpog;
        this.node = node;
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        if (node instanceof VisualRhoClause) return "Function";
        return "Condition";
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object getValue() {
        if (node instanceof VisualRhoClause) {
            VisualRhoClause rhoClause = (VisualRhoClause) this.node;
            return StringGenerator.toString(rhoClause.getFormula());
        } else if (node instanceof VisualArc) {
            VisualArc arc = (VisualArc) this.node;
            return StringGenerator.toString(arc.getCondition());
        } else if (node instanceof VisualVertex) {
            VisualVertex vertex = (VisualVertex) this.node;
            return StringGenerator.toString(vertex.getCondition());
        }
        return null;
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        if (value instanceof String) {
            String string = (String) value;
            Collection<Variable> variables = cpog.getVariables();
            try {
                if (node instanceof VisualRhoClause) {
                    VisualRhoClause rhoClause = (VisualRhoClause) this.node;
                    rhoClause.setFormula(BooleanFormulaParser.parse(string, variables));
                } else if (node instanceof VisualArc) {
                    VisualArc arc = (VisualArc) this.node;
                    arc.setCondition(BooleanFormulaParser.parse(string, variables));
                } else if (node instanceof VisualVertex) {
                    VisualVertex vertex = (VisualVertex) this.node;
                    vertex.setCondition(BooleanFormulaParser.parse(string, variables));
                }
            } catch (ParseException e) {
                throw new InvocationTargetException(e);
            }
        }
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isCombinable() {
        return true;
    }

    @Override
    public boolean isTemplatable() {
        return true;
    }

}

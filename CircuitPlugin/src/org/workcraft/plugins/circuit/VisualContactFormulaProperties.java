package org.workcraft.plugins.circuit;

import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.Disableable;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.jj.ParseException;

public class VisualContactFormulaProperties {

    private abstract class FunctionPropertyDescriptor implements PropertyDescriptor, Disableable {
        @Override
        public Class<?> getType() {
            return String.class;
        }
        @Override
        public Map<Object, String> getChoice() {
            return null;
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
            return false;
        }
    }

    VisualCircuit circuit;

    public VisualContactFormulaProperties(VisualCircuit circuit) {
        this.circuit = circuit;
    }

    private BooleanFormula parseContactFunction(final VisualFunctionContact contact, String function) {
        BooleanFormula formula = null;
        if (!function.isEmpty()) {
            Node parent = contact.getParent();
            try {
                if (parent instanceof VisualFunctionComponent) {
                    VisualFunctionComponent component = (VisualFunctionComponent) parent;
                    formula = CircuitUtils.parseContactFuncton(circuit, component, function);
                } else {
                    formula = CircuitUtils.parsePortFuncton(circuit, function);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return formula;
    }

    public PropertyDescriptor getSetProperty(final VisualFunctionContact contact) {
        return new FunctionPropertyDescriptor() {
            @Override
            public void setValue(Object value) {
                BooleanFormula formula = parseContactFunction(contact, (String) value);
                contact.setSetFunction(formula);
            }
            @Override
            public Object getValue() {
                return FormulaToString.toString(contact.getSetFunction());
            }
            @Override
            public String getName() {
                return FunctionContact.PROPERTY_SET_FUNCTION;
            }
            @Override
            public boolean isDisabled() {
                return contact.isDriven();
            }
        };
    }

    public PropertyDescriptor getResetProperty(final VisualFunctionContact contact) {
        return new FunctionPropertyDescriptor() {
            @Override
            public void setValue(Object value) {
                BooleanFormula formula = parseContactFunction(contact, (String) value);
                contact.setResetFunction(formula);
            }
            @Override
            public Object getValue() {
                return FormulaToString.toString(contact.getResetFunction());
            }
            @Override
            public String getName() {
                return FunctionContact.PROPERTY_RESET_FUNCTION;
            }
            @Override
            public boolean isDisabled() {
                return contact.isDriven();
            }
        };
    }

}

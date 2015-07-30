package org.workcraft.plugins.circuit;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.javacc.ParseException;

public class VisualContactFormulaProperties {
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
					VisualFunctionComponent component = (VisualFunctionComponent)parent;
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
		return new PropertyDescriptor() {

			@Override
			public void setValue(Object value) throws InvocationTargetException {
				BooleanFormula formula = parseContactFunction(contact, (String)value);
				contact.setSetFunction(formula);
			}

			@Override
			public boolean isWritable() {
				return true;
			}

			@Override
			public Object getValue() throws InvocationTargetException {
				return FormulaToString.toString(contact.getSetFunction());
			}

			@Override
			public Class<?> getType() {
				return String.class;
			}

			@Override
			public String getName() {
				return "Set function";
			}

			@Override
			public Map<Object, String> getChoice() {
				return null;
			}

			@Override
			public boolean isCombinable() {
				return true;
			}

			@Override
			public boolean isTemplatable() {
				return false;
			}
		};
	}

	public PropertyDescriptor getResetProperty(final VisualFunctionContact contact) {
		return new PropertyDescriptor() {

			@Override
			public void setValue(Object value) throws InvocationTargetException {
				BooleanFormula formula = parseContactFunction(contact, (String)value);
				contact.setResetFunction(formula);
			}

			@Override
			public boolean isWritable() {
				return true;
			}

			@Override
			public Object getValue() throws InvocationTargetException {
				return FormulaToString.toString(contact.getResetFunction());
			}

			@Override
			public Class<?> getType() {
				return String.class;
			}

			@Override
			public String getName() {
				return "Reset function";
			}

			@Override
			public Map<Object, String> getChoice() {
				return null;
			}

			@Override
			public boolean isCombinable() {
				return true;
			}

			@Override
			public boolean isTemplatable() {
				return false;
			}
		};
	}

}

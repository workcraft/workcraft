package org.workcraft.plugins.circuit;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.Container;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.javacc.BooleanParser;
import org.workcraft.plugins.cpog.optimisation.javacc.ParseException;
import org.workcraft.util.Func;

public class VisualContactFormulaProperties {
	VisualCircuit vcircuit;

	public VisualContactFormulaProperties(VisualCircuit circuit) {
		this.vcircuit = circuit;
	}

	private BooleanFormula parseFormula(final VisualFunctionContact contact, String resetFunction) {
		try {
			return BooleanParser.parse(resetFunction,
					new Func<String, BooleanFormula>() {
						@Override
						public BooleanFormula eval(String name) {
							BooleanFormula result = null;
							Container container = (Container)contact.getParent();
							VisualFunctionContact vc = null;
							if (container instanceof VisualFunctionComponent) {
								vc = vcircuit.getOrCreateContact(container, name, IOType.INPUT);
							} else {
								vc = vcircuit.getOrCreateContact(container, name, IOType.OUTPUT);
							}
							if ((vc != null) && (vc.getReferencedContact() instanceof BooleanFormula)) {
								result = (BooleanFormula)vc.getReferencedContact();
							}
							return result;
						}
					});
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public PropertyDescriptor getSetProperty(final VisualFunctionContact contact) {
		return new PropertyDescriptor() {

			@Override
			public void setValue(Object value) throws InvocationTargetException {
				String setFunction = (String)value;
				if (!setFunction.equals("")) {
					contact.setSetFunction(parseFormula(contact, setFunction));
				} else {
					contact.setSetFunction(null);
				}
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
				String setFunction = (String)value;
				if (!setFunction.equals("")) {
					contact.setResetFunction(parseFormula(contact, setFunction));
				} else {
					contact.setResetFunction(null);
				}
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

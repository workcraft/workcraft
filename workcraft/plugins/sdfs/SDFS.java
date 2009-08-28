package org.workcraft.plugins.sdfs;

import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.AbstractMathModel;
import org.workcraft.dom.MathModelListener;
import org.workcraft.dom.MathNode;
import org.workcraft.dom.VisualClass;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;

@DisplayName ("Static Data Flow Structure")
@VisualClass ("org.workcraft.plugins.sdfs.VisualSDFS")
public class SDFS extends AbstractMathModel {

	public class Listener implements MathModelListener {
		public void onComponentAdded(Component component) {
			if (component instanceof Register)
				registers.add((Register)component);
			else if (component instanceof Logic)
				logic.add((Logic)component);
		}

		public void onComponentRemoved(Component component) {
			if (component instanceof Register)
				registers.remove(component);
			else if (component instanceof Logic)
				logic.remove(component);
		}

		public void onConnectionAdded(Connection connection) {
		}

		public void onConnectionRemoved(Connection connection) {
		}

		public void onNodePropertyChanged(String propertyName, MathNode n) {
		}
	}

	private HashSet<Register> registers = new HashSet<Register>();
	private HashSet<Logic> logic = new HashSet<Logic>();

	public SDFS() {
		super();
		addSupportedComponents();
		addListener(new Listener());
	}

	private void addSupportedComponents() {
		addComponentSupport(Register.class);
		addComponentSupport(Logic.class);
	}

	public void validate() throws ModelValidationException {
	}


	public void validateConnection(Connection connection)	throws InvalidConnectionException {
		if (connection.getFirst() == connection.getSecond())
			throw new InvalidConnectionException ("Self-loops are not allowed");
	}

	final public Register createRegister() {
		Register newRegister = new Register();
		addComponent(newRegister);
		return newRegister;
	}

	final public Logic createLogic() {
		Logic newLogic = new Logic();
		addComponent(newLogic);
		return newLogic;
	}

	final public Set<Register> getRegisters() {
		return new HashSet<Register>(registers);
	}

	final public Set<Logic> getLogic() {
		return new HashSet<Logic>(logic);
	}
}

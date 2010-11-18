package org.workcraft.plugins.circuit;

import java.awt.event.KeyEvent;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.util.Hierarchy;


@DisplayName("Function")
@Hotkey(KeyEvent.VK_F)
@SVGIcon("images/icons/svg/circuit-formula.svg")

public class VisualFunctionComponent extends VisualCircuitComponent {

	public VisualFunctionComponent(CircuitComponent component) {
		super(component);
	}

	public VisualFunctionContact addFunction(String name, IOType ioType, boolean allowShort) {
		name = Contact.getNewName(this.getReferencedComponent(), name, null, allowShort);

		VisualContact.Direction dir=null;
		if (ioType==null) ioType = IOType.OUTPUT;

		dir=VisualContact.Direction.WEST;
		if (ioType==IOType.OUTPUT)
			dir=VisualContact.Direction.EAST;

		FunctionContact c = new FunctionContact(ioType);

		VisualFunctionContact vc = new VisualFunctionContact(c, dir, name);

		addContact(vc);

		return vc;
	}

	public VisualFunctionContact getOrCreateInput(String arg) {

		for (VisualFunctionContact c : Hierarchy.filterNodesByType(getChildren(), VisualFunctionContact.class)) {
			if(c.getName().equals(arg)) return c;
		}

		VisualFunctionContact vc = addFunction(arg, IOType.INPUT, true);

		vc.setSetFunction(One.instance());
		vc.setResetFunction(One.instance());

		return vc;
	}


}

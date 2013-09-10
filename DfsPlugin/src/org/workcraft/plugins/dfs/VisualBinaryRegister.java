package org.workcraft.plugins.dfs;

import java.util.LinkedHashMap;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;

public class VisualBinaryRegister extends VisualComponent {

	public VisualBinaryRegister(BinaryRegister register) {
		super(register);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		LinkedHashMap<String, Object> markingChoice = new LinkedHashMap<String, Object>();
		for (ControlRegister.Marking marking : ControlRegister.Marking.values()) {
			markingChoice.put(marking.name, marking);
		}
		addPropertyDeclaration(new PropertyDeclaration(this, "Marking", "getMarking", "setMarking",
				ControlRegister.Marking.class, markingChoice));
	}

	@Override
	public void draw(DrawRequest r) {

	}

	public BinaryRegister getReferencedControlRegister() {
		return (BinaryRegister)getReferencedComponent();
	}

	public ControlRegister.Marking getMarking() {
		return getReferencedControlRegister().getMarking();
	}

	public void setMarking(ControlRegister.Marking value) {
		getReferencedControlRegister().setMarking(value);
	}

	public boolean isFalseMarked() {
		return getReferencedControlRegister().isFalseMarked();
	}

	public boolean isTrueMarked() {
		return getReferencedControlRegister().isTrusMarked();
	}

}

package org.workcraft.plugins.dfs;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.dfs.BinaryRegister.Marking;

public class VisualBinaryRegister extends VisualComponent {

	public VisualBinaryRegister(BinaryRegister register) {
		super(register);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualBinaryRegister, Marking>(
				this, "Marking", Marking.class, Marking.getChoice()) {
			public void setter(VisualBinaryRegister object, Marking value) {
				object.getReferencedBinaryRegister().setMarking(value);
			}
			public Marking getter(VisualBinaryRegister object) {
				return object.getReferencedBinaryRegister().getMarking();
			}
		});
	}

	@Override
	public void draw(DrawRequest r) {

	}

	public BinaryRegister getReferencedBinaryRegister() {
		return (BinaryRegister)getReferencedComponent();
	}

}

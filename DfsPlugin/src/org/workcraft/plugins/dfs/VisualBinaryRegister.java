package org.workcraft.plugins.dfs;

import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.dfs.BinaryRegister.Marking;

public abstract class VisualBinaryRegister extends VisualAbstractRegister {

	public VisualBinaryRegister(BinaryRegister register) {
		super(register);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualBinaryRegister, Marking>(
				this, BinaryRegister.PROPERTY_MARKING, Marking.class, true, true, true) {
			public void setter(VisualBinaryRegister object, Marking value) {
				object.getReferencedBinaryRegister().setMarking(value);
			}
			public Marking getter(VisualBinaryRegister object) {
				return object.getReferencedBinaryRegister().getMarking();
			}
		});
	}

	public BinaryRegister getReferencedBinaryRegister() {
		return (BinaryRegister)getReferencedComponent();
	}


	@Override
	public void copyStyle(Stylable src) {
		super.copyStyle(src);
		if (src instanceof VisualBinaryRegister) {
			BinaryRegister srcBinaryRegister = ((VisualBinaryRegister)src).getReferencedBinaryRegister();
			getReferencedBinaryRegister().setMarking(srcBinaryRegister.getMarking());
		}
	}

}

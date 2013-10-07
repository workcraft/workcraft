package org.workcraft.plugins.dfs;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.propertyeditor.Getter;
import org.workcraft.gui.propertyeditor.SafePropertyDeclaration;
import org.workcraft.gui.propertyeditor.Setter;
import org.workcraft.plugins.dfs.BinaryRegister.Marking;

public class VisualBinaryRegister extends VisualComponent {

	public VisualBinaryRegister(BinaryRegister register) {
		super(register);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new SafePropertyDeclaration<VisualBinaryRegister, Marking>(
				this, "Marking",
				new Getter<VisualBinaryRegister, Marking>() {
					@Override
					public Marking eval(VisualBinaryRegister object) {
						return object.getReferencedBinaryRegister().getMarking();
					}
				},
				new Setter<VisualBinaryRegister, Marking>() {
					@Override
					public void eval(VisualBinaryRegister object, Marking value) {
						object.getReferencedBinaryRegister().setMarking(value);
					}
				},
				Marking.class, Marking.getChoice()));
	}

	@Override
	public void draw(DrawRequest r) {

	}

	public BinaryRegister getReferencedBinaryRegister() {
		return (BinaryRegister)getReferencedComponent();
	}

}

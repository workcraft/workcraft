package org.workcraft.plugins.balsa;

import org.w3c.dom.Element;
import org.workcraft.dom.XMLSerialisation;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.dom.visual.VisualNode;

public class VisualBreezeSerialiser extends VisualTransformableNodeSerialiser {

	public void serialise(VisualNode node, Element element) {
		try {
			if(!(node instanceof VisualBreezeComponent))
				throw new RuntimeException("Unsupported component");

			final VisualBreezeComponent component = (VisualBreezeComponent)node;

			XMLSerialisation serialisation = new XMLSerialisation();

			serialisation.addSerialiser(new XMLSerialiser()
			{
				public String getTagName() {
					return VisualBreezeComponent.class.getSimpleName();
				}
				public void serialise(Element element) {
					element.setAttribute("ref", component.getRefComponent().getID()+"");
				}
			});

			serialisation.serialise(element);

			super.serialise(node, element);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}

package org.workcraft.plugins.balsa;

/*public class VisualBreezeSerialiser extends VisualTransformableNodeSerialiser {

	public void serialise(VisualNode node, Element element, ExportReferenceResolver refResolver) {
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

				public void serialise(Element element,
						ExportReferenceResolver refResolver) {
					element.setAttribute("ref", refResolver.getReference(component.getRefComponent()));
				}

				public void deserialise(Element element,
						ReferenceResolver refResolver)
						throws ImportException {

				}
			});

			serialisation.serialise(element, refResolver);

			super.serialise(node, element, refResolver);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
*/
package org.workcraft.plugins.stg.serialisation;

import org.w3c.dom.Element;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.ModelXMLDeserialiser;

public class VisualSTGDeserialiser implements ModelXMLDeserialiser {
	public String getClassName() {
		return VisualSTG.class.getName();
	}

	@Override
	public Model deserialise(Element modelElement, Node hierarchyRoot,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver) throws DeserialisationException {
		try {
			return new VisualSTG((STG)externalReferenceResolver.getObject("$model"), (VisualGroup)hierarchyRoot);
		} catch (VisualModelInstantiationException e) {
			throw new DeserialisationException(e);
		}
	}
}

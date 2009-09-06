package org.workcraft.plugins.stg.serialisation;

import org.w3c.dom.Element;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.framework.serialisation.xml.ModelXMLDeserialiser;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;

public class VisualSTGDeserialiser implements ModelXMLDeserialiser {
	public String getClassName() {
		return VisualSTG.class.getName();
	}

	@Override
	public Model deserialise(Element modelElement, Node hierarchyRoot,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver) throws DeserialisationException {
		return new VisualSTG((STG)externalReferenceResolver.getObject("$model"), (VisualGroup)hierarchyRoot);
	}
}

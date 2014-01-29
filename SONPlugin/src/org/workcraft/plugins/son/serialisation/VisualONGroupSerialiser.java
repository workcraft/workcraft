package org.workcraft.plugins.son.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.son.VisualONGroup;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class VisualONGroupSerialiser implements CustomXMLSerialiser{

	@Override
	public String getClassName()
	{
		return VisualONGroup.class.getName();
	}

	@Override
	public void serialise(Element element, Object object, ReferenceProducer internalReferences,
			ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) throws SerialisationException
	{
		VisualONGroup vGroup = (VisualONGroup) object;
		element.setAttribute("mathGroup", externalReferences.getReference(vGroup.getMathGroup()));
	}

}
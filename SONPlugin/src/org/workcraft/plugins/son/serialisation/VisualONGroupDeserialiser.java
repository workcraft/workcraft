package org.workcraft.plugins.son.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.VisualONGroup;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;;

public class VisualONGroupDeserialiser implements CustomXMLDeserialiser{

	@Override
	public String getClassName()
	{
		return VisualONGroup.class.getName();
	}

	@Override
	public void finaliseInstance(Element element, Object instance, ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) throws DeserialisationException
	{
		VisualONGroup group = (VisualONGroup)instance;
		group.setMathGroup((ONGroup)externalReferenceResolver.getObject(element.getAttribute("mathGroup")));

	}

	@Override
	public Object createInstance(Element element, ReferenceResolver externalReferenceResolver,
			Object... constructorParameters)
	{
		return new VisualONGroup((ONGroup)externalReferenceResolver.getObject(element.getAttribute("mathGroup")));
	}

	@Override
	public void initInstance(Element element, Object instance, ReferenceResolver externalReferenceResolver,
			NodeInitialiser nodeInitialiser) throws DeserialisationException
	{
	}
}

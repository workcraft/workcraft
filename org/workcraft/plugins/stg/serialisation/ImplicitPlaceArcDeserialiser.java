package org.workcraft.plugins.stg.serialisation;

import org.w3c.dom.Element;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.ImplicitPlaceArc;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;

public class ImplicitPlaceArcDeserialiser implements CustomXMLDeserialiser {

	@Override
	public String getClassName() {
		return ImplicitPlaceArc.class.getName();
	}

	@Override
	public void finaliseInstance(Element element, Object instance,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver)
	throws DeserialisationException {
		ImplicitPlaceArc arc = (ImplicitPlaceArc) instance;

		arc.setImplicitPlaceArc(
				(VisualComponent)internalReferenceResolver.getObject(element.getAttribute("first")),
				(VisualComponent)internalReferenceResolver.getObject(element.getAttribute("second")),
				(MathConnection)externalReferenceResolver.getObject(element.getAttribute("refCon1")),
				(MathConnection)externalReferenceResolver.getObject(element.getAttribute("refCon2")),
				(Place)externalReferenceResolver.getObject(element.getAttribute("refPlace"))
		);
	}

	@Override
	public Object initInstance(Element element,
			ReferenceResolver externalReferenceResolver)
	throws DeserialisationException {
		return new ImplicitPlaceArc();
	}
}

package org.workcraft.plugins.circuit.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class CircuitConnectionSerializer implements CustomXMLSerialiser{

	@Override
	public void serialise(Element element, Object object,
			ReferenceProducer internalReferences,
			ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser)
			throws SerialisationException {
	}

	@Override
	public String getClassName() {
		return VisualCircuitConnection.class.getName();
	}

}

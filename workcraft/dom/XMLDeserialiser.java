package org.workcraft.dom;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.ModelLoadFailedException;

public interface XMLDeserialiser {
	public void deserialise(Element element) throws ModelLoadFailedException;
	public String getTagName();
}

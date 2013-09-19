package org.workcraft.plugins.son.elements;

import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class NamedElement extends MathNode {
	private String name = "";

	@NoAutoSerialisation
	public String getName(){
		return this.name;
	}

	@NoAutoSerialisation
	public void setName(String name){
		this.name = name;
		sendNotification(new PropertyChangedEvent(this, "name"));
	}

}

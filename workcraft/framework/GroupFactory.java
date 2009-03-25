package org.workcraft.framework;

import java.lang.reflect.Constructor;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;

public class GroupFactory {

	public static VisualNode createVisualGroup(Element element, VisualModel visualModel) {
		String className = element.getAttribute("class");
		String deserialiserName = element.getAttribute("deserialiser");

		try {
			if(className != null && className.length() > 0)
			{
				Class<?> groupClass = Class.forName(className);
				Constructor<?> ctor = groupClass.getConstructor(Element.class, VisualModel.class);
				VisualGroup group = (VisualGroup)ctor.newInstance(element, visualModel);
				return group;
			}
			else if(deserialiserName != null && deserialiserName.length() > 0)
			{
				Class<?> deserialiserClass = Class.forName(deserialiserName);
				Constructor<?> ctor = deserialiserClass.getConstructor();
				VisualNodeDeserialiser deserialiser = (VisualNodeDeserialiser)ctor.newInstance();
				return deserialiser.deserialise(element, visualModel);
			}
			else
				return new VisualGroup(element, visualModel);


		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}

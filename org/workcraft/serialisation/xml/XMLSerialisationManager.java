/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.serialisation.xml;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.workcraft.PluginInfo;
import org.workcraft.PluginProvider;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.util.XmlUtil;

public class XMLSerialisationManager implements SerialiserFactory, NodeSerialiser {
	private HashMap<String, Class<? extends XMLSerialiser>> serialisers = new HashMap<String, Class<? extends XMLSerialiser>>();
	private HashMap<Class<?>, XMLSerialiser> serialiserCache = new HashMap<Class<?>, XMLSerialiser>();
	private DefaultNodeSerialiser nodeSerialiser = new DefaultNodeSerialiser(this,this);
	private XMLSerialiserState state = null;

	private void registerSerialiser (Class<? extends XMLSerialiser> cls) {
		XMLSerialiser inst;
		try {
			inst = cls.newInstance();
			serialisers.put(inst.getClassName(), cls);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public XMLSerialiser getSerialiserFor(Class<?> cls) throws InstantiationException, IllegalAccessException {
		XMLSerialiser serialiser = serialiserCache.get(cls);

		if (serialiser == null) {
			Class<? extends XMLSerialiser> serialiserClass = serialisers.get(cls.getName());
			if (serialiserClass != null)
			{
				serialiser = serialiserClass.newInstance();

				serialiserCache.put(cls, serialiser);
			}
		}

		return serialiser;
	}

	public void begin(ReferenceProducer internalReferenceResolver, ReferenceProducer externalReferenceResolver) {
		state = new XMLSerialiserState(internalReferenceResolver, externalReferenceResolver);
	}

	public void end() {
		state = null;
	}

	@SuppressWarnings("unchecked")
	public void processPlugins(PluginProvider manager) {
		PluginInfo[] serialiserInfos = manager.getPluginsImplementing(XMLSerialiser.class.getName());

		for (PluginInfo info : serialiserInfos) {
			try {
				registerSerialiser( (Class<? extends XMLSerialiser>) info.loadClass());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void serialise(Element element, Object object) throws SerialisationException
	{
		element.setAttribute("class", object.getClass().getName());

		nodeSerialiser.serialise(element, object, state.internalReferences, state.externalReferences);

		if (object instanceof Container)
			for (Node child : ((Container)object).getChildren()) {
				Element childElement = XmlUtil.createChildElement("node", element);
				serialise(childElement, child);
			}
	}
}

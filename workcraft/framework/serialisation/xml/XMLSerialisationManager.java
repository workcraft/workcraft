package org.workcraft.framework.serialisation.xml;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.plugins.PluginInfo;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;
import org.workcraft.framework.serialisation.ReferenceResolver;

public class XMLSerialisationManager implements SerialiserFactory, DeserialiserFactory {
	private HashMap<String, Class<? extends XMLSerialiser>> serialisers = new HashMap<String, Class<? extends XMLSerialiser>>();
	private HashMap<String, Class<? extends XMLDeserialiser>> deserialisers = new HashMap<String, Class<? extends XMLDeserialiser>>();

	private HashMap<Class<?>, XMLSerialiser> serialiserCache = new HashMap<Class<?>, XMLSerialiser>();
	private HashMap<String, XMLDeserialiser> deserialiserCache = new HashMap<String, XMLDeserialiser>();

	private NodeSerialiser nodeSerialiser = new NodeSerialiser(this);
	private NodeDeserialiser nodeDeserialiser = new NodeDeserialiser(this);

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

	private void registerDeserialiser (Class<? extends XMLDeserialiser> cls) {
		XMLDeserialiser inst;
		try {
			inst = cls.newInstance();
			deserialisers.put(inst.getClassName(), cls);
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

	 public XMLDeserialiser getDeserialiserFor(String className) throws InstantiationException, IllegalAccessException {
		XMLDeserialiser deserialiser = deserialiserCache.get(className);

		if (deserialiser == null) {
			Class<? extends XMLDeserialiser> deserialiserClass = deserialisers.get(className);
			if (deserialiserClass != null)
			{
				deserialiser = deserialiserClass.newInstance();
				deserialiserCache.put(className, deserialiser);
			}
		}

		return deserialiser;
	}

	@SuppressWarnings("unchecked")
	public void processPlugins(PluginManager manager) {
		PluginInfo[] serialiserInfos = manager.getPluginsByInterface(BasicXMLSerialiser.class.getName());

		for (PluginInfo info : serialiserInfos) {
			try {
				registerSerialiser( (Class<? extends BasicXMLSerialiser>) info.loadClass());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		PluginInfo[] deserialiserInfos = manager.getPluginsByInterface(BasicXMLDeserialiser.class.getName());

		for (PluginInfo info : deserialiserInfos) {
			try {
				registerDeserialiser( (Class<? extends BasicXMLDeserialiser>) Class.forName(info.getClassName()));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void serialise (Element element, Object object, ExternalReferenceResolver referenceResolver) throws SerialisationException
	{
		nodeSerialiser.serialise(element, object, referenceResolver);
	}

	public Object initInstance (Element element) throws DeserialisationException
	{
		return nodeDeserialiser.initInstance(element);
	}

	public void finalise(Element element, Object instance, ReferenceResolver referenceResolver) throws DeserialisationException {
		nodeDeserialiser.finaliseInstance(element, instance, referenceResolver);
	}
}

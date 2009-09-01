package org.workcraft.framework.serialisation.xml;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.plugins.PluginInfo;
import org.workcraft.framework.plugins.PluginProvider;
import org.workcraft.framework.serialisation.ReferenceProducer;
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
	public void processPlugins(PluginProvider manager) {
		PluginInfo[] serialiserInfos = manager.getPluginsImplementing(XMLSerialiser.class.getName());

		for (PluginInfo info : serialiserInfos) {
			try {
				registerSerialiser( (Class<? extends XMLSerialiser>) info.loadClass());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		PluginInfo[] deserialiserInfos = manager.getPluginsImplementing(XMLDeserialiser.class.getName());

		for (PluginInfo info : deserialiserInfos) {
			try {
				registerDeserialiser( (Class<? extends XMLDeserialiser>) Class.forName(info.getClassName()));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void serialise(Element element, Object object,
			ReferenceProducer internalReferences,
			ReferenceProducer externalReferences) throws SerialisationException
	{
		nodeSerialiser.serialise(element, object, internalReferences, externalReferences);
	}

	public Object initInstance (Element element, ReferenceResolver externalReferenceResolver) throws DeserialisationException
	{
		return nodeDeserialiser.initInstance(element, externalReferenceResolver);
	}

	public void finalise(Element element, Object instance, ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver) throws DeserialisationException {
		nodeDeserialiser.finaliseInstance(element, instance, internalReferenceResolver, externalReferenceResolver);
	}
}

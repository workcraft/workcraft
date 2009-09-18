package org.workcraft.serialisation.xml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.workcraft.PluginInfo;
import org.workcraft.PluginProvider;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.util.ConstructorParametersMatcher;

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

	public Model createModel (Element element, Node root,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver) throws DeserialisationException {

		String className = element.getAttribute("class");

		if (className == null || className.isEmpty())
			throw new DeserialisationException("Class name attribute is not set\n" + element.toString());

		Model result;
		Class<?> cls;

		try {
			org.workcraft.serialisation.xml.XMLDeserialiser deserialiser  = getDeserialiserFor(className);
			cls = Class.forName(className);

			if (deserialiser instanceof ModelXMLDeserialiser) {
				result = ((ModelXMLDeserialiser)deserialiser).deserialise(element, root, internalReferenceResolver, externalReferenceResolver);
			} else if (deserialiser != null) {
				throw new DeserialisationException ("Deserialiser for model class must implement ModelXMLDesiraliser interface");
			} else {
				Constructor<?> ctor = new ConstructorParametersMatcher().match(cls, root.getClass());
				result = (Model) ctor.newInstance(root);
			}

		} catch (InstantiationException e) {
			throw new DeserialisationException(e);
		} catch (IllegalAccessException e) {
			throw new DeserialisationException(e);
		} catch (ClassNotFoundException e) {
			throw new DeserialisationException(e);
		} catch (NoSuchMethodException e) {
			throw new DeserialisationException("In order to be deserialised automatically, the model must declare a constructor with parameter " + root.getClass(), e);
		} catch (IllegalArgumentException e) {
			throw new DeserialisationException(e);
		} catch (InvocationTargetException e) {
			throw new DeserialisationException(e);
		}

		nodeDeserialiser.doInitialisation(element, result, cls, externalReferenceResolver);
		nodeDeserialiser.doFinalisation(element, result, internalReferenceResolver, externalReferenceResolver, cls.getSuperclass());

		return result;
	}

	public void finalise(Element element, Object instance, ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver) throws DeserialisationException {
		nodeDeserialiser.finaliseInstance(element, instance, internalReferenceResolver, externalReferenceResolver);
	}
}

package org.workcraft.framework.serialisation.xml;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.plugins.PluginInfo;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;
import org.workcraft.framework.serialisation.ReferenceResolver;

public class XMLSerialisation {
	private HashMap<String, Class<? extends XMLSerialiser>> serialisers = new HashMap<String, Class<? extends XMLSerialiser>>();
	private HashMap<String, Class<? extends XMLDeserialiser>> deserialisers = new HashMap<String, Class<? extends XMLDeserialiser>>();

	private HashMap<Class<?>, XMLSerialiser> serialiserCache = new HashMap<Class<?>, XMLSerialiser>();
	private HashMap<Class<?>, XMLDeserialiser> deserialiserCache = new HashMap<Class<?>, XMLDeserialiser>();

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

	private XMLSerialiser getSerialiserFor(Class<?> cls) throws InstantiationException, IllegalAccessException {
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

	private XMLDeserialiser getDeserialiserFor(Class<?> cls) throws InstantiationException, IllegalAccessException {
		XMLDeserialiser deserialiser = deserialiserCache.get(cls);

		if (deserialiser == null) {
			Class<? extends XMLDeserialiser> deserialiserClass = deserialisers.get(cls.getName());
			if (deserialiserClass != null)
			{
				deserialiser = deserialiserClass.newInstance();
				deserialiserCache.put(cls, deserialiser);
			}
		}

		return deserialiser;
	}

	private void doSerialisation(Element element, Object object, ExternalReferenceResolver referenceResolver, Class<?> currentLevel) throws InstantiationException, IllegalAccessException, SerialisationException, IntrospectionException, IllegalArgumentException, InvocationTargetException
	{
		Element curLevelElement = element.getOwnerDocument().createElement(currentLevel.getSimpleName());
		element.appendChild(curLevelElement);

		XMLSerialiser serialiser = getSerialiserFor(currentLevel);

		autoSerialiseProperties(curLevelElement, object, referenceResolver, currentLevel);

		if (serialiser != null)
			if (serialiser instanceof BasicXMLSerialiser)
				((BasicXMLSerialiser)serialiser).serialise(curLevelElement, object);
			else if (serialiser instanceof ReferencingXMLSerialiser)
				((ReferencingXMLSerialiser)serialiser).serialise(curLevelElement, object, referenceResolver);


		if (currentLevel.getSuperclass() != Object.class)
			doSerialisation(element, object, referenceResolver, currentLevel.getSuperclass());
	}

	private void doDeserialisation(Element element, Object object, ReferenceResolver referenceResolver, Class<?> currentLevel) throws InstantiationException, IllegalAccessException, DeserialisationException
	{
		/*Element curLevelElement = XmlUtil.getChildElement(currentLevel.getSimpleName(), element);

		if (curLevelElement != null)
		{
			BasicXMLDeserialiser deserialiser = getDeserialiserFor(currentLevel);
			if (deserialiser == null)
			{
				//TODO: auto-deserialise public properties
			} else
			{
				deserialiser.deserialise(curLevelElement, referenceResolver);
			}
		}

		if (currentLevel.getSuperclass() != Object.class)
			doDeserialisation(element, object, referenceResolver, currentLevel.getSuperclass());*/
	}

	private void autoSerialiseProperties(Element element, Object object, ExternalReferenceResolver referenceResolver, Class<?> currentLevel) throws IntrospectionException, InstantiationException, IllegalAccessException, IllegalArgumentException, SerialisationException, InvocationTargetException {
		// type explicitly requested to be excluded from auto serialisation
		if (currentLevel.getAnnotation(NoAutoSerialisation.class) != null)
			return;

		BeanInfo info = Introspector.getBeanInfo(currentLevel, currentLevel.getSuperclass());

		for (PropertyDescriptor desc : info.getPropertyDescriptors())
		{
			if (desc.getPropertyType() == null)
				continue;

			if (desc.getWriteMethod() == null || desc.getReadMethod() == null)
				continue;

			// property explicitly requested to be excluded from auto serialisation
			if (
					desc.getReadMethod().getAnnotation(NoAutoSerialisation.class) != null ||
					desc.getWriteMethod().getAnnotation(NoAutoSerialisation.class) != null
					)
				continue;

			// the property is writable and is not of array type, try to get a serialiser
			XMLSerialiser serialiser = getSerialiserFor(desc.getPropertyType());

			if (!(serialiser != null  && serialiser instanceof BasicXMLSerialiser))
			{
				// no serialiser, try to use the special case enum serialiser
				if (desc.getPropertyType().isEnum())
				{
					serialiser = getSerialiserFor(Enum.class);
					if (serialiser == null)
						continue;
				} else
					continue;
			}

			Element propertyElement = element.getOwnerDocument().createElement("property");
			element.appendChild(propertyElement);
			propertyElement.setAttribute("class", desc.getPropertyType().getName());
			propertyElement.setAttribute("name", desc.getName());

			((BasicXMLSerialiser)serialiser).serialise(propertyElement, desc.getReadMethod().invoke(object));
		}
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
		try {
			doSerialisation(element, object, referenceResolver, object.getClass());
		} catch (InstantiationException e) {
			throw new SerialisationException(e);
		} catch (IllegalAccessException e) {
			throw new SerialisationException(e);
		} catch (IntrospectionException e) {
			throw new SerialisationException(e);
		} catch (IllegalArgumentException e) {
			throw new SerialisationException(e);
		} catch (InvocationTargetException e) {
			throw new SerialisationException(e);		}
	}

	public void deserialise (Element element, Object object, ReferenceResolver referenceResolver) throws DeserialisationException
	{
		try {
			doDeserialisation(element, object, referenceResolver, object.getClass());
		} catch (InstantiationException e) {
			throw new DeserialisationException(e);
		} catch (IllegalAccessException e) {
			throw new DeserialisationException(e);
		}
	}
}

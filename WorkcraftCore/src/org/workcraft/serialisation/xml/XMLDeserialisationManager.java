package org.workcraft.serialisation.xml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.workcraft.PluginProvider;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.References;
import org.workcraft.util.ConstructorParametersMatcher;
import org.workcraft.util.XmlUtils;

public class XMLDeserialisationManager implements DeserialiserFactory, NodeInitialiser, NodeFinaliser {

    private final HashMap<String, XMLDeserialiser> deserialisers = new HashMap<>();
    private final DefaultNodeDeserialiser nodeDeserialiser = new DefaultNodeDeserialiser(this, this, this);
    private XMLDeserialiserState state = null;

    private void registerDeserialiser(XMLDeserialiser deserialiser) {
        deserialisers.put(deserialiser.getClassName(), deserialiser);
    }

    @Override
    public XMLDeserialiser getDeserialiserFor(String className) throws InstantiationException, IllegalAccessException {
        return deserialisers.get(className);
    }

    public void begin(ReferenceResolver externalReferenceResolver) {
        state = new XMLDeserialiserState(externalReferenceResolver);
    }

    public References getReferenceResolver() {
        return state;
    }

    public void processPlugins(PluginProvider manager) {
        for (PluginInfo<? extends XMLDeserialiser> info : manager.getPlugins(XMLDeserialiser.class)) {
            registerDeserialiser(info.newInstance());
        }
    }

    @Override
    public Object initInstance(Element element, Object ... constructorParameters) throws DeserialisationException {
        Object instance = nodeDeserialiser.initInstance(element, state.getExternalReferences(), constructorParameters);

        state.setInstanceElement(instance, element);
        String ref = element.getAttribute("ref");
        state.setObject(ref, instance);

        if (instance instanceof Container) {
            for (Element subNodeElement : XmlUtils.getChildElements("node", element)) {
                Object subNode = initInstance(subNodeElement);

                if (subNode instanceof Node) {
                    state.addChildNode((Container) instance, (Node) subNode);
                }
            }
        }
        return instance;
    }

    public static Model createModel(Class<?> cls, Node root, Object underlyingModel, References rr) throws DeserialisationException {
        Model result;
        try {
            Constructor<?> ctor;
            if (underlyingModel == null) {
                try {
                    ctor = new ConstructorParametersMatcher().match(cls, root.getClass(), References.class);
                    result = (Model) ctor.newInstance(root, rr);
                } catch (NoSuchMethodException e) {
                    ctor = new ConstructorParametersMatcher().match(cls, root.getClass());
                    result = (Model) ctor.newInstance(root);
                }
            } else {
                try {
                    ctor = new ConstructorParametersMatcher().match(cls, underlyingModel.getClass(), root.getClass(), References.class);
                    result = (Model) ctor.newInstance(underlyingModel, root, rr);
                } catch (NoSuchMethodException e) {
                    ctor = new ConstructorParametersMatcher().match(cls, underlyingModel.getClass(), root.getClass());
                    result = (Model) ctor.newInstance(underlyingModel, root);
                }
            }
        } catch (InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException e) {
            throw new DeserialisationException(e);
        } catch (NoSuchMethodException e) {
            throw new DeserialisationException("Missing appropriate constructor for model deserealisation.", e);
        }
        return result;
    }

    public void deserialiseModelProperties(Element element, Model model) throws DeserialisationException {
        nodeDeserialiser.doInitialisation(element, model, model.getClass(), state.getExternalReferences());
        nodeDeserialiser.doFinalisation(element, model, state.getInternalReferences(), state.getExternalReferences(),
                model.getClass().getSuperclass());
    }

    public void finaliseInstances() throws DeserialisationException {
        // finalise all instances
        for (Object o : state.instanceElements.keySet()) {
            finaliseInstance(o);
        }
        // now add children to their respective containers
        for (Object o : state.instanceElements.keySet()) {
            if (o instanceof Container) {
                Container c = (Container) o;
                c.add(state.getChildren(c));
            }
        }
    }

    @Override
    public void finaliseInstance(Object instance) throws DeserialisationException {
        nodeDeserialiser.finaliseInstance(state.getInstanceElement(instance), instance,
                state.getInternalReferences(), state.getExternalReferences());
    }

}

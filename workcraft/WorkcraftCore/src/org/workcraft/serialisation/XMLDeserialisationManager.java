package org.workcraft.serialisation;

import org.w3c.dom.Element;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.plugins.PluginProvider;
import org.workcraft.serialisation.reflection.ConstructorParametersMatcher;
import org.workcraft.utils.XmlUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class XMLDeserialisationManager implements DeserialiserFactory, NodeInitialiser, NodeFinaliser {

    private final HashMap<String, XMLDeserialiser> deserialisers = new HashMap<>();
    private final DefaultNodeDeserialiser nodeDeserialiser = new DefaultNodeDeserialiser(this, this, this);
    private XMLDeserialiserState state = null;

    private void registerDeserialiser(XMLDeserialiser deserialiser) {
        deserialisers.put(deserialiser.getClassName(), deserialiser);
    }

    @Override
    public XMLDeserialiser getDeserialiserFor(String className) {
        return deserialisers.get(className);
    }

    public void begin(ReferenceResolver externalReferenceResolver) {
        state = new XMLDeserialiserState(externalReferenceResolver);
    }

    public References getReferenceResolver() {
        return state;
    }

    public void processPlugins(PluginProvider pp) {
        for (PluginInfo<? extends XMLDeserialiser> info : pp.getPlugins(XMLDeserialiser.class)) {
            registerDeserialiser(info.getInstance());
        }
    }

    @Override
    public Object initInstance(Element element, Object... constructorParameters) throws DeserialisationException {
        Object instance = nodeDeserialiser.initInstance(element, state.getExternalReferences(), constructorParameters);

        state.setInstanceElement(instance, element);
        String ref = element.getAttribute(XMLCommonAttributes.REF_ATTRIBUTE);
        state.setObject(ref, instance);

        if (instance instanceof Container) {
            for (Element subNodeElement : XmlUtils.getChildElements(XMLCommonAttributes.NODE_ATTRIBUTE, element)) {
                Object subNode = initInstance(subNodeElement);

                if (subNode instanceof Node) {
                    state.addChildNode((Container) instance, (Node) subNode);
                }
            }
        }
        return instance;
    }

    public static Model<?, ?> createModel(Class<?> cls, Node root, Object underlyingModel, References rr)
            throws DeserialisationException {

        Model<?, ?> result;
        try {
            Constructor<?> ctor;
            if (underlyingModel == null) {
                ctor = new ConstructorParametersMatcher().match(cls, root.getClass(), References.class);
                result = (Model<?, ?>) ctor.newInstance(root, rr);
            } else {
                ctor = new ConstructorParametersMatcher().match(cls, underlyingModel.getClass(), root.getClass());
                result = (Model<?, ?>) ctor.newInstance(underlyingModel, root);
            }
        } catch (NoSuchMethodException e) {
            throw new DeserialisationException("Missing appropriate constructor for model deserialisation.", e);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new DeserialisationException(e);
        }
        return result;
    }

    public void deserialiseModelProperties(Element element, Model<?, ?> model) throws DeserialisationException {
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
            if (o instanceof Container c) {
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

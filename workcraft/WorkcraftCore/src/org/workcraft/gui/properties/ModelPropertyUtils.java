package org.workcraft.gui.properties;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.types.Pair;
import org.workcraft.utils.SortUtils;

import java.util.*;

public class ModelPropertyUtils {

    public static ModelProperties getTemplateProperties(VisualModel model, VisualNode templateNode) {
        ModelProperties result = getProperties(model, templateNode);
        for (final PropertyDescriptor pd : new LinkedList<>(result.getDescriptors())) {
            if (!pd.isTemplatable()) {
                result.remove(pd);
            }
        }
        return result;
    }

    public static ModelProperties getSelectionProperties(VisualModel model) {
        final Collection<? extends VisualNode> selection = model.getSelection();
        if (selection.isEmpty()) {
            return getProperties(model);
        } else if (selection.size() == 1) {
            final VisualNode node = selection.iterator().next();
            return getProperties(model, node);
        } else {
            return getProperties(model, selection);
        }
    }

    private static ModelProperties getProperties(VisualModel model) {
        return model.getProperties(null);
    }

    private static ModelProperties getProperties(VisualModel model, VisualNode node) {
        ModelProperties result = new ModelProperties();
        result.addAll(sortDescriptors(collectDescriptors(model, node)));
        return result;
    }

    private static ModelProperties getProperties(VisualModel model, Collection<? extends VisualNode> nodes) {
        List<PropertyDescriptor> descriptors = new ArrayList<>();
        for (VisualNode node : nodes) {
            descriptors.addAll(collectDescriptors(model, node));
        }
        ModelProperties result = new ModelProperties();
        result.addAll(combineDescriptors(sortDescriptors(descriptors)));
        return result;
    }

    private static List<PropertyDescriptor> collectDescriptors(VisualModel model, VisualNode node) {
        List<PropertyDescriptor> result = new ArrayList<>();
        Properties properties = model.getProperties(node);
        result.addAll(properties.getDescriptors());
        result.addAll(node.getDescriptors());
        return result;
    }

    private static List<PropertyDescriptor> sortDescriptors(List<PropertyDescriptor> descriptors) {
        return SortUtils.getSortedNatural(descriptors, PropertyDescriptor::getName);
    }

    private static List<PropertyDescriptor> combineDescriptors(List<PropertyDescriptor> descriptors) {
        LinkedHashMap<Pair<String, Class<?>>, Set<PropertyDescriptor>> categories = new LinkedHashMap<>();
        for (PropertyDescriptor descriptor : descriptors) {
            if (descriptor.isCombinable()) {
                Pair<String, Class<?>> key = Pair.of(descriptor.getName(), descriptor.getType());
                Set<PropertyDescriptor> value = categories.computeIfAbsent(key, k -> new HashSet<>());
                value.add(descriptor);
            }
        }
        List<PropertyDescriptor> result = new ArrayList<>();
        for (Pair<String, Class<?>> key: categories.keySet()) {
            final String name = key.getFirst();
            final Class<?> type = key.getSecond();
            final Set<PropertyDescriptor> values = categories.get(key);
            PropertyDescriptor comboDescriptor = new PropertyCombiner(name, type, values);
            result.add(comboDescriptor);
        }
        return result;
    }

}

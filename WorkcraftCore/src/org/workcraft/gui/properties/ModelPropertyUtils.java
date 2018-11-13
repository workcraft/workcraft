package org.workcraft.gui.properties;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;

import java.util.Collection;
import java.util.LinkedList;

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
        if (selection.size() == 0) {
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
        Properties properties = model.getProperties(node);
        result.addAll(properties.getDescriptors());
        result.addAll(node.getDescriptors());
        return result;
    }

    private static ModelProperties getProperties(VisualModel model, Collection<? extends VisualNode> nodes) {
        ModelProperties allProperties = new ModelProperties();
        for (VisualNode node: nodes) {
            Properties properties = getProperties(model, node);
            allProperties.addAll(properties.getDescriptors());
        }
        // Combine duplicates by creating a new ModelProperties
        return new ModelProperties(allProperties.getDescriptors());
    }

}

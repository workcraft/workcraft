package org.workcraft.utils;

import org.workcraft.dom.references.FileReference;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.properties.PropertyDescriptor;

import java.util.HashSet;
import java.util.Set;

public class FileReferenceUtils {

    private static Set<FileReference> getFileReferenceProperties(VisualModel model) {
        Set<PropertyDescriptor> properties = new HashSet<>();
        properties.addAll(model.getProperties(null).getDescriptors());
        for (VisualNode node : Hierarchy.getDescendantsOfType(model.getRoot(), VisualNode.class)) {
            properties.addAll(node.getDescriptors());
            properties.addAll(model.getProperties(node).getDescriptors());
        }
        Set<FileReference> fileReferences = new HashSet<>();
        for (PropertyDescriptor property : properties) {
            Object value = property.getValue();
            if (value instanceof FileReference) {
                fileReferences.add((FileReference) value);
            }
        }
        return fileReferences;
    }

    public static void makeAbsolute(VisualModel model) {
        for (FileReference fileReference : getFileReferenceProperties(model)) {
            fileReference.setBase(null);
        }
    }

    public static void makeRelative(VisualModel model, String base) {
        for (FileReference fileReference : getFileReferenceProperties(model)) {
            fileReference.setBase(base);
        }
    }

    public static void makeAbsolute(VisualModel model, String base) {
        for (FileReference fileReference : getFileReferenceProperties(model)) {
            fileReference.setBase(base);
            fileReference.setBase(null);
        }
    }

}

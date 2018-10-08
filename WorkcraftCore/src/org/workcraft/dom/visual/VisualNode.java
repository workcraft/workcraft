package org.workcraft.dom.visual;

import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.ObservableStateImpl;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;

public abstract class VisualNode implements Properties, Node, Touchable, Stylable, ObservableState, Hidable {
    protected ObservableStateImpl observableStateImpl = new ObservableStateImpl();
    private Node parent = null;
    private boolean hidden = false;
    private final ModelProperties properties = new ModelProperties();

    public void addPropertyDeclaration(PropertyDescriptor declaration) {
        properties.add(declaration);
    }

    public void removePropertyDeclarationByName(String propertyName) {
        properties.removeByName(propertyName);
    }

    public void renamePropertyDeclarationByName(String propertyName, String newPropertyName) {
        properties.renameByName(propertyName, newPropertyName);
    }

    @Override
    public Collection<PropertyDescriptor> getDescriptors() {
        return properties.getDescriptors();
    }

    @Override
    public Rectangle2D getBoundingBox() {
        return null;
    }

    @Override
    public Point2D getCenter() {
        return new Point2D.Double(getBoundingBox().getCenterX(), getBoundingBox().getCenterY());
    }

    @Override
    public Collection<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public void setParent(Node parent) {
        this.parent = parent;
    }

    @NoAutoSerialisation
    @Override
    public boolean isHidden() {
        return hidden;
    }

    @NoAutoSerialisation
    @Override
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public void addObserver(StateObserver obs) {
        observableStateImpl.addObserver(obs);
    }

    @Override
    public void sendNotification(StateEvent e) {
        observableStateImpl.sendNotification(e);
    }

    @Override
    public void removeObserver(StateObserver obs) {
        observableStateImpl.removeObserver(obs);
    }

    @Override
    public void copyStyle(Stylable src) {
    }

    @Override
    public void mixStyle(Stylable... srcs) {
    }

}

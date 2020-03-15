package org.workcraft.plugins.policy;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class VisualLocality extends VisualGroup {
    private Locality refLocality;

    public VisualLocality(Locality locality) {
        this.refLocality = locality;
    }

    @Override
    public void add(Node node) {
        Node mathNode = null;
        if (node instanceof VisualComponent) {
            mathNode = ((VisualComponent) node).getReferencedComponent();
        } else if (node instanceof VisualLocality) {
            mathNode = ((VisualLocality) node).getLocality();
        }
        if (mathNode != null) {
            Locality oldLocality = (Locality) mathNode.getParent();
            oldLocality.reparent(Arrays.asList(mathNode), refLocality);
        }
        super.add(node);
    }

    @Override
    public void add(Collection<? extends Node> nodes) {
        for (Node node : nodes) {
            this.add(node);
        }
    }

    public Locality getLocality() {
        return refLocality;
    }

    public void setLocality(Locality newLocality) {
        if (refLocality != newLocality) {
            if (refLocality != null) {
                refLocality.reparent(refLocality.getChildren(), newLocality);
            }
            refLocality = newLocality;
        }
    }

    private Collection<Node> filterRefNodesByLocality(Collection<? extends Node> nodes, Locality locality) {
        Collection<Node> result = new ArrayList<>();
        for (Node node: nodes) {
            Node refNode = null;
            if (node instanceof VisualComponent) {
                refNode = ((VisualComponent) node).getReferencedComponent();
            } else if (node instanceof VisualLocality) {
                refNode = ((VisualLocality) node).getLocality();
            }
            if (refNode != null && refNode.getParent() == locality) {
                result.add(refNode);
            }
        }
        return result;
    }

    @Override
    public void reparent(Collection<? extends Node> nodes, Container newParent) {
        if (newParent instanceof VisualLocality) {
            VisualLocality newLocality = (VisualLocality) newParent;
            refLocality.reparent(filterRefNodesByLocality(nodes, refLocality), newLocality.getLocality());
        }
        super.reparent(nodes, newParent);
    }

    @Override
    public void reparent(Collection<? extends Node> nodes) {
        refLocality.reparent(filterRefNodesByLocality(nodes, null));
        super.reparent(nodes);
    }

}

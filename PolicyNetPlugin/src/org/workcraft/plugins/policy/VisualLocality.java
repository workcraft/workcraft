package org.workcraft.plugins.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;

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
    public void remove(Node node) {
        super.remove(node);
    }

    @Override
    public void add(Collection<Node> nodes) {
        for (Node node : nodes)
            this.add(node);
    }

    @Override
    public void remove(Collection<Node> nodes) {
        super.remove(nodes);
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

    private Collection<Node> filterRefNodesByLocality(Collection<Node> nodes, Locality locality) {
        Collection<Node> result = new ArrayList<Node>();
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
    public void reparent(Collection<Node> nodes, Container newParent) {
        if (newParent instanceof VisualLocality) {
            VisualLocality newLocality = (VisualLocality) newParent;
            refLocality.reparent(filterRefNodesByLocality(nodes, refLocality), newLocality.getLocality());
        }
        super.reparent(nodes, newParent);
    }

    @Override
    public void reparent(Collection<Node> nodes) {
        refLocality.reparent(filterRefNodesByLocality(nodes, null));
        super.reparent(nodes);
    }

}

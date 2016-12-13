package org.workcraft.plugins.stg.tools;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.NodeTransformer;
import org.workcraft.AbstractTransformationCommand;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceUtils;

public final class InsertDummyTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Insert dummies into selected arcs";
    }

    @Override
    public String getPopupName() {
        return "Insert dummy";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, Stg.class);
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return (node instanceof VisualImplicitPlaceArc)
                || PetriNetUtils.isVisualConsumingArc(node)
                || PetriNetUtils.isVisualProducingArc(node);
    }

    @Override
    public boolean isEnabled(ModelEntry me, Node node) {
        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public Collection<Node> collect(Model model) {
        Collection<Node> arcs = new HashSet<>();
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            arcs.addAll(stg.getVisualImplicitPlaceArcs());
            arcs.addAll(PetriNetUtils.getVisualConsumingArcs(stg));
            arcs.addAll(PetriNetUtils.getVisualProducingArcs(stg));
            arcs.retainAll(stg.getSelection());
        }
        return arcs;
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualStg) && (node instanceof VisualConnection)) {
            VisualStg stg = (VisualStg) model;
            VisualConnection connection = (VisualConnection) node;
            Container container = (Container) connection.getParent();
            VisualDummyTransition dummy = stg.createDummyTransition(null, container);
            Point2D splitPoint = connection.getSplitPoint();
            dummy.setPosition(splitPoint);
            try {
                LinkedList<Point2D> prefixLocationsInRootSpace = ConnectionHelper.getPrefixControlPoints(connection, splitPoint);
                VisualConnection predConnection = stg.connect(connection.getFirst(), dummy);
                predConnection.copyStyle(connection);
                ConnectionHelper.addControlPoints(predConnection, prefixLocationsInRootSpace);
            } catch (InvalidConnectionException e) {
                LogUtils.logWarningLine(e.getMessage());
            }
            try {
                LinkedList<Point2D> suffixLocationsInRootSpace = ConnectionHelper.getSuffixControlPoints(connection, splitPoint);
                VisualConnection succConnection = stg.connect(dummy, connection.getSecond());
                succConnection.copyStyle(connection);
                if (succConnection instanceof VisualImplicitPlaceArc) {
                    ((VisualImplicitPlaceArc) succConnection).getImplicitPlace().setTokens(0);
                }
                ConnectionHelper.addControlPoints(succConnection, suffixLocationsInRootSpace);
            } catch (InvalidConnectionException e) {
                LogUtils.logWarningLine(e.getMessage());
            }
            stg.remove(connection);
        }
    }

}

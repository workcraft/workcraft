package org.workcraft.plugins.stg.tools;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.NodeTransformer;
import org.workcraft.TransformationTool;
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
import org.workcraft.workspace.WorkspaceEntry;

public final class DummyInserterTool extends TransformationTool implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Insert dummies into selected arcs";
    }

    @Override
    public String getPopupName() {
        return "Insert dummy";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof Stg;
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return (node instanceof VisualImplicitPlaceArc)
                || PetriNetUtils.isVisualConsumingArc(node)
                || PetriNetUtils.isVisualProducingArc(node);
    }

    @Override
    public boolean isEnabled(WorkspaceEntry we, Node node) {
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
    public void run(WorkspaceEntry we) {
        final VisualStg model = (VisualStg) we.getModelEntry().getVisualModel();
        HashSet<VisualConnection> connections = new HashSet<>();
        connections.addAll(model.getVisualImplicitPlaceArcs());
        connections.addAll(PetriNetUtils.getVisualConsumingArcs(model));
        connections.addAll(PetriNetUtils.getVisualProducingArcs(model));
        connections.retainAll(model.getSelection());
        if (!connections.isEmpty()) {
            we.saveMemento();
            for (VisualConnection connection: connections) {
                transform(model, connection);
            }
        }
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

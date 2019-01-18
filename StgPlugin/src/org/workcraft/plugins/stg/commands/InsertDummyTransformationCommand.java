package org.workcraft.plugins.stg.commands;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Container;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.utils.PetriNetUtils;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

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
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return (node instanceof VisualImplicitPlaceArc)
                || PetriNetUtils.isVisualConsumingArc(node)
                || PetriNetUtils.isVisualProducingArc(node);
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public Collection<VisualNode> collect(VisualModel model) {
        Collection<VisualNode> arcs = new HashSet<>();
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
    public void transform(VisualModel model, VisualNode node) {
        if ((model instanceof VisualStg) && (node instanceof VisualConnection)) {
            VisualStg stg = (VisualStg) model;
            VisualConnection connection = (VisualConnection) node;
            Container container = (Container) connection.getParent();
            VisualDummyTransition dummy = stg.createVisualDummyTransition(null, container);
            Point2D splitPoint = connection.getSplitPoint();
            dummy.setPosition(splitPoint);
            try {
                LinkedList<Point2D> prefixLocationsInRootSpace = ConnectionHelper.getPrefixControlPoints(connection, splitPoint);
                VisualConnection predConnection = stg.connect(connection.getFirst(), dummy);
                predConnection.copyStyle(connection);
                ConnectionHelper.addControlPoints(predConnection, prefixLocationsInRootSpace);
            } catch (InvalidConnectionException e) {
                LogUtils.logWarning(e.getMessage());
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
                LogUtils.logWarning(e.getMessage());
            }
            stg.remove(connection);
            model.addToSelection(dummy);
        }
    }

}

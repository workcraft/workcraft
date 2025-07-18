package org.workcraft.plugins.stg.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.Container;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petri.utils.ConnectionUtils;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualNamedTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public abstract class AbstractInsertTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return (node instanceof VisualImplicitPlaceArc)
                || ConnectionUtils.isVisualConsumingArc(node)
                || ConnectionUtils.isVisualProducingArc(node);
    }

    @Override
    public MenuVisibility getMenuVisibility() {
        return MenuVisibility.APPLICABLE_POPUP_ONLY;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> arcs = new HashSet<>();
        if (model instanceof VisualStg stg) {
            arcs.addAll(stg.getVisualImplicitPlaceArcs());
            arcs.addAll(ConnectionUtils.getVisualConsumingArcs(stg));
            arcs.addAll(ConnectionUtils.getVisualProducingArcs(stg));
            arcs.retainAll(stg.getSelection());
        }
        return arcs;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualStg stg) && (node instanceof VisualConnection connection)) {
            VisualTransition transition = insertTransitionIntoConnection(stg, connection);
            model.addToSelection(transition);
        }
    }

    public VisualNamedTransition insertTransitionIntoConnection(VisualStg stg, VisualConnection connection) {
        Container container = (Container) connection.getParent();
        VisualNamedTransition transition = createTransition(stg, container);
        Point2D splitPoint = connection.getSplitPoint();
        transition.setPosition(splitPoint);
        try {
            LinkedList<Point2D> prefixLocationsInRootSpace = ConnectionHelper.getPrefixControlPoints(connection, splitPoint);
            VisualConnection predConnection = stg.connect(connection.getFirst(), transition);
            predConnection.copyStyle(connection);
            ConnectionHelper.addControlPoints(predConnection, prefixLocationsInRootSpace);
        } catch (InvalidConnectionException e) {
            LogUtils.logWarning(e.getMessage());
        }
        try {
            LinkedList<Point2D> suffixLocationsInRootSpace = ConnectionHelper.getSuffixControlPoints(connection, splitPoint);
            VisualConnection succConnection = stg.connect(transition, connection.getSecond());
            succConnection.copyStyle(connection);
            if (succConnection instanceof VisualImplicitPlaceArc) {
                ((VisualImplicitPlaceArc) succConnection).getImplicitPlace().setTokens(0);
            }
            ConnectionHelper.addControlPoints(succConnection, suffixLocationsInRootSpace);
        } catch (InvalidConnectionException e) {
            LogUtils.logWarning(e.getMessage());
        }
        stg.remove(connection);
        return transition;
    }

    public abstract VisualNamedTransition createTransition(VisualStg stg, Container container);

}

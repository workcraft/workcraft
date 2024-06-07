package org.workcraft.plugins.stg.commands;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petri.commands.ContractTransitionTransformationCommand;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.VisualStgPlace;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class ContractNamedTransitionTransformationCommand extends ContractTransitionTransformationCommand {

    private final HashSet<VisualStgPlace> convertedImplicitPlaces = new HashSet<>();

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public void beforeContraction(VisualModel visualModel, VisualTransition visualTransition) {
        super.beforeContraction(visualModel, visualTransition);
        convertedImplicitPlaces.clear();
        if (visualModel instanceof VisualStg) {
            VisualStg visualStg = (VisualStg) visualModel;
            Set<Connection> adjacentConnections = new HashSet<>(visualModel.getConnections(visualTransition));
            for (Connection connection : adjacentConnections) {
                if (connection instanceof VisualImplicitPlaceArc) {
                    VisualStgPlace formerImplicitPlace = visualStg.makeExplicit((VisualImplicitPlaceArc) connection);
                    convertedImplicitPlaces.add(formerImplicitPlace);
                }
            }
        }
    }

    @Override
    public void nameProductPlace(VisualModel model,
            VisualPlace productPlace, ProductPlacePositioning productPlacePositioning,
            VisualPlace predPlace, VisualPlace succPlace) {

        if (!(model instanceof VisualStg) || !(productPlace instanceof VisualStgPlace)) {
            super.nameProductPlace(model, productPlace, productPlacePositioning, predPlace, succPlace);
        }
        VisualStg stg = (VisualStg) model;
        VisualStgPlace productStgPlace = (VisualStgPlace) productPlace;
        if (convertedImplicitPlaces.contains(predPlace) && convertedImplicitPlaces.contains(succPlace)) {
            super.nameProductPlace(model, productPlace, productPlacePositioning, predPlace, succPlace);
        } else if (convertedImplicitPlaces.contains(predPlace)) {
            String succPlaceName = stg.getMathName(succPlace);
            stg.setMathName(succPlace, Identifier.makeInternal(succPlaceName));
            stg.setMathName(productStgPlace, succPlaceName);
        } else if (convertedImplicitPlaces.contains(succPlace)) {
            String predPlaceName = stg.getMathName(predPlace);
            stg.setMathName(predPlace, Identifier.makeInternal(predPlaceName));
            stg.setMathName(productStgPlace, predPlaceName);
        } else {
            super.nameProductPlace(model, productPlace, productPlacePositioning, predPlace, succPlace);
        }
    }

    @Override
    public void positionProductPlace(VisualModel model,
            VisualPlace productPlace, ProductPlacePositioning productPlacePositioning,
            VisualPlace predPlace, VisualTransition transition, VisualPlace succPlace) {

        if (!(model instanceof VisualStg) || !(productPlace instanceof VisualStgPlace)) {
            super.positionProductPlace(model, productPlace, productPlacePositioning, predPlace, transition, succPlace);
        }
        VisualStg stg = (VisualStg) model;
        VisualStgPlace productStgPlace = (VisualStgPlace) productPlace;
        if (convertedImplicitPlaces.contains(predPlace) && convertedImplicitPlaces.contains(succPlace)) {
            productStgPlace.copyPosition(transition);
            shapeProductPlacePredConnection(stg, productStgPlace, predPlace, null, null);
            shapeProductPlaceSuccConnection(stg, productStgPlace, succPlace, null, null);
            VisualConnection connection = stg.makeImplicitIfPossible(productStgPlace, true);
            filterControlPoints(connection);
        } else if (convertedImplicitPlaces.contains(predPlace)) {
            productStgPlace.copyPosition(succPlace);
            productStgPlace.copyStyle(succPlace);
            shapeProductPlacePredConnection(stg, productStgPlace, predPlace, transition, succPlace);
        } else if (convertedImplicitPlaces.contains(succPlace)) {
            productStgPlace.copyPosition(predPlace);
            productStgPlace.copyStyle(predPlace);
            shapeProductPlaceSuccConnection(stg, productStgPlace, succPlace, transition, predPlace);
        } else {
            super.positionProductPlace(model, productPlace, productPlacePositioning, predPlace, transition, succPlace);
        }
    }

    private void shapeProductPlacePredConnection(VisualStg stg, VisualStgPlace productPlace,
            VisualPlace predPlace, VisualTransition transition, VisualPlace succPlace) {

        VisualConnection predPlacePredConnection = getOnlyPredConnectionOrNull(stg, predPlace);
        VisualConnection predPlaceSuccConnection = getOnlySuccConnectionOrNull(stg, predPlace);

        if ((predPlacePredConnection != null) && (predPlaceSuccConnection != null)) {
            LinkedList<Point2D> predControlPoints = new LinkedList<>();
            predControlPoints.addAll(
                    ConnectionHelper.getMergedControlPoints(predPlace, predPlacePredConnection, predPlaceSuccConnection));

            VisualConnection transitionSuccConnection = stg.getConnection(transition, succPlace);
            if (transitionSuccConnection != null) {
                predControlPoints.addAll(
                        ConnectionHelper.getMergedControlPoints(transition, null, transitionSuccConnection));
            }

            VisualConnection productPlacePredConnection = stg.getConnection(predPlacePredConnection.getFirst(), productPlace);
            if (productPlacePredConnection != null) {
                ConnectionHelper.straightenConnection(productPlacePredConnection);
                ConnectionHelper.addControlPoints(productPlacePredConnection, predControlPoints);
                filterControlPoints(productPlacePredConnection);
            }
        }
    }

    private void shapeProductPlaceSuccConnection(VisualStg stg, VisualStgPlace productPlace,
            VisualPlace succPlace, VisualTransition transition, VisualPlace predPlace) {

        VisualConnection succPlacePredConnection = getOnlyPredConnectionOrNull(stg, succPlace);
        VisualConnection succPlaceSuccConnection = getOnlySuccConnectionOrNull(stg, succPlace);

        if ((succPlacePredConnection != null) && (succPlaceSuccConnection != null)) {
            LinkedList<Point2D> succControlPoints = new LinkedList<>();
            VisualConnection transitionPredConnection = stg.getConnection(predPlace, transition);
            if (transitionPredConnection != null) {
                succControlPoints.addAll(
                        ConnectionHelper.getMergedControlPoints(transition, transitionPredConnection, null));
            }

            succControlPoints.addAll(
                    ConnectionHelper.getMergedControlPoints(succPlace, succPlacePredConnection, succPlaceSuccConnection));

            VisualConnection productPlaceSuccConnection = stg.getConnection(productPlace, succPlaceSuccConnection.getSecond());
            if (productPlaceSuccConnection != null) {
                ConnectionHelper.straightenConnection(productPlaceSuccConnection);
                ConnectionHelper.addControlPoints(productPlaceSuccConnection, succControlPoints);
                filterControlPoints(productPlaceSuccConnection);
            }
        }
    }

    @Override
    public void shapeProductPredConnections(VisualModel model,
            Map<VisualConnection, VisualConnection> productToOriginalConnectionMap,
            VisualPlace productPlace, ProductPlacePositioning productPlacePositioning,
            VisualPlace predPlace, VisualTransition transition) {

        if (!convertedImplicitPlaces.contains(predPlace)) {
            super.shapeProductPredConnections(model, productToOriginalConnectionMap, productPlace,
                    productPlacePositioning, predPlace, transition);
        }

    }
    @Override
    public void shapeProductSuccConnections(VisualModel model,
            Map<VisualConnection, VisualConnection> productToOriginalConnectionMap,
            VisualPlace productPlace, ProductPlacePositioning productPlacePositioning,
            VisualTransition transition, VisualPlace succPlace) {

        if (!convertedImplicitPlaces.contains(succPlace)) {
            super.shapeProductSuccConnections(model, productToOriginalConnectionMap, productPlace,
                    productPlacePositioning, transition, succPlace);
        }

    }

    private VisualConnection getOnlyPredConnectionOrNull(VisualStg stg, VisualTransformableNode node) {
        VisualConnection result = null;
        for (VisualConnection connection : stg.getConnections(node)) {
            if (connection.getSecond() == node) {
                if (result == null) {
                    result = connection;
                } else {
                    return null;
                }
            }
        }
        return result;
    }

    private VisualConnection getOnlySuccConnectionOrNull(VisualStg stg, VisualTransformableNode node) {
        VisualConnection result = null;
        for (VisualConnection connection : stg.getConnections(node)) {
            if (connection.getFirst() == node) {
                if (result == null) {
                    result = connection;
                } else {
                    return null;
                }
            }
        }
        return result;
    }

    @Override
    public VisualStgPlace createProductPlace(VisualModel model, VisualPlace predPlace, VisualPlace succPlace) {
        Container visualContainer = (Container) Hierarchy.getCommonParent(predPlace, succPlace);
        Container mathContainer = NamespaceHelper.getMathContainer(model, visualContainer);
        MathModel mathModel = model.getMathModel();
        StgPlace mathPlace = mathModel.createNode(null, mathContainer, StgPlace.class);
        return model.createVisualComponent(mathPlace, VisualStgPlace.class, visualContainer);
    }

}

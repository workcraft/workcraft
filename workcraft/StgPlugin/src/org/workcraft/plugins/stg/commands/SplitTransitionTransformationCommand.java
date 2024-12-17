package org.workcraft.plugins.stg.commands;

import org.workcraft.commands.AbstractSplitTransformationCommand;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.utils.ConversionUtils;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualNamedTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public final class SplitTransitionTransformationCommand extends AbstractSplitTransformationCommand {

    public SplitTransitionTransformationCommand() {
        registerSplittableClass(VisualNamedTransition.class);
    }

    @Override
    public String getDisplayName() {
        return "Split selected transition";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Split transition";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public void beforeNodeTransformation(VisualModel model, VisualNode node) {
        if (model instanceof VisualStg stg) {
            for (VisualConnection connection : stg.getConnections(node)) {
                if (connection instanceof VisualReadArc readArc) {
                    ConversionUtils.convertReadArcTotDualArc(model, readArc);
                }
            }
        }
    }

    @Override
    public VisualComponent createDuplicate(VisualModel model, VisualComponent component) {
        if (model instanceof VisualStg stg) {
            if (component instanceof VisualDummyTransition dummyTransition) {
                VisualDummyTransition result = stg.createVisualDummyTransition(stg.getMathName(dummyTransition),
                        Hierarchy.getNearestContainer(component));

                result.copyPosition(component);
                return result;
            }
            if (component instanceof VisualSignalTransition signalTransition) {
                VisualSignalTransition result = stg.createVisualSignalTransition(stg.getSignalReference(signalTransition),
                        signalTransition.getSignalType(), signalTransition.getDirection(),
                        Hierarchy.getNearestContainer(component));

                result.copyPosition(component);
                return result;
            }
        }
        return super.createDuplicate(model, component);
    }

    @Override
    public void removeOriginalComponentAndInheritName(VisualModel model, VisualComponent component,
            VisualComponent firstComponent, VisualComponent secondComponent) {

        model.remove(component);
        // Split transitions do not need renaming as are instances of the original one
    }

}

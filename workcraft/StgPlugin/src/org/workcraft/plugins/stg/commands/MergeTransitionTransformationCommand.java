package org.workcraft.plugins.stg.commands;

import org.workcraft.commands.AbstractMergeTransformationCommand;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.HashSet;
import java.util.Set;

public final class MergeTransitionTransformationCommand extends AbstractMergeTransformationCommand {

    public MergeTransitionTransformationCommand() {
        registerMergableClass(VisualSignalTransition.class);
        registerMergableClass(VisualDummyTransition.class);
    }

    @Override
    public String getDisplayName() {
        return "Merge selected transitions";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public <T extends VisualComponent> T createMergedComponent(VisualModel model, Set<VisualComponent> components, Class<T> type) {
        T result = super.createMergedComponent(model, components, type);

        HashSet<String> signalNames = new HashSet<>();
        for (VisualComponent component: components) {
            if (component instanceof VisualSignalTransition signalTransition) {
                signalNames.add(signalTransition.getSignalName());
            }
        }
        if (model.getMathModel() instanceof Stg stg) {
            StringBuilder resultSignalName = null;
            for (String signalName: signalNames) {
                if (resultSignalName == null) {
                    resultSignalName = new StringBuilder(signalName);
                } else {
                    resultSignalName.append("_").append(signalName);
                }
            }
            if (resultSignalName != null) {
                stg.setName(result.getReferencedComponent(), resultSignalName.toString());
            }
        }
        return result;
    }

}

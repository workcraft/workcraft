package org.workcraft.plugins.stg.tools;

import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.graph.tools.AbstractMergeTransformationCommand;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public final class MergeTransitionTransformationCommand extends AbstractMergeTransformationCommand {

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
        return null;
    }

    @Override
    public Set<Class<? extends VisualComponent>> getMergableClasses() {
        Set<Class<? extends VisualComponent>> result = super.getMergableClasses();
        result.add(VisualSignalTransition.class);
        result.add(VisualDummyTransition.class);
        return result;
    }

    @Override
    public <T extends VisualComponent> T createMergedComponent(VisualModel model, Set<VisualComponent> components, Class<T> type) {
        T result = super.createMergedComponent(model, components, type);

        HashSet<String> signalNames = new HashSet<>();
        for (VisualComponent component: components) {
            if (component instanceof VisualSignalTransition) {
                VisualSignalTransition signalTransition = (VisualSignalTransition) component;
                signalNames.add(signalTransition.getSignalName());
            }
        }
        if (model.getMathModel() instanceof Stg) {
            Stg stg = (Stg) model.getMathModel();
            String resultSignalName = null;
            for (String signalName: signalNames) {
                if (resultSignalName == null) {
                    resultSignalName = signalName;
                } else {
                    resultSignalName = resultSignalName + "_" + signalName;
                }
            }
            if (resultSignalName != null) {
                stg.setName(result.getReferencedComponent(), resultSignalName);
            }
        }
        return result;
    }

}

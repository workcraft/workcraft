package org.workcraft.utils;

import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.Container;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.LayoutException;
import org.workcraft.observation.ModelModifiedEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.builtin.commands.DotLayoutCommand;
import org.workcraft.plugins.builtin.commands.RandomLayoutCommand;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;

public final class LayoutUtils {

    private LayoutUtils() {
    }

    public static boolean attemptLayout(WorkspaceEntry we) {
        return attemptLayout(we.getModelEntry().getVisualModel());
    }

    public static boolean attemptLayout(VisualModel visualModel) {
        Container root = visualModel.getRoot();
        int nodeCount = Hierarchy.getDescendantsOfType(root, VisualNode.class).size();
        int answer = 0;
        if (nodeCount > EditorCommonSettings.getLargeModelSize()) {
            String message = "The model may be too large for automatic layout (" + nodeCount + " elements)."
                    + "\nPerform layout anyway before opening in editor?";

            answer = DialogUtils.showYesNoCancel(message, "Graph layout",
                    JOptionPane.QUESTION_MESSAGE, JOptionPane.NO_OPTION);
        }

        if (answer > 1) {
            return false;
        }
        // FIXME: Send notification to components, so their dimensions are updated before layout.
        for (VisualComponent component : Hierarchy.getDescendantsOfType(root, VisualComponent.class)) {
            if (component instanceof StateObserver) {
                ((StateObserver) component).notify(new ModelModifiedEvent(visualModel));
            }
        }
        AbstractLayoutCommand layoutCommand = answer == 0
                ? visualModel.getBestLayouter()
                : visualModel.getFallbackLayouter();

        if (layoutCommand == null) {
            layoutCommand = new DotLayoutCommand();
        }
        try {
            layoutCommand.layout(visualModel);
        } catch (LayoutException e) {
            layoutCommand = new RandomLayoutCommand();
            layoutCommand.layout(visualModel);
        }
        return true;
    }

}

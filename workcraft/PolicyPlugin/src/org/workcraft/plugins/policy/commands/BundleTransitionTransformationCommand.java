package org.workcraft.plugins.policy.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.plugins.policy.VisualPolicy;
import org.workcraft.plugins.policy.tools.PolicyToPetriConverter;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class BundleTransitionTransformationCommand extends AbstractTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Bundle transitions";
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualPolicy.class);
    }

    @Override
    public void transform(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        we.saveMemento();
        final VisualPolicy policy = WorkspaceUtils.getAs(we, VisualPolicy.class);
        policy.unbundleTransitions(policy.getVisualBundledTransitions());
        final PolicyToPetriConverter converter = new PolicyToPetriConverter(policy);
        final TransitionBundler bundler = new TransitionBundler(converter);
        bundler.run();
        if (framework.isInGuiMode()) {
            final MainWindow mainWindow = framework.getMainWindow();
            final GraphEditorPanel editor = mainWindow.getCurrentEditor();
            editor.repaint();
        }
    }

}

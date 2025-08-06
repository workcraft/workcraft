package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.function.Function;

public abstract class AbstractTagCommand implements ScriptableCommand<Void> {

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.NEVER;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Circuit.class);
    }

    @Override
    public final void run(WorkspaceEntry we) {
        // Run synchronously (blocking the editor) as model is changed.
        execute(we);
    }

    @Override
    public final Void execute(WorkspaceEntry we) {
        we.captureMemento();
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        Collection<Contact> contacts = getFunction().apply(circuit);
        if (contacts.isEmpty()) {
            we.uncaptureMemento();
        } else {
            we.saveMemento();
            Collection<String> refs = ReferenceHelper.getReferenceList(circuit, contacts);
            LogUtils.logInfo(TextUtils.wrapMessageWithItems(getMessage(), refs));
        }

        final Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            final Toolbox toolbox = framework.getMainWindow().getToolbox(we);
            Class<? extends GraphEditorTool> toolClass = getToolClass();
            toolbox.selectTool(toolbox.getToolInstance(toolClass));
        }
        return null;
    }

    public abstract Function<Circuit, Collection<Contact>> getFunction();

    public abstract String getMessage();

    public abstract Class<? extends GraphEditorTool> getToolClass();

}

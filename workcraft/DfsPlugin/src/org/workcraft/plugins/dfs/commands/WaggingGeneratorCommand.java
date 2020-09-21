package org.workcraft.plugins.dfs.commands;

import org.workcraft.commands.ScriptableCommand;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.VisualRegister;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WaggingGeneratorCommand implements ScriptableCommand<Void> {

    @Override
    public String getDisplayName() {
        return "Custom wagging...";
    }

    @Override
    public String getSection() {
        return "Wagging";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, Dfs.class);
    }

    @Override
    public final void run(WorkspaceEntry we) {
        // Run synchronously (blocking the editor) as model is changed.
        execute(we);
    }

    @Override
    public final Void execute(WorkspaceEntry we) {
        if (checkPrerequisites(we)) {
            VisualDfs dfs = WorkspaceUtils.getAs(we, VisualDfs.class);
            int count = getWayCount();
            if (count < 2) {
                DialogUtils.showError("Wagging cannot be less than 2-way.");
            } else {
                we.saveMemento();
                WaggingGenerator generator = new WaggingGenerator(dfs, count);
                generator.run();
            }
        }
        return null;
    }

    private boolean checkPrerequisites(WorkspaceEntry we) {
        if (!isApplicableTo(we)) {
            return false;
        }
        VisualDfs dfs = WorkspaceUtils.getAs(we, VisualDfs.class);
        Set<VisualNode> selection = new HashSet<>(dfs.getSelection());
        return hasRegister(selection) && hasEntryRegister(dfs, selection);

    }

    private boolean hasRegister(Collection<VisualNode> nodes) {
        int selectedRegisterCount = 0;
        for (VisualNode node : nodes) {
            if (node instanceof VisualRegister) {
                selectedRegisterCount++;
            }
        }
        if (selectedRegisterCount < 1) {
            DialogUtils.showError("Select at least one register for wagging.");
            return false;
        }
        return true;
    }

    private boolean hasEntryRegister(VisualDfs dfs, Set<VisualNode> nodes) {
        Collection<String> nonRegisterEntryRefs = new ArrayList<>();
        for (VisualNode node : nodes) {
            if (node instanceof VisualComponent) {
                for (VisualNode predNode : dfs.getPreset(node)) {
                    if ((node instanceof VisualRegister) || nodes.contains(predNode)) continue;
                    nonRegisterEntryRefs.add(dfs.getMathReference(node));
                }
            }
        }
        if (!nonRegisterEntryRefs.isEmpty()) {
            String msg = "It is advised to have registers at the entry to wagging slice.\n" +
                    "This enables the token to propagate into the active slice with the\n" +
                    "minimum delay and free the space for the next token.\n\n" +
                    TextUtils.wrapMessageWithItems("Non-register entry component", nonRegisterEntryRefs) +
                    "\n\nProceed anyway?";

            return DialogUtils.showConfirmWarning(msg);
        }
        return true;
    }

    public int getWayCount() {
        int count = 0;
        String ans = DialogUtils.showInput("Enter the number of wagging branches:", "4");
        if (ans != null) {
            try {
                count = Integer.parseInt(ans);
            } catch (NumberFormatException e) {
                DialogUtils.showError("Your input is not an integer.");
            }
        }
        return count;
    }

}

package org.workcraft.plugins.dfs.commands;

import org.workcraft.commands.ScriptableCommand;
import org.workcraft.dom.Node;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.VisualRegister;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
    public final Void execute(WorkspaceEntry we) {
        final VisualDfs dfs = WorkspaceUtils.getAs(we, VisualDfs.class);
        int selectedRegisterCount = 0;
        for (Node node: dfs.getSelection()) {
            if (node instanceof VisualRegister) {
                selectedRegisterCount++;
            }
        }
        if (selectedRegisterCount < 1) {
            DialogUtils.showError("Select at least one register for wagging.");
        } else {
            int count = getWayCount();
            if (count >= 2) {
                we.saveMemento();
                WaggingGenerator generator = new WaggingGenerator(dfs, count);
                generator.run();
            }
        }
        return null;
    }

    public int getWayCount() {
        int count = 0;
        String ans = DialogUtils.showInput("Enter the number of wagging branches:", "4");
        if (ans != null) {
            try {
                count = Integer.parseInt(ans);
                if (count < 2) {
                    DialogUtils.showError("Wagging cannot be less than 2-way.");
                }
            } catch (NumberFormatException e) {
                DialogUtils.showError("Your input is not an integer.");
            }
        }
        return count;
    }

}

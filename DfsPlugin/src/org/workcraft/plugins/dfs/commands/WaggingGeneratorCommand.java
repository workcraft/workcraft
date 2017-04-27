package org.workcraft.plugins.dfs.commands;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.gui.graph.commands.ScriptableCommand;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.VisualRegister;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class WaggingGeneratorCommand implements ScriptableCommand {

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
    public final WorkspaceEntry execute(WorkspaceEntry we) {
        final VisualDfs dfs = WorkspaceUtils.getAs(we, VisualDfs.class);
        int selectedRegisterCount = 0;
        for (Node node: dfs.getSelection()) {
            if (node instanceof VisualRegister) {
                selectedRegisterCount++;
            }
        }
        if (selectedRegisterCount < 1) {
            final Framework framework = Framework.getInstance();
            JOptionPane.showMessageDialog(framework.getMainWindow(),
                    "Select at least one register for wagging!", "Wagging", JOptionPane.ERROR_MESSAGE);
        } else {
            int count = getWayCount();
            if (count >= 2) {
                we.saveMemento();
                WaggingGenerator generator = new WaggingGenerator(dfs, count);
                generator.run();
            }
        }
        return we;
    }

    @Override
    public final void run(WorkspaceEntry we) {
        execute(we);
    }

    public int getWayCount() {
        int count = 0;
        final Framework framework = Framework.getInstance();
        String ans = JOptionPane.showInputDialog(framework.getMainWindow(),
                "Enter the number of wagging branches:", "4");
        if (ans != null) {
            try {
                count = Integer.parseInt(ans);
                if (count < 2) {
                    JOptionPane.showMessageDialog(framework.getMainWindow(),
                            "Wagging cannot be less than 2-way!", "Wagging", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(framework.getMainWindow(),
                        "Your input is not an integer!", "Wagging", JOptionPane.ERROR_MESSAGE);
            }
        }
        return count;
    }

}

package org.workcraft.plugins.dfs.tools;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.VisualRegister;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class WaggingGeneratorTool implements Tool {

    @Override
    public String getDisplayName() {
        return "Custom wagging...";
    }

    @Override
    public String getSection() {
        return "Wagging";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicableExact(me, Dfs.class);
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        return me; // !!!
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        final VisualDfs dfs = (VisualDfs) we.getModelEntry().getVisualModel();
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

    public int getWayCount() {
        int count = 0;
        final Framework framework = Framework.getInstance();
        String ans = JOptionPane.showInputDialog(framework.getMainWindow(),
                "Please enter the number of wagging branches:", "4");
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

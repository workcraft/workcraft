package org.workcraft.plugins.statistics;

import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.gui.graph.commands.AbstractStatisticsCommand;
import org.workcraft.util.MultiSet;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class BasicStatisticsCommand extends AbstractStatisticsCommand {

    @Override
    public String getDisplayName() {
        return "Component count";
    }

    @Override
    public String getStatistics(WorkspaceEntry we) {
        AbstractMathModel model = WorkspaceUtils.getAs(we, AbstractMathModel.class);
        String s = "";
        MultiSet<String> statistics = model.getStatistics();
        for (String categoryName: statistics.toSet()) {
            String displayName = renameCategory(categoryName);
            if ((displayName == null) || displayName.isEmpty()) continue;
            s += "  " + displayName + " -  " + statistics.count(categoryName) + "\n";
        }
        if (s.isEmpty()) {
            return "The model is empty.";
        }
        return "Component count:\n" + s;
    }

    public String renameCategory(String categoryName) {
        if (CommentNode.class.getSimpleName().equals(categoryName)) {
            return "";
        }
        if (MathConnection.class.getSimpleName().equals(categoryName)) {
            return "Arc";
        }
        return categoryName;
    }

}

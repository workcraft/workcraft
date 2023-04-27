package org.workcraft.plugins.builtin.commands;

import org.workcraft.commands.AbstractStatisticsCommand;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.types.MultiSet;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

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
            s += "\n  " + displayName + " -  " + statistics.count(categoryName);
        }
        if (s.isEmpty()) {
            return "The model is empty.";
        }
        return "Component count:" + s + '\n';
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

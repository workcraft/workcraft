package org.workcraft.plugins.xbm.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.xbm.VisualXbm;
import org.workcraft.plugins.xbm.Xbm;
import org.workcraft.plugins.xbm.XbmState;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class UniqueStateEncodingVerification extends AbstractVerificationCommand {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Unique State Encoding Check";
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        if (!isApplicableTo(we)) {
            return null;
        }
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        final Xbm xbm = WorkspaceUtils.getAs(we, Xbm.class);
        HashSet<XbmState> commonStateEncodings = findCommonStateEncodings(xbm);

        if (commonStateEncodings.isEmpty()) {
            DialogUtils.showInfo("All states have an unique state encoding.", TITLE);
        } else {
            String msg = "The unique state encoding property was violated due to the following states having the same encoding:\n" + getStatesAsString(xbm, commonStateEncodings)
                    + "\n\nSelect states with common encodings?\n";
            if (DialogUtils.showConfirmInfo(msg, TITLE, true)) {
                VisualXbm visualXbm = WorkspaceUtils.getAs(we, VisualXbm.class);
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                SelectionHelper.selectByReferencedComponents(visualXbm, commonStateEncodings);
            }
        }
        return commonStateEncodings.isEmpty();
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Xbm.class);
    }

    private HashSet<XbmState> findCommonStateEncodings(final Xbm xbm) {
        final Collection<XbmState> states = Hierarchy.getDescendantsOfType(xbm.getRoot(), XbmState.class);
        final HashSet<XbmState> result = new LinkedHashSet<>();
        for (XbmState s1: states) {
            for (XbmState s2: states) {
                if (s1 != s2 && !(s1.getStateEncoding().isEmpty() || s2.getStateEncoding().isEmpty()) &&
                        s1.getEncoding().entrySet().equals(s2.getEncoding().entrySet())) {

                    result.add(s1);
                    result.add(s2);
                }
            }
        }
        return result;
    }

    private static String getStatesAsString(Xbm xbm, Set<XbmState> states) {
        String result = "";
        for (XbmState state: states) {
            if (!result.isEmpty()) {
                result += ", ";
            }
            result += xbm.getNodeReference(state);
        }
        return result;
    }
}

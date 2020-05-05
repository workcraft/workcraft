package org.workcraft.plugins.punf.tasks;

import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.plugins.punf.utils.Ltl2tgbaUtils;
import org.workcraft.tasks.AbstractOutputInterpreter;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.IOException;

public class Ltl2tgbaOutputInterpreter extends AbstractOutputInterpreter<Ltl2tgbaOutput, Boolean> {

    public static final String TITLE = "Verification results";

    public Ltl2tgbaOutputInterpreter(WorkspaceEntry we, Ltl2tgbaOutput output, boolean interactive) {
        super(we, output, interactive);
    }

    public void showOutcome(boolean stutterInvariant, Pair<String, String> example) {
        String message = getMessage(example);
        if (stutterInvariant) {
            if (isInteractive()) {
                DialogUtils.showInfo(message, TITLE);
            } else {
                LogUtils.logInfo(message);
            }
        } else {
            if (isInteractive()) {
                DialogUtils.showWarning(message, TITLE);
            } else {
                LogUtils.logWarning(message);
            }
        }
    }

    public String getMessage(Pair<String, String> example) {
        return example == null ? "The property is stutter-invariant"
                : "The property is stutter-sensitive as shown by the following stutter-equivalent words:"
                + "\n" + PropertyHelper.BULLET_PREFIX + "Accepted word: " + example.getFirst()
                + "\n" + PropertyHelper.BULLET_PREFIX + "Rejected word: " + example.getSecond();
    }

    @Override
    public Boolean interpret() {
        if (getOutput() == null) {
            return null;
        }
        try {
            Pair<String, String> example = Ltl2tgbaUtils.extraxtStutterExample(getOutput());
            boolean stutterInvariant = example == null;
            showOutcome(stutterInvariant, example);
            return stutterInvariant;
        } catch (IOException e) {
        }
        return null;
    }

}

package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.plugins.mpsat_verification.utils.SpotUtils;
import org.workcraft.tasks.AbstractOutputInterpreter;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.IOException;

public class Ltl2tgbaOutputInterpreter extends AbstractOutputInterpreter<Ltl2tgbaOutput, Boolean> {

    public Ltl2tgbaOutputInterpreter(WorkspaceEntry we, Ltl2tgbaOutput output, boolean interactive) {
        super(we, output, interactive);
    }

    public void showOutcome(Pair<String, String> example) {
        if (example == null) {
            String msg = "The property is stutter-invariant.";
            if (isInteractive()) {
                DialogUtils.showInfo(msg);
            } else {
                LogUtils.logInfo(msg);
            }
        } else {
            String message = "The property is stutter-sensitive as shown by the following stutter-equivalent words:"
                    + "\n" + PropertyHelper.BULLET_PREFIX + "Accepted word: " + example.getFirst()
                    + "\n" + PropertyHelper.BULLET_PREFIX + "Rejected word: " + example.getSecond();

            if (isInteractive()) {
                DialogUtils.showError(message);
            } else {
                LogUtils.logError(message);
            }
        }
    }

    @Override
    public Boolean interpret() {
        if (getOutput() == null) {
            return null;
        }
        try {
            Pair<String, String> example = SpotUtils.extractStutterExample(getOutput());
            showOutcome(example);
            return example == null;
        } catch (IOException e) {
        }
        return null;
    }

}

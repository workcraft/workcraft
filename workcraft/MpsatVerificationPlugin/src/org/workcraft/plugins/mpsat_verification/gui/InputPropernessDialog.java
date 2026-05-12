package org.workcraft.plugins.mpsat_verification.gui;

import org.workcraft.gui.dialogs.ListDataDialog;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.mpsat_verification.commands.InputPropernessVerificationCommand;
import org.workcraft.plugins.mpsat_verification.presets.InputPropernessDataPreserver;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.WorkspaceUtils;

import java.awt.*;
import java.util.Collection;

public class InputPropernessDialog extends ListDataDialog {

    public InputPropernessDialog(Window owner, InputPropernessDataPreserver userData) {
        super(owner, InputPropernessVerificationCommand.TITLE, userData);
    }

    @Override
    public Color getItemColorOrNullForInvalid(Object item) {
        return isValidItem(item) ? SignalCommonSettings.getInputColor() : super.getItemColorOrNullForInvalid(item);
    }

    @Override
    public Collection<String> getItems() {
        Stg stg = WorkspaceUtils.getAs(getUserData().getWorkspaceEntry(), Stg.class);
        return stg.getSignalReferences(Signal.Type.INPUT);
    }

}

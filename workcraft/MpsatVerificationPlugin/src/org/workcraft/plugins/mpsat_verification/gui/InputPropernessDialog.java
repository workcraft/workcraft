package org.workcraft.plugins.mpsat_verification.gui;

import org.workcraft.gui.dialogs.ListDataDialog;
import org.workcraft.gui.lists.ColorListCellRenderer;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.mpsat_verification.presets.InputPropernessDataPreserver;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.WorkspaceUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class InputPropernessDialog extends ListDataDialog {

    public InputPropernessDialog(Window owner, InputPropernessDataPreserver userData) {
        super(owner, "Input properness", userData);
    }

    @Override
    public DefaultListCellRenderer getItemListCellRenderer() {
        return new ColorListCellRenderer(item -> SignalCommonSettings.getInputColor());
    }

    @Override
    public Collection<String> getItems() {
        Stg stg = WorkspaceUtils.getAs(getUserData().getWorkspaceEntry(), Stg.class);
        return stg.getSignalReferences(Signal.Type.INPUT);
    }

}

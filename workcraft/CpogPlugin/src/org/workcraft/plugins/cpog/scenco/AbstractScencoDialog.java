package org.workcraft.plugins.cpog.scenco;

import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.VisualCpog;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractScencoDialog extends JDialog {

    private final EncoderSettings settings;
    private final VisualCpog model;

    public AbstractScencoDialog(Window owner, String title, EncoderSettings settings, VisualCpog model) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.settings = settings;
        this.model = model;
    }

    public EncoderSettings getSettings() {
        return settings;
    }

    public VisualCpog getModel() {
        return model;
    }

    public boolean reveal() {
        setVisible(true);
        return true;
    }

}

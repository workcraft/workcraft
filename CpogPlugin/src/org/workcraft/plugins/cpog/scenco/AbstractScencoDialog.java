package org.workcraft.plugins.cpog.scenco;

import java.awt.Dimension;
import java.awt.Window;

import javax.swing.JDialog;

import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.VisualCpog;

@SuppressWarnings("serial")
public abstract class AbstractScencoDialog extends JDialog {

    private final EncoderSettings settings;
    private final VisualCpog model;
    private boolean done;

    public AbstractScencoDialog(Window owner, String title, EncoderSettings settings, VisualCpog model) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.settings = settings;
        this.model = model;
        done = false;
    }

    public void sizeWindow(int width, int height) {
        setMinimumSize(new Dimension(width, height));
        pack();
    }

    public EncoderSettings getSettings() {
        return settings;
    }

    public VisualCpog getModel() {
        return model;
    }

    public void setDone() {
        done = true;
    }

    public boolean isDone() {
        return done;
    }

}

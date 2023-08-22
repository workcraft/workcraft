package org.workcraft.gui.lists;

import javax.swing.*;

public class MultipleListSelectionModel extends DefaultListSelectionModel {

    private boolean started = false;

    @Override
    public void setSelectionInterval(int index0, int index1) {
        if (!started) {
            if (isSelectedIndex(index0)) {
                super.removeSelectionInterval(index0, index1);
            } else {
                super.addSelectionInterval(index0, index1);
            }
        }
        started = true;
    }

    @Override
    public void setValueIsAdjusting(boolean isAdjusting) {
        if (!isAdjusting) {
            started = false;
        }
    }

}

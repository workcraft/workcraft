package org.workcraft.gui.actions;

import org.workcraft.Framework;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.interop.Exporter;

public class ExportAction extends Action {
    private final Exporter exporter;

    public ExportAction(Exporter exporter) {
        this.exporter = exporter;
    }

    @Override
    public void run() {
        try {
            final Framework framework = Framework.getInstance();
            framework.getMainWindow().export(exporter);
        } catch (OperationCancelledException e) {
        }
    }

    public String getText() {
        return exporter.getDescription();
    }

}

package org.workcraft.gui.actions;

import org.workcraft.Framework;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Format;

public class ExportAction extends Action {
    private final Exporter exporter;

    public ExportAction(Exporter exporter) {
        this.exporter = exporter;
    }

    @Override
    public void run() {
        try {
            Framework.getInstance().getMainWindow().export(exporter);
        } catch (OperationCancelledException e) {
        }
    }

    @Override
    public String getText() {
        Format format = exporter.getFormat();
        return format.getExtension() + "(" + format.getDescription() + ")";
    }

}

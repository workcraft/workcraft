package org.workcraft.exceptions;

import org.workcraft.dom.Model;
import org.workcraft.interop.Format;

public class NoExporterException extends RuntimeException {

    public NoExporterException(Model<?, ?> model, Format format) {
        this(model.getDisplayName(), format.getName());
    }

    public NoExporterException(String modelName, String formatName) {
        super("No exporter available for " + modelName + " model to produce " + formatName + " format.");
    }

}

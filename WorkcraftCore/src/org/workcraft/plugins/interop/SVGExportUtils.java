package org.workcraft.plugins.interop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.SerialisationException;

public class SVGExportUtils {

    public static InputStream stream(Model model) throws IOException, SerialisationException {
        SVGExporter svgExporter = new SVGExporter();
        ByteArrayOutputStream svgOut = new ByteArrayOutputStream();
        svgExporter.export(model, svgOut);
        return new ByteArrayInputStream(svgOut.toByteArray());
    }

}

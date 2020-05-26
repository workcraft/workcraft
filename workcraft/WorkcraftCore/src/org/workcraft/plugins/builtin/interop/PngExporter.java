package org.workcraft.plugins.builtin.interop;

import org.apache.batik.transcoder.image.PNGTranscoder;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.utils.BatikUtils;

import java.io.OutputStream;

public class PngExporter implements Exporter {

    @Override
    public void export(Model model, OutputStream out) throws SerialisationException {
        if (!(model instanceof VisualModel)) {
            throw new SerialisationException("Non-visual model cannot be exported as PNG file.");
        }
        BatikUtils.transcode((VisualModel) model, out, new PNGTranscoder());
    }

    @Override
    public boolean isCompatible(Model model) {
        return model instanceof VisualModel;
    }

    @Override
    public PngFormat getFormat() {
        return PngFormat.getInstance();
    }

}

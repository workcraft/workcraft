package org.workcraft.plugins.builtin.interop;

import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.utils.BatikUtils;

import java.io.OutputStream;

public class SvgExporter implements Exporter {

    @Override
    public boolean isCompatible(Model model) {
        return model instanceof VisualModel;
    }

    @Override
    public SvgFormat getFormat() {
        return SvgFormat.getInstance();
    }

    @Override
    public void serialise(Model model, OutputStream out) throws SerialisationException {
        BatikUtils.transcode((VisualModel) model, out);
    }

}

package org.workcraft.plugins.builtin.interop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.render.ps.EPSTranscoder;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;

public class EpsExporter implements Exporter {

    @Override
    public void export(Model model, OutputStream out) throws IOException, SerialisationException {
        InputStream svg = SvgExportUtils.streamTripleVote(model);
        Transcoder transcoder = new EPSTranscoder();
        TranscoderInput transcoderInput = new TranscoderInput(svg);
        TranscoderOutput transcoderOutput = new TranscoderOutput(out);
        try {
            transcoder.transcode(transcoderInput, transcoderOutput);
        } catch (TranscoderException e) {
            throw new SerialisationException(e);
        }
    }

    @Override
    public boolean isCompatible(Model model) {
        return model instanceof VisualModel;
    }

    @Override
    public EpsFormat getFormat() {
        return EpsFormat.getInstance();
    }

}

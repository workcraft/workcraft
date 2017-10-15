package org.workcraft.plugins.interop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;

public class PdfExporter implements Exporter {

    @Override
    public void export(Model model, OutputStream out) throws IOException, SerialisationException {
        InputStream svg = SvgExportUtils.stream(model);
        Transcoder transcoder = new PDFTranscoder();
        TranscoderInput transcoderInput = new TranscoderInput(svg);
        TranscoderOutput transcoderOutput = new TranscoderOutput(out);
        try {
            transcoder.transcode(transcoderInput, transcoderOutput);
        } catch (TranscoderException e) {
            throw new SerialisationException(e);
        }
    }

    @Override
    public int getCompatibility(Model model) {
        if (model instanceof VisualModel) {
            return Exporter.GENERAL_COMPATIBILITY;
        } else {
            return Exporter.NOT_COMPATIBLE;
        }
    }

    @Override
    public PdfFormat getFormat() {
        return PdfFormat.getInstance();
    }

}

package org.workcraft.plugins.interop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.serialisation.Format;

public class PDFExporter implements Exporter {

    @Override
    public void export(Model model, OutputStream out) throws IOException, SerialisationException {
        InputStream svg = SVGExportUtils.stream(model);
        Transcoder transcoder = new PDFTranscoder();
        TranscoderInput transcoderInput = new TranscoderInput(svg);
        TranscoderOutput transcoderOutput = new TranscoderOutput(out);
        // Configure logj4 output and set INFO verbosity.
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        try {
            transcoder.transcode(transcoderInput, transcoderOutput);
        } catch (TranscoderException e) {
            throw new SerialisationException(e);
        }
    }

    @Override
    public String getDescription() {
        return ".pdf (FOP PDF generator)";
    }

    @Override
    public String getExtenstion() {
        return ".pdf";
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
    public UUID getTargetFormat() {
        return Format.PDF;
    }

}

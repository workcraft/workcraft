package org.workcraft.utils;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.w3c.dom.Document;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.tools.Decorator;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class BatikUtils {

    private static final double SCALE_FACTOR = 50.0;

    public static void transcode(VisualModel model, OutputStream out)
            throws SerialisationException {

        generateSvgGraphics(model, out);
    }

    public static void transcode(VisualModel model, OutputStream out, Transcoder transcoder)
            throws SerialisationException {

        ByteArrayOutputStream bufOut = new ByteArrayOutputStream();
        generateSvgGraphics(model, bufOut);
        ByteArrayInputStream bufIn = new ByteArrayInputStream(bufOut.toByteArray());
        TranscoderInput transcoderInput = new TranscoderInput(bufIn);
        TranscoderOutput transcoderOutput = new TranscoderOutput(out);
        try {
            transcoder.transcode(transcoderInput, transcoderOutput);
        } catch (TranscoderException e) {
            throw new SerialisationException(e);
        }
    }

    private static void generateSvgGraphics(VisualModel model, OutputStream out) throws SerialisationException {
        try {
            Document doc = XmlUtils.createDocument();
            SVGGraphics2D g2d = new SVGGraphics2D(doc);
            g2d.setUnsupportedAttributes(null);
            g2d.scale(SCALE_FACTOR, SCALE_FACTOR);
            VisualGroup visualGroup = (VisualGroup) model.getRoot();
            Rectangle2D bounds = visualGroup.getBoundingBoxInLocalSpace();
            g2d.translate(-bounds.getMinX(), -bounds.getMinY());
            int canvasWidth = (int) (bounds.getWidth() * SCALE_FACTOR);
            int canvasHeight = (int) (bounds.getHeight() * SCALE_FACTOR);
            g2d.setSVGCanvasSize(new Dimension(canvasWidth, canvasHeight));
            model.draw(g2d, Decorator.Empty.INSTANCE);
            g2d.stream(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            throw new SerialisationException(e);
        }
    }

}

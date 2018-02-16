package org.workcraft.plugins.interop;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.interop.Exporter;
import org.workcraft.util.XmlUtils;

public class SvgExporter implements Exporter {

    private static final double SCALE_FACTOR = 50.0;

    @Override
    public void export(Model model, OutputStream out) throws IOException, SerialisationException {
        if (!(model instanceof VisualModel)) {
            throw new SerialisationException("Non-visual model cannot be exported as SVG file.");
        }
        VisualModel visualModel = (VisualModel) model;
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
            visualModel.draw(g2d, Decorator.Empty.INSTANCE);
            g2d.stream(new OutputStreamWriter(out));
        } catch (ParserConfigurationException e) {
            throw new SerialisationException(e);
        }
    }

    @Override
    public boolean isCompatible(Model model) {
        return model instanceof VisualModel;
    }

    @Override
    public SvgFormat getFormat() {
        return SvgFormat.getInstance();
    }

}

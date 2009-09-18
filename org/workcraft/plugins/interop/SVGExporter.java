package org.workcraft.plugins.interop;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.serialisation.Format;
import org.workcraft.util.XmlUtil;

public class SVGExporter implements Exporter {


	public void export(Model model, OutputStream out) throws IOException,
			ModelValidationException, SerialisationException {

		if (model == null)
			throw new SerialisationException ("Not a visual model");

			try {
				Document doc = XmlUtil.createDocument();

				SVGGraphics2D g2d = new SVGGraphics2D(doc);

				g2d.scale(50, 50);

				Rectangle2D bounds = ((VisualGroup)model.getRoot()).getBoundingBoxInLocalSpace();

				g2d.translate(-bounds.getMinX(), -bounds.getMinY());
				g2d.setSVGCanvasSize(new Dimension((int)bounds.getWidth()*50, (int)bounds.getHeight()*50));

				((VisualModel)model).draw(g2d);

				g2d.stream(new OutputStreamWriter(out));

			} catch (ParserConfigurationException e) {
				throw new SerialisationException(e);
			}
		}


	public String getDescription() {
		return ".svg (Batik SVG generator)";
	}

	public String getExtenstion() {
		return ".svg";
	}

	public boolean isApplicableTo(Model model) {
		if (model instanceof VisualModel)
			return true;
		else
			return false;
	}


	public UUID getFormatUUID() {
		return Format.STG;
	}
}
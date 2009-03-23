package org.workcraft.plugins.interop;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;
import org.workcraft.dom.Model;
import org.workcraft.framework.Exporter;
import org.workcraft.framework.exceptions.ExportException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.util.XmlUtil;

public class SVGExporter implements Exporter {


	public void exportToFile(Model model, File name) throws IOException,
			ModelValidationException, ExportException {

		if (model.getVisualModel() == null)
			throw new ExportException ("Not a visual model");

			try {
				Document doc = XmlUtil.createDocument();

				SVGGraphics2D g2d = new SVGGraphics2D(doc);

				g2d.scale(50, 50);

				Rectangle2D bounds = model.getVisualModel().getRoot().getBoundingBoxInLocalSpace();

				g2d.translate(-bounds.getMinX(), -bounds.getMinY());
				g2d.setSVGCanvasSize(new Dimension((int)bounds.getWidth()*50, (int)bounds.getHeight()*50));

				model.getVisualModel().draw(g2d);

				g2d.stream(name.getPath());

			} catch (ParserConfigurationException e) {
				throw new ExportException(e);
			}

	}


	public String getDescription() {
		return ".svg (Scalable Vector Graphics)";
	}

	public String getExtenstion() {
		return ".svg";
	}

	public boolean isApplicableTo(Model model) {
		if (model.getVisualModel() != null)
			return true;
		else
			return false;
	}

}
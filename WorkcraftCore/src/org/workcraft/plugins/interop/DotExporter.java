package org.workcraft.plugins.interop;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Hierarchy;

public class DotExporter implements Exporter {

	private class ExportNode {
		public final String id;
		public final double width;
		public final double height;
		public final Collection<String> destinations;
		public final String comment;

		public ExportNode(String id, double width, double height, Collection<String> destinations, String comment) {
			this.id = id;
			this.width = width;
			this.height = height;
			this.destinations = destinations;
			this.comment = comment;
		}

	}

	public static void export(Collection<ExportNode> nodes, OutputStream outStream) throws IOException {
		PrintStream out = new PrintStream(outStream);

		out.println("digraph work {");
//		out.println("graph [nodesep=\"0.5\", overlap=\"false\", splines=\"ortho\"];");
		out.println("graph [nodesep=\"0.5\", overlap=\"false\", splines=\"true\", ranksep=\"2.0\"];");
		out.println("node [shape=box];");

		for (ExportNode node : nodes) {
			out.println("\"" + node.id + "\" [width=\"" + node.width + "\", height=\"" + node.height + "\", fixedsize=\"true\"];  // " + node.comment);
			for(String target : node.destinations) {
				out.println("\"" + node.id + "\" -> \"" + target +"\";");
			}
		}
		out.println("}");
	}

	@Override
	public void export(Model model, OutputStream outStream) throws IOException,
			ModelValidationException, SerialisationException {

		final List<ExportNode> dotExportNodes = new ArrayList<ExportNode>();
		VisualModel visualModel = null;
		if (model instanceof VisualModel) {
			visualModel = (VisualModel)model;
		}
		for (VisualComponent component : Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualComponent.class)) {
			String id = visualModel.getNodeReference(component);
			Rectangle2D bb = component.getBoundingBoxInLocalSpace();
			if ((id != null) && (bb != null)) {
				List<String> destinations = new ArrayList<String>();
				Set<Node> postset = visualModel.getPostset(component);
				for(Node target : postset) {
					String targetId = visualModel.getNodeReference(target);
					if (targetId != null) {
						destinations.add(targetId);
					}
				}
				String comment = visualModel.getNodeMathReference(component);
				dotExportNodes.add(new ExportNode(id, bb.getWidth(), bb.getHeight(), destinations, comment));
			}
		}
		export(dotExportNodes, outStream);
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
	public String getDescription() {
		return ".dot (GraphViz dot graph format)";
	}

	@Override
	public String getExtenstion() {
		return ".dot";
	}

	@Override
	public UUID getTargetFormat() {
		return Format.DOT;
	}
}

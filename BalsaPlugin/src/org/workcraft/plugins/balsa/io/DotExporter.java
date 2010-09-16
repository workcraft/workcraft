package org.workcraft.plugins.balsa.io;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.workcraft.LegacyPlugin;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.balsa.VisualBalsaCircuit;
import org.workcraft.plugins.balsa.VisualBreezeComponent;
import org.workcraft.plugins.balsa.VisualHandshake;
import org.workcraft.plugins.interop.DotExportNode;
import org.workcraft.plugins.interop.DotExportable;
import org.workcraft.serialisation.Format;

public class DotExporter implements Exporter, LegacyPlugin {

	@Override
	public void export(final Model model, OutputStream outStream) throws IOException,
			ModelValidationException, SerialisationException {

		org.workcraft.plugins.interop.DotExporter.export(new DotExportable()
		{
			@Override
			public DotExportNode[] getNodes() {
				ArrayList<DotExportNode> result = new ArrayList<DotExportNode>();
				for (Node n : model.getRoot().getChildren()) {
					if (n instanceof VisualBreezeComponent) {
						VisualBreezeComponent comp = (VisualBreezeComponent) n;
						final String id = model.getNodeReference(comp);
						if(id!=null) {
							final Rectangle2D bb = comp.getBoundingBoxInLocalSpace();
							if(bb!=null) {

								final ArrayList<String> destinations = new ArrayList<String>();
								for (VisualHandshake hs : comp.getHandshakes())
								{
									if(hs.getHandshake().isActive())
									{
										Set<Node> nodes = new HashSet<Node>();
										nodes.addAll(model.getPostset(hs));
										nodes.addAll(model.getPreset(hs));
										for(Node target : nodes) {
											if (target instanceof VisualHandshake) {
												VisualBreezeComponent targetComp = (VisualBreezeComponent)((VisualHandshake)target).getParent();
												String targetId = model.getNodeReference(targetComp);
												if(targetId!=null) {
													destinations.add(targetId);
												}
											}
										}
									}
								}

								result.add(new DotExportNode()
								{
									@Override
									public String getName() {
										return id;
									}

									@Override
									public Dimension2D getDimensions() {
										return new Dimension2D()
										{

											@Override
											public double getWidth() {
												return bb.getWidth();
											}

											@Override
											public double getHeight() {
												return bb.getHeight();
											}

											@Override
											public void setSize(double width, double height) {
												throw new NotSupportedException();
											}
										};
									}

									@Override
									public String[] getOutgoingArcs() {
										return destinations.toArray(new String[0]);
									}

								});
							}
						}
					}
				}
				return result.toArray(new DotExportNode[0]);
			}

		}, outStream);

	}

	@Override
	public int getCompatibility(Model model) {
		if (model instanceof VisualBalsaCircuit)
			return Exporter.BEST_COMPATIBILITY;
		else
			return Exporter.NOT_COMPATIBLE;
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

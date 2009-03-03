package org.workcraft.dom.visual;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.MathNode;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;

public abstract class VisualComponent extends VisualTransformableNode {
	private Component refComponent = null;
	private HashSet<VisualConnection> connections = new HashSet<VisualConnection>();
	private HashSet<VisualComponent> preset = new HashSet<VisualComponent>();
	private HashSet<VisualComponent> postset = new HashSet<VisualComponent>();
	private String label = "";

	private static Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);
	private GlyphVector labelGlyphs = null;
	private Point2D labelPosition = null;
	private Color labelColor = Color.BLACK;

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration("Label", "getLabel", "setLabel", String.class));
	}

	public VisualComponent(Component refComponent) {
		super();
		this.refComponent = refComponent;
		addPropertyDeclarations();
	}

	public VisualComponent(Component refComponent, Element xmlElement) {
		super(xmlElement);
		this.refComponent = refComponent;
		addPropertyDeclarations();
	}

	public Set<VisualConnection> getConnections() {
		return new HashSet<VisualConnection>(connections);

	}

	final public void addConnection(VisualConnection connection) {
		connections.add(connection);

		if (connection.getFirst() == this)
			postset.add(connection.getSecond());
		else
			preset.add(connection.getFirst());

	}

	final public void removeConnection(VisualConnection connection) {
		connections.remove(connection);

		if (connection.getFirst() == this)
			postset.remove(connection.getSecond());
		else
			preset.remove(connection.getFirst());
	}

	final public Component getReferencedComponent() {
		return refComponent;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
		labelGlyphs = null;
	}

	final public Set<VisualComponent> getPreset() {
		return new HashSet<VisualComponent>(preset);
	}

	final public Set<VisualComponent> getPostset() {
		return new HashSet<VisualComponent>(postset);
	}

	public Set<MathNode> getReferences() {
		Set<MathNode> ret = new HashSet<MathNode>();
		ret.add(getReferencedComponent());
		return ret;
	}

	protected void drawLabelInLocalSpace(Graphics2D g) {
		if (labelGlyphs == null) {
			labelGlyphs = labelFont.createGlyphVector(g.getFontRenderContext(), label);
			Rectangle2D textBB = labelGlyphs.getVisualBounds();
			Rectangle2D bb = getBoundingBoxInLocalSpace();
			labelPosition = new Point2D.Double( bb.getMinX() + ( bb.getWidth() - textBB.getWidth() ) *0.5, bb.getMaxY() + textBB.getHeight() + 0.1);
		}

		g.setColor(Coloriser.colorise(labelColor, getColorisation()));
		g.drawGlyphVector(labelGlyphs, (float)labelPosition.getX(), (float)labelPosition.getY());
	}

}

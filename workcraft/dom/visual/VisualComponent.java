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
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.XmlUtil;

public abstract class VisualComponent extends VisualTransformableNode {
	private Component refComponent = null;
	private HashSet<VisualConnection> connections = new HashSet<VisualConnection>();


	private HashSet<VisualComponent> preset = new HashSet<VisualComponent>();
	private HashSet<VisualComponent> postset = new HashSet<VisualComponent>();


	private String label = "";

	private static Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);
	private GlyphVector labelGlyphs = null;
	private Point2D labelPosition = null;

	private Color labelColor = CommonVisualSettings.getForegroundColor();
	private Color foregroundColor = CommonVisualSettings.getForegroundColor();
	private Color fillColor = CommonVisualSettings.getFillColor();

	@Override
	public boolean isReferring(int ID) {
		return refComponent.getID()==ID;
	}

	private static class VisualComponentDeserialiser {

		public static void deserialise(Element element, VisualComponent node)
		{
			Element e = XmlUtil.getChildElement(VisualComponent.class.getSimpleName(), element);

			int ID = XmlUtil.readIntAttr(e, "ID", -1);
			node.setID(ID);

			node.setLabelColor(XmlUtil.readColorAttr(e, "labelColor", CommonVisualSettings.getForegroundColor()));
			node.setFillColor(XmlUtil.readColorAttr(e, "fillColor", CommonVisualSettings.getFillColor()));
			node.setForegroundColor(XmlUtil.readColorAttr(e, "foregroundColor", CommonVisualSettings.getForegroundColor()));

		}
	}


	private void addXMLSerialiser() {
		addXMLSerialiser(new XMLSerialiser(){
			public String getTagName() {
				return VisualComponent.class.getSimpleName();
			}
			public void serialise(Element element) {
				if (refComponent != null)
					XmlUtil.writeIntAttr(element, "refID", refComponent.getID());

				XmlUtil.writeIntAttr(element, "ID", getID());

				XmlUtil.writeColorAttr(element, "labelColor", getLabelColor());
				XmlUtil.writeColorAttr(element, "foregroundColor", getForegroundColor());
				XmlUtil.writeColorAttr(element, "fillColor", getFillColor());
			}
		});
	}
	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration("Label", "getLabel", "setLabel", String.class));
		addPropertyDeclaration(new PropertyDeclaration("Label color", "getLabelColor", "setLabelColor", Color.class));
		addPropertyDeclaration(new PropertyDeclaration("Foreground color", "getForegroundColor", "setForegroundColor", Color.class));
		addPropertyDeclaration(new PropertyDeclaration("Fill color", "getFillColor", "setFillColor", Color.class));
	}

	public VisualComponent(Component refComponent) {
		super();
		this.refComponent = refComponent;

		addPropertyDeclarations();
		addXMLSerialiser();

		setFillColor (CommonVisualSettings.getFillColor());
		setForegroundColor(CommonVisualSettings.getForegroundColor());
		setLabelColor(CommonVisualSettings.getForegroundColor());
	}

	public boolean isTurnable() {
		return false;
	}

	public void setRotation() {

	}

	public double getRotation() {
		return 0;
	}

	public void setRotation(double rot) {

	}

	public VisualComponent(Component refComponent, Element xmlElement) {
		super(xmlElement);
		this.refComponent = refComponent;

		VisualComponentDeserialiser.deserialise(xmlElement, this);

		addPropertyDeclarations();
		addXMLSerialiser();
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

	public GlyphVector getLabelGlyphs(Graphics2D g) {
		if (labelGlyphs == null) {
			labelGlyphs = labelFont.createGlyphVector(g.getFontRenderContext(), label);
		}

		return labelGlyphs;
	}

	public Rectangle2D getLabelBB(Graphics2D g) {
		if (labelGlyphs == null) {
			labelGlyphs = labelFont.createGlyphVector(g.getFontRenderContext(), label);
		}

		return labelGlyphs.getVisualBounds();
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

	public VisualComponent hitComponent(Point2D pointInLocalSpace) {
		if(hitTestInLocalSpace(pointInLocalSpace) != 0)
			return this;
		return null;
	}

	public Color getLabelColor() {
		return labelColor;
	}

	public void setLabelColor(Color labelColor) {
		this.labelColor = labelColor;
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}
}

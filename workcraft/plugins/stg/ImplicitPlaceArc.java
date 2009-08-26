package org.workcraft.plugins.stg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import org.w3c.dom.Element;
import org.workcraft.dom.Connection;
import org.workcraft.dom.MathNode;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.dom.visual.PopupMenuBuilder;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.actions.ScriptedActionMenuItem;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.util.XmlUtil;


public class ImplicitPlaceArc extends VisualConnection {
	private Place implicitPlace;
	private Connection refCon1;
	private Connection refCon2;

	private static double tokenSpaceSize = 0.8;
	private static double singleTokenSize = tokenSpaceSize / 1.9;
	private static double multipleTokenSeparation = 0.0125;
	private static Color tokenColor = Color.BLACK;

	private void addXMLSerialiser() {
		addXMLSerialiser(new XMLSerialiser() {
			public String getTagName() {
				return ImplicitPlaceArc.class.getSimpleName();
			}

			public void deserialise(Element element,
					ReferenceResolver refResolver) throws DeserialisationException {
				/*int ID = XmlUtil.readIntAttr(element2, "ID", -1);
				setID(ID);

				int firstID = XmlUtil.readIntAttr(element2, "first", -1);
				int secondID = XmlUtil.readIntAttr(element2, "second", -1);

				first = referenceResolver.getVisualComponentByID(firstID);
				second = referenceResolver.getVisualComponentByID(secondID);

				Element element = XmlUtil.getChildElement(ImplicitPlaceArc.class.getSimpleName(), xmlElement);

				implicitPlace = (Place)referenceResolver.getComponentByID(XmlUtil.readIntAttr(element, "placeRef", -1));
				refCon1 = referenceResolver.getConnectionByID(XmlUtil.readIntAttr(element, "refCon1", -1));
				refCon2 = referenceResolver.getConnectionByID(XmlUtil.readIntAttr(element, "refCon2", -1));

				readXMLConnectionProperties(element);*/
			}

			public void serialise(Element element,
					ExternalReferenceResolver refResolver) {
				XmlUtil.writeIntAttr(element, "refCon1", refCon1.getID());
				XmlUtil.writeIntAttr(element, "refCon2", refCon2.getID());
				XmlUtil.writeIntAttr(element, "placeRef", implicitPlace.getID());
				writeXMLConnectionProperties(element);
			}
		});
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration ("Tokens", "getTokens", "setTokens", int.class));
		addPropertyDeclaration(new PropertyDeclaration ("Capacity", "getCapacity", "setCapacity", int.class));

		addPopupMenuSegment(new PopupMenuBuilder.PopupMenuSegment() {
			public void addItems(JPopupMenu menu,
					ScriptedActionListener actionListener) {
				ScriptedActionMenuItem addToken = new ScriptedActionMenuItem(new VisualPlace.AddTokenAction(implicitPlace));
				addToken.addScriptedActionListener(actionListener);

				ScriptedActionMenuItem removeToken = new ScriptedActionMenuItem(new VisualPlace.RemoveTokenAction(implicitPlace));
				removeToken.addScriptedActionListener(actionListener);

				menu.add(new JLabel ("Implicit place"));
				menu.addSeparator();
				menu.add(addToken);
				menu.add(removeToken);
			}
		});
	}

	public ImplicitPlaceArc (VisualComponent first, VisualComponent second, Connection refCon1, Connection refCon2, Place implicitPlace) {
		super(null, first, second);
		this.refCon1 = refCon1;
		this.refCon2 = refCon2;
		this.implicitPlace = implicitPlace;

		addPropertyDeclarations();
		addXMLSerialiser();
	}

	public void draw(Graphics2D g) {
		super.draw(g);

		int tokens = implicitPlace.getTokens();

		Point2D p = getPointOnConnection(0.5);

		g.translate(p.getX(), p.getY());
		VisualPlace.drawTokens(tokens, singleTokenSize, multipleTokenSeparation, tokenSpaceSize, 0, Coloriser.colorise(tokenColor, getColorisation()), g);
	}

	public int getTokens() {
		return implicitPlace.getTokens();
	}

	public void setTokens(int tokens) {
		implicitPlace.setTokens(tokens);
	}

	public int getCapacity() {
		return implicitPlace.getCapacity();
	}

	public void setCapacity(int c) {
		implicitPlace.setCapacity(c);
	}

	public Place getImplicitPlace() {
		return implicitPlace;
	}

	public Connection getRefCon1() {
		return refCon1;
	}

	public Connection getRefCon2() {
		return refCon2;
	}

	@Override
	public Set<MathNode> getMathReferences() {
		Set<MathNode> ret = new HashSet<MathNode>();
		ret.add(implicitPlace);
		ret.add(refCon1);
		ret.add(refCon2);
		return ret;
	}

}

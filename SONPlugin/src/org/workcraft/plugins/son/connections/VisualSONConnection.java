package org.workcraft.plugins.son.connections;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;

public class VisualSONConnection extends VisualConnection {
	public VisualSONConnection() {
		this(null, null, null);
	}

	public VisualSONConnection(SONConnection refConnection) {
		this(refConnection, null, null);
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualSONConnection, String>(
				this, "Semantic", String.class, true, true, true) {
			public void setter(VisualSONConnection object, String value) {
			}
			public String getter(VisualSONConnection object) {
				switch (getSemantics()) {
				case PNLINE:
					return "Petri-net connection";
				case SYNCLINE:
					return "Synchronous communication";
				case ASYNLINE:
					return "Asynchronous communication";
				case BHVLINE:
					return "Behavioural abstraction";
				default:
					return getSemantics().toString();
				}
			}
		});
	}

	public VisualSONConnection(SONConnection refConnection, VisualComponent first, VisualComponent second) {
		super(refConnection, first, second);
		addPropertyDeclarations();
		removePropertyDeclarationByName(VisualConnection.PROPERTY_LINE_WIDTH);
		removePropertyDeclarationByName(VisualConnection.PROPERTY_ARROW_WIDTH);
		removePropertyDeclarationByName(VisualConnection.PROPERTY_ARROW_LENGTH);
	}

	public SONConnection getReferencedSONConnection() {
		return (SONConnection)getReferencedConnection();
	}

	public Semantics getSemantics() {
		return getReferencedSONConnection().getSemantics();
	}

	public void setSemantics(Semantics semantics) {
		getReferencedSONConnection().setSemantics(semantics);
		invalidate();
	}

	@Override
	public Stroke getStroke() {
		switch (getSemantics()) {
		case SYNCLINE:
		case ASYNLINE:
			return new BasicStroke(0.15f , BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.5f, new float[]{ 0.1f , 0.075f,}, 0f);
		case BHVLINE:
			return new BasicStroke( 0.02f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 2.0f, new float[]{ 0.24f , 0.15f,}, 0f);
		default:
			return super.getStroke();
		}
	}

	@Override
	public double getArrowWidth() {
		if (getSemantics() == Semantics.ASYNLINE) {
			return 0.3;
		}
		return super.getArrowWidth();
	}

	@Override
	public double getArrowLength() {
		if (getSemantics() == Semantics.ASYNLINE) {
			return 0.7;
		}
		return super.getArrowLength();
	}

	@Override
	public boolean hasArrow() {
		if (getSemantics() == Semantics.SYNCLINE) {
			return false;
		}
		return super.hasArrow();
	}
}

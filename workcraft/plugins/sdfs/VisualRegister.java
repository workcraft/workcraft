package org.workcraft.plugins.sdfs;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.PopupMenuBuilder;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.framework.plugins.HotKeyDeclaration;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.actions.ScriptedAction;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.actions.ScriptedActionMenuItem;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.XmlUtil;

@HotKeyDeclaration(KeyEvent.VK_R)
public class VisualRegister extends VisualComponent {
	static public class ToggleMarkedAction extends ScriptedAction {
		private int regID;
		String text;

		public ToggleMarkedAction(Register register) {
			super();
			this.regID = register.getID();

			if (register.isMarked())
				text = "Unmark";
			else
				text = "Mark";
		}
		public String getScript() {
			return "r=model.getComponentByID("+regID+");\nr.setMarked(!r.isMarked());\nmodel.fireNodePropertyChanged(\"Marked\", r);";
		}
		public String getUndoScript() {
			return getScript();
		}
		public String getRedoScript() {
			return getScript();
		}
		public String getText() {
			return text;
		}
	}

	static public class ToggleEnabledAction extends ScriptedAction {
		private int regID;
		String text;

		public ToggleEnabledAction(Register register) {
			super();
			this.regID = register.getID();

			if (register.isEnabled())
				text = "Disable";
			else
				text = "Enable";
		}
		public String getScript() {
			return "r=model.getComponentByID("+regID+");\nr.setEnabled(!r.isEnabled());\nmodel.fireNodePropertyChanged(\"Enabled\", r);";
		}
		public String getUndoScript() {
			return getScript();
		}
		public String getRedoScript() {
			return getScript();
		}
		public String getText() {
			return text;
		}
	}

	private static class VisualRegisterDeserialiser {

		public static void deserialise(Element element, VisualRegister node)
		{
			Element e = XmlUtil.getChildElement(VisualRegister.class.getSimpleName(), element);
			node.setLabelColor(XmlUtil.readColorAttr(e, "labelColor", SDFSVisualSettings.getForegroundColor()));
			node.setFillColor(XmlUtil.readColorAttr(e, "fillColor", SDFSVisualSettings.getFillColor()));
			node.setForegroundColor(XmlUtil.readColorAttr(e, "foregroundColor", SDFSVisualSettings.getForegroundColor()));
			node.setMarked(XmlUtil.readBoolAttr(e, "marked"));
			node.setEnabled(XmlUtil.readBoolAttr(e, "enabled"));
		}
	}

	private static Rectangle2D outerRect;
	private static Rectangle2D innerRect;
	private static Ellipse2D token;

	public Register getReferencedRegister() {
		return (Register)getReferencedComponent();
	}

	public VisualRegister(Register register) {
		super(register);
		addPropertyDeclarations();

		setFillColor (SDFSVisualSettings.getFillColor());
		setForegroundColor(SDFSVisualSettings.getForegroundColor());
		setLabelColor(SDFSVisualSettings.getForegroundColor());
	}

	public VisualRegister(Register register, Element xmlElement) {
		super(register, xmlElement);
		addPropertyDeclarations();
		VisualRegisterDeserialiser.deserialise(xmlElement, this);
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration ("Enabled", "isEnabled", "setEnabled", boolean.class));
		addPropertyDeclaration(new PropertyDeclaration ("Marked", "isMarked", "setMarked", boolean.class));

		addPopupMenuSegment(new PopupMenuBuilder.PopupMenuSegment() {
			public void addItems(JPopupMenu menu,
					ScriptedActionListener actionListener) {
				ScriptedActionMenuItem addToken = new ScriptedActionMenuItem(new ToggleMarkedAction(getReferencedRegister()));
				addToken.addScriptedActionListener(actionListener);

				ScriptedActionMenuItem removeToken = new ScriptedActionMenuItem(new ToggleEnabledAction(getReferencedRegister()));
				removeToken.addScriptedActionListener(actionListener);

				menu.add(new JLabel ("Register"));
				menu.addSeparator();
				menu.add(addToken);
				menu.add(removeToken);
			}
		});
	}


	@Override
	public void draw(Graphics2D g) {
		drawLabelInLocalSpace(g);

		double size = SDFSVisualSettings.getSize();
		double strokeWidth = SDFSVisualSettings.getStrokeWidth();


		outerRect = new Rectangle2D.Double (-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth);

		innerRect = new Rectangle2D.Double (
				-size / 2 + strokeWidth / 2 + size * 0.2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth - size * 0.4,
				size - strokeWidth);

		token = new Ellipse2D.Double (
				-size *0.15 , -size/2  + size / 8, size * 0.3, size * 0.3
			);



		g.setColor(Coloriser.colorise(getFillColor(), getColorisation()));
		g.fill(outerRect);
		g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));
		g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
		g.draw(outerRect);

		if (isEnabled())
			g.setColor(Coloriser.colorise(SDFSVisualSettings.getEnabledRegisterColor(), getColorisation()));
		else
			g.setColor(Coloriser.colorise(SDFSVisualSettings.getDisabledRegisterColor(), getColorisation()));
		g.fill(innerRect);

		g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));
		g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
		g.draw(innerRect);

		if (isMarked()) {
			g.setColor(SDFSVisualSettings.getTokenColor());
			g.fill(token);
		}
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		double size = CommonVisualSettings.getSize();
		return new Rectangle2D.Double(-size/2, -size/2, size, size);	}


	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
			return true;
	}

	public boolean isEnabled() {
		return getReferencedRegister().isEnabled();
	}

	public void setEnabled(boolean enabled) {
		getReferencedRegister().setEnabled(enabled);
	}

	public boolean isMarked() {
		return getReferencedRegister().isMarked();
	}

	public void setMarked(boolean marked) {
		getReferencedRegister().setMarked(marked);
	}
}

/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.sdfs;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.PopupMenuBuilder;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.actions.ScriptedAction;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.actions.ActionMenuItem;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.sdfs.tools.RegisterDecoration;

@Hotkey(KeyEvent.VK_R)
@DisplayName ("Register")
@SVGIcon("images/icons/svg/sdfs-register.svg")
public class VisualRegister extends VisualComponent {
	static public class ToggleMarkedAction extends ScriptedAction {
		String text;

		public ToggleMarkedAction(Register register) {
			super();

			if (register.isMarked())
				text = "Unmark";
			else
				text = "Mark";
		}

		public String getScript() {
			throw new RuntimeException("Not implemented");
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
		String text;

		public ToggleEnabledAction(Register register) {
			super();

			if (register.isEnabled())
				text = "Disable";
			else
				text = "Enable";
		}

		public String getScript() {
			throw new RuntimeException("Not implemented");
			//return "r=model.getComponentByID("+regID+");\nr.setEnabled(!r.isEnabled());\nmodel.fireNodePropertyChanged(\"Enabled\", r);";
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

	public VisualRegister(Register register) {
		super(register);
		addPropertyDeclarations();
	}

	public Register getReferencedRegister() {
		return (Register)getReferencedComponent();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration (this, "Enabled", "isEnabled", "setEnabled", boolean.class));
		addPropertyDeclaration(new PropertyDeclaration (this, "Marked", "isMarked", "setMarked", boolean.class));

		addPopupMenuSegment(new PopupMenuBuilder.PopupMenuSegment() {
			public void addItems(JPopupMenu menu,
					ScriptedActionListener actionListener) {
				ActionMenuItem addToken = new ActionMenuItem(new ToggleMarkedAction(getReferencedRegister()));
				addToken.addScriptedActionListener(actionListener);

				ActionMenuItem removeToken = new ActionMenuItem(new ToggleEnabledAction(getReferencedRegister()));
				removeToken.addScriptedActionListener(actionListener);

				menu.add(new JLabel ("Register"));
				menu.addSeparator();
				menu.add(addToken);
				menu.add(removeToken);
			}
		});
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		double xy = -size / 2 + strokeWidth / 2;
		double wh = size - strokeWidth;
		double dx = 0.2 * size;
		double strokeWidth2 = strokeWidth / 2;
		double dy = strokeWidth2 / 2;
		Shape outerRect = new Rectangle2D.Double (xy, xy, wh, wh);;
		Shape innerRect = new Rectangle2D.Double (xy + dx, xy + dy, wh - dx - dx, wh - dy - dy);
		Shape token = new Ellipse2D.Double (-size *0.15 , -size/2  + size / 8, size * 0.3, size * 0.3);

		g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
		g.fill(outerRect);
		g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		g.setStroke(new BasicStroke((float)strokeWidth));
		g.draw(outerRect);

		boolean enabled = isEnabled();
		if (d instanceof RegisterDecoration) {
			enabled =((RegisterDecoration)d).isEnabled();
		}
		if (enabled) {
			g.setColor(Coloriser.colorise(SDFSVisualSettings.getEnabledRegisterColor(), d.getBackground()));
		} else {
			g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
		}
		g.fill(innerRect);
		g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		g.setStroke(new BasicStroke((float)strokeWidth2));
		g.draw(innerRect);

		boolean marked = isMarked();
		if (d instanceof RegisterDecoration) {
			marked = ((RegisterDecoration)d).isMarked();
		}
		if (marked) {
			g.setColor(getForegroundColor());
			g.fill(token);
		}

		drawLabelInLocalSpace(r);
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

package org.workcraft.gui.edit.tools;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JOptionPane;

import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.framework.ComponentFactory;
import org.workcraft.framework.exceptions.ComponentCreationException;
import org.workcraft.framework.exceptions.VisualComponentCreationException;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class ComponentCreationTool extends AbstractTool {
	protected Class<? extends Component> componentClass;
	protected int hotKeyCode;

	public ComponentCreationTool (Class<? extends Component> componentClass) {
		this.componentClass = componentClass;

	}

	public String getIconPath() {
		return null;
	}

	public String getName() {
		DisplayName name = componentClass.getAnnotation(DisplayName.class);
		if (name == null)
			return "Create " + componentClass.getSimpleName().toLowerCase();
		else
			return "Create " + name.value();
	}

	public void mousePressed(GraphEditorMouseEvent e) {
		try {
			Component comp = ComponentFactory.createComponent(componentClass.getName());

			initComponent(comp);

			e.getEditor().getModel().getMathModel().addComponent(comp);

			VisualNode vComp = ComponentFactory.createVisualComponent(comp);

			if(vComp instanceof VisualTransformableNode)
			{
				VisualTransformableNode transformable = (VisualTransformableNode) vComp;

				Point2D pos = e.getPosition();
				e.getEditor().snap(pos);
				transformable.setX(pos.getX());
				transformable.setY(pos.getY());
			}

			e.getEditor().getModel().getCurrentLevel().add(vComp);
			e.getEditor().getModel().addNode(vComp);

		} catch (ComponentCreationException e1) {
			JOptionPane.showMessageDialog(null, "Cannot create component:\n"+e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} catch (VisualComponentCreationException e1) {
			JOptionPane.showMessageDialog(null, "Cannot create visual component:\n"+e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);		}
	}

	protected void initComponent(Component comp) {
	}

	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		g.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
		String message = "Click to create a " + componentClass.getSimpleName();
		Rectangle2D r = g.getFont().getStringBounds(message, g.getFontRenderContext());
		g.setColor(Color.BLUE);
		g.drawString (message, editor.getWidth()/2 - (int)r.getWidth()/2, editor.getHeight() - 20);

	}

	@Override
	public int getHotKeyCode() {
		return ComponentFactory.getHotKeyCodeForClass(componentClass);
	}
}

package org.workcraft.plugins.stg;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.petri.VisualPlace;

public class STGSelectionTool extends SelectionTool
{
	@Override
	public void mouseClicked(GraphEditorMouseEvent e)
	{
		super.mouseClicked(e);
		if (e.getClickCount() > 1)
		{
			Collection<Node> selection = e.getModel().getSelection();
			if(selection.size() == 1)
			{
				Node selectedNode = selection.iterator().next();

				if(selectedNode instanceof VisualPlace)
				{
					VisualPlace place = (VisualPlace) selectedNode;
					if (place.getTokens()==1)
						place.setTokens(0);
					else if (place.getTokens()==0)
						place.setTokens(1);
				} else if (selectedNode instanceof VisualSignalTransition) {
					final VisualSignalTransition transition = (VisualSignalTransition) selectedNode;
					final STG model = (STG)e.getModel().getMathModel();

					Rectangle2D bb = transition.getBoundingBox();

					Viewport viewport = e.getEditor().getViewport();
					Point pt = viewport.userToScreen(new Point2D.Double(bb.getMinX(), bb.getMinY()));

					Rectangle r = viewport.userToScreen(bb);


					final JTextField text = new JTextField();
					text.setText(e.getModel().getMathModel().getNodeReference(transition.getReferencedTransition()));
					text.setFont(text.getFont().deriveFont( Math.max(12.0f, (float)r.getHeight()*0.7f)));
					text.selectAll();

					text.setBounds(r.x, r.y, Math.max(r.width, 60), Math.max(r.height, 18));

					e.getEditor().getOverlay().add(text);
					text.requestFocusInWindow();

					text.addKeyListener( new KeyListener() {

						@Override
						public void keyPressed(KeyEvent arg0) {
							if (arg0.getKeyCode() == KeyEvent.VK_ENTER)
								text.getParent().remove(text);
						}

						@Override
						public void keyReleased(KeyEvent arg0) {
						}

						@Override
						public void keyTyped(KeyEvent arg0) {
						}

					});

					text.addFocusListener(new FocusListener() {
						@Override
						public void focusGained(FocusEvent arg0) {
						}

						@Override
						public void focusLost(FocusEvent arg0) {
							if (text.getParent() != null)
								text.getParent().remove(text);

							try {
								model.setName(transition.getReferencedTransition(), text.getText());
							} catch (ArgumentException e) {
								JOptionPane.showMessageDialog(null, e.getMessage());
							}
						}
					});
				}
			}

		}
	}
}

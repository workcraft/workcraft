package org.workcraft.plugins.stg;

import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.petri.VisualPlace;

public class STGSelectionTool extends SelectionTool
{
	private boolean cancelEdit = false;

	private void editInPlace (final GraphEditor editor, final VisualComponent t, String initialText) {
		final Viewport viewport = editor.getViewport();
		final STG model = (STG)editor.getModel().getMathModel();

		Rectangle2D bb = t.getBoundingBox();
		Rectangle r = viewport.userToScreen(bb);


		final JTextField text = new JTextField();

		if (initialText != null)
			text.setText(initialText);
		else
			text.setText(editor.getModel().getMathModel().getNodeReference(t.getReferencedComponent()));

		text.setFont(text.getFont().deriveFont( Math.max(12.0f, (float)r.getHeight()*0.7f)));
		text.selectAll();

		text.setBounds(r.x, r.y, Math.max(r.width, 60), Math.max(r.height, 18));

		editor.getOverlay().add(text);
		text.requestFocusInWindow();

		text.addKeyListener( new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					cancelEdit = false;
					text.getParent().remove(text);
				}
				else if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
					cancelEdit = true;
					text.getParent().remove(text);
				}
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

				final String newName = text.getText();

				if (!cancelEdit)
					try {
						model.setName(t.getReferencedComponent(), newName);
					} catch (ArgumentException e) {
						JOptionPane.showMessageDialog(null, e.getMessage());
						editInPlace(editor, t, newName);
					}

				editor.repaint();
			}
		});
	}

	@Override
	public void mouseClicked(GraphEditorMouseEvent e)
	{
		super.mouseClicked(e);

		VisualModel model = e.getEditor().getModel();

		if(e.getButton()==MouseEvent.BUTTON1 && e.getClickCount() > 1) {
			VisualNode node = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);
			if (node != null)
			{
				if(node instanceof VisualPlace)
				{
					VisualPlace place = (VisualPlace) node;
					if (place.getTokens()==1)
						place.setTokens(0);
					else if (place.getTokens()==0)
						place.setTokens(1);
				} else if (node instanceof VisualImplicitPlaceArc) {
					STGPlace place = ((VisualImplicitPlaceArc) node).getImplicitPlace();
					if (place.getTokens()==1)
						place.setTokens(0);
					else if (place.getTokens()==0)
						place.setTokens(1);
				} else if (node instanceof VisualSignalTransition || node instanceof VisualDummyTransition) {
					editInPlace(e.getEditor(), (VisualComponent)node, null);
				}

			}

		} else if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
			VisualNode node = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);
			JPopupMenu popup = createPopupMenu(node);
			if (popup!=null)
				popup.show(e.getSystemEvent().getComponent(), e.getSystemEvent().getX(), e.getSystemEvent().getY());
		}
	}

	private JPopupMenu createPopupMenu(VisualNode node) {
		JPopupMenu popup = new JPopupMenu();

		if (node instanceof VisualPlace) {
			popup.setFocusable(false);
			popup.add(new JLabel("Place"));
			popup.addSeparator();
			popup.add(new JMenuItem("Add token"));
			popup.add(new JMenuItem("Remove token"));
			return popup;
		}

		return null;
	}
}

package org.workcraft.plugins.stg.tools;

import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class STGSelectionTool extends SelectionTool
{
	private boolean cancelInPlaceEdit = false;

	@Override
	public void mouseClicked(GraphEditorMouseEvent e)
	{
		boolean processed = false;
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
			VisualModel model = e.getEditor().getModel();
			Node node = HitMan.hitTestForSelection(e.getPosition(), model);
			if (node != null)
			{
				if (node instanceof VisualPlace)
				{
					Place place = ((VisualPlace) node).getPlace();
					toggleToken(place, e);
					processed = true;
				} else if (node instanceof VisualImplicitPlaceArc) {
					Place place = ((VisualImplicitPlaceArc) node).getImplicitPlace();
					toggleToken(place, e);
					processed = true;
				} else if (node instanceof VisualSignalTransition || node instanceof VisualDummyTransition) {
					VisualComponent component = (VisualComponent)node;
					editNameInPlace(e.getEditor(), component, model.getNodeReference(component.getReferencedComponent()));
					processed = true;
				}
			}
		}
		if (!processed) {
			super.mouseClicked(e);
		}
	}

	private void toggleToken(Place place, GraphEditorMouseEvent e) {
		if (place.getTokens() <= 1) {
			e.getEditor().getWorkspaceEntry().saveMemento();

			if (place.getTokens() == 1)
				place.setTokens(0);
			else
				place.setTokens(1);
		}
	}

	private void editNameInPlace (final GraphEditor editor, final VisualComponent component, String initialText) {
		final STG model = (STG)editor.getModel().getMathModel();
		final JTextField text = new JTextField(initialText);
		Rectangle bb = editor.getViewport().userToScreen(component.getBoundingBox());
		text.setFont(text.getFont().deriveFont( Math.max(10.0f, (float)bb.getHeight()*0.7f)));
		text.selectAll();
		text.setBounds(bb.x, bb.y, Math.max(bb.width, 60), Math.max(bb.height, 18));
		editor.getOverlay().add(text);
		text.requestFocusInWindow();

		text.addKeyListener( new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					cancelInPlaceEdit = false;
					text.getParent().remove(text);
				}
				else if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
					cancelInPlaceEdit = true;
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
				editor.getWorkspaceEntry().setCanUndoAndRedo(false);
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				if (text.getParent() != null)
					text.getParent().remove(text);
				final String newName = text.getText();
				if (!cancelInPlaceEdit) {
					editor.getWorkspaceEntry().captureMemento();
					try {
						model.setName(component.getReferencedComponent(), newName);
						editor.getWorkspaceEntry().saveMemento();
					} catch (ArgumentException e) {
						JOptionPane.showMessageDialog(null, e.getMessage());
						editNameInPlace(editor, component, newName);
						editor.getWorkspaceEntry().cancelMemento();
					}
				}
				editor.getWorkspaceEntry().setCanUndoAndRedo(true);
				editor.repaint();
			}
		});
	}

}

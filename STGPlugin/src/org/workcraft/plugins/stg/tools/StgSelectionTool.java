package org.workcraft.plugins.stg.tools;

import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualNamedTransition;

public class StgSelectionTool extends SelectionTool {
    private boolean cancelInPlaceEdit = false;

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            GraphEditor editor = e.getEditor();
            VisualModel model = editor.getModel();
            Node node = HitMan.hitTestForSelection(e.getPosition(), model);
            if (node != null) {
                if (node instanceof VisualPlace) {
                    Place place = ((VisualPlace) node).getReferencedPlace();
                    toggleToken(place, editor);
                    processed = true;
                } else if (node instanceof VisualImplicitPlaceArc) {
                    if (e.getKeyModifiers() == MouseEvent.CTRL_DOWN_MASK) {
                        Place place = ((VisualImplicitPlaceArc) node).getImplicitPlace();
                        toggleToken(place, editor);
                        processed = true;
                    }
                } else if (node instanceof VisualNamedTransition) {
                    VisualNamedTransition transition = (VisualNamedTransition) node;
                    editNameInPlace(editor, transition, transition.getName());
                    processed = true;
                }
            }
        }
        if (!processed) {
            super.mouseClicked(e);
        }
    }

    private void toggleToken(Place place, GraphEditor editor) {
        if (place.getTokens() <= 1) {
            editor.getWorkspaceEntry().saveMemento();

            if (place.getTokens() == 1) {
                place.setTokens(0);
            } else {
                place.setTokens(1);
            }
        }
    }

    private void editNameInPlace(final GraphEditor editor, final VisualNamedTransition transition, String initialText) {
        final JTextField text = new JTextField(initialText);
        AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(transition);
        Rectangle2D bbRoot = TransformHelper.transform(transition, localToRootTransform).getBoundingBox();
        Rectangle bbScreen = editor.getViewport().userToScreen(BoundingBoxHelper.expand(bbRoot, 1.0, 0.5));
        float fontSize = VisualNamedTransition.font.getSize2D() * (float) editor.getViewport().getTransform().getScaleY();
        text.setFont(VisualNamedTransition.font.deriveFont(fontSize));
        text.setBounds(bbScreen.x, bbScreen.y, bbScreen.width, bbScreen.height);
        text.setHorizontalAlignment(JTextField.CENTER);
        text.selectAll();
        editor.getOverlay().add(text);
        text.requestFocusInWindow();

        text.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    editor.requestFocus();
                } else if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelInPlaceEdit = true;
                    editor.requestFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
            }
        });

        final STG model = (STG) editor.getModel().getMathModel();
        text.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent arg0) {
                editor.getWorkspaceEntry().setCanModify(false);
                cancelInPlaceEdit = false;
            }

            @Override
            public void focusLost(FocusEvent arg0) {
                final String newName = text.getText();
                text.getParent().remove(text);
                if (!cancelInPlaceEdit) {
                    try {
                        editor.getWorkspaceEntry().saveMemento();
                        model.setName(transition.getReferencedComponent(), newName, true);
                    } catch (ArgumentException e) {
                        JOptionPane.showMessageDialog(null, e.getMessage());
                        editNameInPlace(editor, transition, newName);
                    }
                }
                editor.getWorkspaceEntry().setCanModify(true);
                editor.repaint();
            }
        });
    }

}

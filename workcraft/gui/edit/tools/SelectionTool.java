package org.workcraft.gui.edit.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.xml.parsers.ParserConfigurationException;

import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.visual.Colorisable;
import org.workcraft.dom.visual.HierarchyHelper;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnectionAnchorPoint;
import org.workcraft.gui.edit.graph.GraphEditorPanel;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.propertyeditor.PropertyEditable;

public class SelectionTool extends AbstractTool {
	private static final int DRAG_NONE = 0;
	private static final int DRAG_MOVE = 1;
	private static final int DRAG_SELECT = 2;

	private static final int SELECTION_ADD = 0;
	private static final int SELECTION_REMOVE = 1;

	protected Color selectionBorderColor = new Color(200, 200, 200);
	protected Color selectionFillColor = new Color(99, 130, 191, 32);
	protected Color selectionColor = new Color(99, 130, 191).brighter();;
	protected Color grayOutColor = Color.LIGHT_GRAY;

	private int drag = DRAG_NONE;
	private Point2D prevPosition;
	private Point2D startPosition;
	private Point2D snapOffset;
	private LinkedList<HierarchyNode> savedSelection = new LinkedList<HierarchyNode>();
	private int selectionMode;

	private GraphEditor currentEditor = null;

	@Override
	public void activated(GraphEditor editor) {
		currentEditor = editor;
	}

	@Override
	public void deactivated(GraphEditor editor) {
		currentEditor = null;
	}

	protected void clearSelection(VisualModel model) {
		for (VisualNode so : model.getSelection()) {
			so.clearColorisation();
		}
		model.selectNone();
	}

	protected void addToSelection(VisualModel model, HierarchyNode so) {
		model.addToSelection(so);
		if(so instanceof Colorisable)
			((Colorisable)so).setColorisation(selectionColor);
	}

	protected void addToSelection(VisualModel model, Collection<HierarchyNode> s) {
		for (HierarchyNode n : s) {
			addToSelection(model, n);
		}
	}

	protected void removeFromSelection(VisualModel model, HierarchyNode so) {
		model.removeFromSelection(so);
		if(so instanceof Colorisable)
			((Colorisable)so).setColorisation(selectionColor);
	}

	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
		//System.out.println ("mouseClicking <_<");

		VisualModel model = e.getEditor().getModel();

		if(e.getButton()==MouseEvent.BUTTON1) {
			if(drag!=DRAG_NONE)
				cancelDrag(e);

			HierarchyNode node = model.hitNode(e.getPosition());

			if ((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.ALT_DOWN_MASK))==0) {
				if (node != null) {
					if (model.isObjectSelected(node) && node instanceof VisualConnection && e.getClickCount() == 2) {
						/*
						VisualConnectionAnchorPoint ap = ((VisualConnection)node).addAnchorPoint(e.getPosition());

						clearSelection(model);
						addToSelection(model, ap.getParentConnection());
						ap.getParentConnection().getParent().add(ap);
						addToSelection(model, ap);
						*/

					} else {
						clearSelection(model);
						addToSelection(model, node);
//						if (node instanceof VisualConnectionAnchorPoint) {
//							addToSelection(model, ((VisualConnectionAnchorPoint)node).getParentConnection());
//						}
					}
				} else
					clearSelection(model);
			} else {
				if((e.getModifiers()&MouseEvent.ALT_DOWN_MASK)!=0)
					removeFromSelection(model, node);
				else {
					addToSelection(model, node);
				}
			}

			model.fireSelectionChanged();
		}
		else if(e.getButton()==MouseEvent.BUTTON3) {
			HierarchyNode so = model.hitNode(e.getPosition());

			if (so!=null) {
				Point2D screenPoint = e.getEditor().getViewport().userToScreen(e.getPosition());

				if(so instanceof VisualNode)
				{
					JPopupMenu popup = ((VisualNode)so).createPopupMenu( ((GraphEditorPanel)currentEditor).getMainWindow().getDefaultActionListener() );

					if (popup.getComponentCount() != 0) {
						popup.setFocusable(false);
						popup.show((GraphEditorPanel)e.getEditor(), (int)screenPoint.getX(), (int) screenPoint.getY());
					}
				}
			}
		}

		//System.out.println ("mouseClicked >_>");
	}

	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(drag==DRAG_MOVE) {
			Point2D pos = new Point2D.Double(e.getX()+snapOffset.getX(), e.getY()+snapOffset.getY());
			e.getEditor().snap(pos);
			//
			if (model.getSelection().length==1) {
				/*for (VisualNode vn : model.getSelection()) {
					if (vn instanceof VisualConnection) {
						VisualConnection vc = (VisualConnection)vn;
						if (vc.getConnectionType()!=VisualConnection.ConnectionType.POLYLINE||
								vc.getAnchorPointCount()==0) {
							vc.setConnectionType(VisualConnection.ConnectionType.BEZIER);
							vc.showAnchorPoints();
							for (VisualConnectionAnchorPoint ap: vc.getAnchorPointComponents()) {
								addToSelection(model, ap);
							}
						}
					}
				}*/
			}

			offsetSelection(e, pos.getX()-prevPosition.getX(), pos.getY()-prevPosition.getY());
			model.fireLayoutChanged();
			prevPosition = pos;
		}
		else if(drag==DRAG_SELECT) {
			LinkedList<Touchable> hit = model.hitObjects(startPosition, e.getPosition());

			clearSelection(model);
			for (HierarchyNode so: savedSelection)
				addToSelection(model, so);

			for(Touchable so : hit)
				if(selectionMode==SELECTION_ADD)
					addToSelection(model, so);
				else if(selectionMode==SELECTION_REMOVE)
					removeFromSelection(model, so);
			prevPosition = e.getPosition();
			e.getEditor().repaint();
		}
		else
			prevPosition = e.getPosition();
	}


	@Override
	public void mousePressed(GraphEditorMouseEvent e) {

		//System.out.println ("mousePressing ^_^");
		VisualModel model = e.getEditor().getModel();

		if(e.getButton()==MouseEvent.BUTTON1) {
			startPosition = e.getPosition();
			prevPosition = e.getPosition();
			HierarchyNode so = model.hitNode(e.getPosition());
			if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.ALT_DOWN_MASK))==0 && so!=null) {
				if(!model.isObjectSelected(so)) {
					clearSelection(model);

					addToSelection(model, so);
					if (so instanceof VisualConnectionAnchorPoint) {
						addToSelection(model, ((VisualConnectionAnchorPoint)so).getParentConnection());
					}
					model.fireSelectionChanged();
				}
				drag = DRAG_MOVE;
				if(so instanceof VisualTransformableNode) {
					VisualTransformableNode node = (VisualTransformableNode) so;
					snapOffset = new Point2D.Double(node.getX()-e.getX(), node.getY()-e.getY());
					prevPosition = new Point2D.Double(node.getX(), node.getY());
				}
				else
					snapOffset = new Point2D.Double(0, 0);
			}
			else {
				savedSelection.clear();
				if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.ALT_DOWN_MASK))==0 && so==null)
					clearSelection(model);
				if((e.getModifiers()&MouseEvent.ALT_DOWN_MASK)!=0)
					selectionMode = SELECTION_REMOVE;
				else
					selectionMode = SELECTION_ADD;
				savedSelection.addAll(model.selection());
				drag = DRAG_SELECT;
			}
		}
		else if(e.getButton()==MouseEvent.BUTTON3)
			if(drag!=DRAG_NONE)
				cancelDrag(e);

		//System.out.println ("mousePressed d^_^b");
	}

	@Override
	public void mouseReleased(GraphEditorMouseEvent e) {
		//System.out.println ("mouseReleasing T_T");
		VisualModel model = e.getModel();

		if(e.getButton()==MouseEvent.BUTTON1) {
			if (drag == DRAG_SELECT)
				model.fireSelectionChanged();

			drag = DRAG_NONE;
		}
		//System.out.println ("mouseReleased X_X");
	}

	private void grayOutNotActive(VisualModel model)
	{
		HierarchyNode root = model.getRoot();
		if (root instanceof Colorisable)
			((Colorisable)root).setColorisation(grayOutColor);

		model.getCurrentLevel().clearColorisation();
		for(VisualNode node : model.getSelection())
			node.setColorisation(selectionColor);
	}

	@Override
	public void keyPressed(GraphEditorKeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			e.getModel().deleteSelection();
			e.getEditor().repaint();
		}

		if (!e.isCtrlDown())
		{
			if (!e.isShiftDown()) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_PAGE_UP:
					currentLevelUp(e);
					break;
				case KeyEvent.VK_PAGE_DOWN:
					currentLevelDown(e);
					break;
				case KeyEvent.VK_OPEN_BRACKET:
					e.getModel().rotateSelection(-Math.PI/2);
					break;
				case KeyEvent.VK_CLOSE_BRACKET:
					e.getModel().rotateSelection(Math.PI/2);
					break;
				case KeyEvent.VK_LEFT:
					e.getModel().translateSelection(-1,0);
					break;
				case KeyEvent.VK_RIGHT:
					e.getModel().translateSelection(1,0);
					break;
				case KeyEvent.VK_UP:
					e.getModel().translateSelection(0,-1);
					break;
				case KeyEvent.VK_DOWN:
					e.getModel().translateSelection(0,1);
					break;
				}
			} else { // Shift is pressed

				switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
					e.getModel().scaleSelection(-1,1);
					break;
				case KeyEvent.VK_UP:
					e.getModel().scaleSelection(1,-1);
					break;
				}
			}
		}

		if (e.isCtrlDown()) {
			switch(e.getKeyCode()){
			case KeyEvent.VK_G:
				e.getModel().groupSelection();
				e.getEditor().repaint();
				break;
			case KeyEvent.VK_U:
				e.getModel().ungroupSelection();
				e.getEditor().repaint();
				break;
			case KeyEvent.VK_C:
				try {
						e.getModel().copy(Toolkit.getDefaultToolkit().getSystemClipboard(), null);
					} catch (ParserConfigurationException e2) {
						JOptionPane.showMessageDialog(null, "Copy failed. Please refer to the Problems window for details.", "Error", JOptionPane.ERROR_MESSAGE);
						e2.printStackTrace();
					}
				break;
			case KeyEvent.VK_X:
				try {
						e.getModel().cut(Toolkit.getDefaultToolkit().getSystemClipboard(), null);
					} catch (ParserConfigurationException e2) {
						JOptionPane.showMessageDialog(null, "Copy failed. Please refer to the Problems window for details.", "Error", JOptionPane.ERROR_MESSAGE);
						e2.printStackTrace();
					}
				break;
			case KeyEvent.VK_V:
				clearSelection(e.getModel());
					//addToSelection(e.getModel(), e.getModel().paste(Toolkit.getDefaultToolkit().getSystemClipboard(), prevPosition));
					e.getModel().fireSelectionChanged();
					e.getEditor().repaint();
			}
		}
	}

	private void currentLevelDown(GraphEditorKeyEvent e) {
		VisualNode[] selection = e.getModel().getSelection();
		if(selection.length == 1)
		{
			VisualNode selectedNode = selection[0];
			if(selectedNode instanceof VisualGroup)
				e.getModel().setCurrentLevel((VisualGroup)selectedNode);
		}
		grayOutNotActive(e.getModel());
	}

	private void currentLevelUp(GraphEditorKeyEvent e) {
		VisualGroup level = e.getModel().getCurrentLevel();
		VisualGroup parent = HierarchyHelper.getNearestAncestor(level.getParent(), VisualGroup.class);
		if(parent!=null)
		{
			e.getModel().setCurrentLevel(parent);
			e.getModel().addToSelection(level);
			grayOutNotActive(e.getModel());
		}
	}

	private void offsetSelection(GraphEditorMouseEvent e, double dx, double dy) {
		VisualModel model = e.getEditor().getModel().getVisualModel();

		for(HierarchyNode node : model.selection())
			if(node instanceof Movable) {
				Movable mv = (Movable) node;
				MovableHelper.translate(mv, dx, dy);

				if(node instanceof PropertyEditable)
					((PropertyEditable)node).firePropertyChanged("transform");
			}

		model.fireLayoutChanged();
	}

	private void cancelDrag(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(drag==DRAG_MOVE) {
			offsetSelection(e, startPosition.getX()-e.getX(), startPosition.getY()-e.getY());
		}
		else if(drag == DRAG_SELECT) {
			clearSelection(model);
			for (HierarchyNode so: savedSelection)
				addToSelection(model, so);
			savedSelection.clear();
			model.fireSelectionChanged();
		}
		drag = DRAG_NONE;

	}

	private Rectangle2D selectionRect(Point2D currentPosition) {
		return new Rectangle2D.Double(
				Math.min(startPosition.getX(), currentPosition.getX()),
				Math.min(startPosition.getY(), currentPosition.getY()),
				Math.abs(startPosition.getX()-currentPosition.getX()),
				Math.abs(startPosition.getY()-currentPosition.getY())
		);
	}


	public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
		//VisualModel model = editor.getModel();
		g.setStroke(new BasicStroke((float) editor.getViewport().pixelSizeInUserSpace().getX()));

		Rectangle2D.Double rect = null;
		/*for(VisualNode vo : model.getSelection()) {

			if(vo==null)
				continue;


			Rectangle2D bb = vo.getBoundingBoxInUserSpace();

			if (bb == null) {
				System.err.println (vo.getClass().getName());
				return;
			}

			if(rect==null) {
				rect = new Rectangle2D.Double();
				rect.setRect(bb);
			}
			else
				rect.add(bb);
		}*/

		if(rect!=null) {
			g.setColor(new Color(255, 0, 0, 128));
			g.draw(rect);
		}

		if(drag==DRAG_SELECT) {
			Rectangle2D bb = selectionRect(prevPosition);
			g.setColor(selectionFillColor);
			g.fill(bb);
			g.setColor(selectionBorderColor);
			g.draw(bb);

		}
	}

	public String getIconPath() {
		return "org" + File.separator + "workcraft" + File.separator + "gui" + File.separator + "icons" + File.separator + "select.png";
	}

	public String getName() {
		return "Select";
	}

	public int getHotKeyCode() {
		return KeyEvent.VK_S;
	}
}
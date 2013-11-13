package org.workcraft.plugins.dfs.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.NumberFormatter;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.dfs.VisualDelayComponent;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.util.GUI;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.graph.cycle.ElementaryCyclesSearch;

public class CycleAnaliserTool extends AbstractTool {
	final private int COLUMN_THROUGHPUT = 0;
	final private int COLUMN_TOKEN = 1;
	final private int COLUMN_DELAY = 2;
	final private int COLUMN_CYCLE = 3;

	private VisualDfs dfs;
	private GraphEditor editor;
	private ArrayList<Cycle> cycles;
	private double minDelay;
	private double maxDelay;
	protected Cycle selectedCycle = null;
	private int cycleCount = 10;

	protected JPanel interfacePanel;
	protected JPanel controlPanel;
	protected JScrollPane infoPanel;
	protected JPanel statusPanel;
	protected JTable cycleTable;

	public CycleAnaliserTool() {
		super();
		controlPanel = new JPanel();
		cycleTable = new JTable(new CycleTableModel());
		cycleTable.addMouseListener(new CycleTableMouseListenerImplementation());
		cycleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cycleTable.getColumnModel().getColumn(COLUMN_THROUGHPUT).setPreferredWidth(50);
		cycleTable.getColumnModel().getColumn(COLUMN_TOKEN).setPreferredWidth(30);
		cycleTable.getColumnModel().getColumn(COLUMN_DELAY).setPreferredWidth(30);
		cycleTable.getColumnModel().getColumn(COLUMN_CYCLE).setPreferredWidth(300);
		cycleTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		infoPanel = new JScrollPane(cycleTable);
		statusPanel = new JPanel();

		interfacePanel = new JPanel();
		interfacePanel.setLayout(new BorderLayout());
		interfacePanel.add(controlPanel, BorderLayout.PAGE_START);
		interfacePanel.add(infoPanel, BorderLayout.CENTER);
		interfacePanel.add(statusPanel, BorderLayout.PAGE_END);

		NumberFormat format = NumberFormat.getIntegerInstance();
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setAllowsInvalid(false);
		formatter.setMinimum(1);
		formatter.setMaximum(1000);
		final JFormattedTextField cycleCountText = new JFormattedTextField(formatter);
		cycleCountText.setPreferredSize(new Dimension(100, 24));
		cycleCountText.setText(new Integer(cycleCount).toString());
		cycleCountText.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					cycleCount = Integer.parseInt(cycleCountText.getText());
					cycleTable.tableChanged(null);
				}
				else if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
					cycleCountText.setText(new Integer(cycleCount).toString());
				}
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
			}
			@Override
			public void keyTyped(KeyEvent arg0) {
			}
		});

		cycleCountText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
				cycleCountText.setText(new Integer(cycleCount).toString());
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				cycleCount = Integer.parseInt(cycleCountText.getText());
				cycleTable.tableChanged(null);
			}
		});

		JLabel cycleCountLabel = new JLabel();
		cycleCountLabel.setText("Cycle count:");
		cycleCountLabel.setLabelFor(cycleCountText);

		controlPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		controlPanel.add(cycleCountLabel);
		controlPanel.add(cycleCountText);
	}

	@Override
	public void activated(GraphEditor editor) {
		editor.getWorkspaceEntry().setCanModify(false);
		this.dfs = (VisualDfs)editor.getModel();
		this.editor = editor;
		cycleTable.clearSelection();
		selectedCycle = null;
		this.cycles = findCycles();
	}

	@Override
	public void deactivated(GraphEditor editor) {
		editor.getWorkspaceEntry().setCanModify(true);
	}

	@Override
	public String getLabel() {
		return "Cycle analiser";
	}

	@Override
	public int getHotKeyCode() {
		return KeyEvent.VK_A;
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/tool-cycle_analysis.svg");
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}

	@Override
	public Decorator getDecorator() {
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				if (node instanceof VisualDelayComponent) {
					double delay = ((VisualDelayComponent)node).getReferencedDelayComponent().getDelay();
					if (selectedCycle == null) {
						double range = (maxDelay - minDelay);
						double offset = (delay - minDelay);
						final Color fgColor = ((range > 0 &&  offset > 0.8 * range) ? Color.RED : null);
						return new Decoration(){
							@Override
							public Color getColorisation() {
								return fgColor;
							}
							@Override
							public Color getBackground() {
								return null;
							}
						};
					} else if (selectedCycle.components.contains(node)) {
						double range = (selectedCycle.maxDelay - selectedCycle.minDelay);
						double offset = (delay - selectedCycle.minDelay);
						int bgIintencity = 150;
						if (range > 0) {
							bgIintencity = (int)(bgIintencity + (255 - bgIintencity) * offset / range);
						}
						final Color fgColor = ((range > 0 &&  offset > 0.8 * range) ? Color.RED : null);
						final Color bgColor = new Color(bgIintencity, 0, 0);
						return new Decoration(){
							@Override
							public Color getColorisation() {
								return fgColor;
							}
							@Override
							public Color getBackground() {
								return bgColor;
							}
						};
					}
				}
				return null;
			}
		};
	}

	public ArrayList<Cycle> findCycles() {
		ArrayList<Cycle> result = new ArrayList<Cycle>();
		// update global min and max delay values
		Collection<VisualDelayComponent> allComponents = Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualDelayComponent.class);
		boolean first = true;
		for (VisualDelayComponent c: allComponents) {
			double delay = c.getReferencedDelayComponent().getDelay();
			if (first || minDelay > delay) {
				minDelay = delay;
			}
			if (first || maxDelay < delay) {
				maxDelay = delay;
			}
			first = false;
		}
		// prepare temporary node array and adjacency matrix
	    int size = allComponents.size();
	    VisualComponent tmpComponents[] = allComponents.toArray(new VisualComponent[size]);
		boolean adjMatrix[][] = new boolean[size][size];
		for (int i = 0; i < size; i++) {
			HashSet<Node> preset = new HashSet<Node>(dfs.getPreset(tmpComponents[i]));
			HashSet<Node> postset = new HashSet<Node>(dfs.getPostset(tmpComponents[i]));
			for (int j = i+1; j < size; j++) {
				adjMatrix[i][j] = postset.contains(tmpComponents[j]);
				adjMatrix[j][i] = preset.contains(tmpComponents[j]);
			}
		}
		// calculate simple cycles and process the results
		ElementaryCyclesSearch ecs = new ElementaryCyclesSearch(adjMatrix, tmpComponents);
		List tmpCycles = ecs.getElementaryCycles();
		for (int i = 0; i < tmpCycles.size(); i++) {
			List tmpCycle = (List) tmpCycles.get(i);
			String toString = "";
			LinkedHashSet<VisualDelayComponent> components = new LinkedHashSet<VisualDelayComponent>();
			for (int j = 0; j < tmpCycle.size(); j++) {
				VisualDelayComponent component = (VisualDelayComponent)tmpCycle.get(j);
				if (toString.length() > 0) toString += "→";
				toString += dfs.getMathModel().getNodeReference(component.getReferencedComponent());
				components.add(component);
			}
			Cycle cycle = new Cycle(components, toString);
			result.add(cycle);
		}
		Collections.sort(result);
		return result;
	}

	@SuppressWarnings("serial")
	private final class CycleTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int column) {
			String result;
			switch (column) {
			case COLUMN_CYCLE:
				result = "Cycle";
				break;
			case COLUMN_TOKEN:
				result = "Tokens";
				break;
			case COLUMN_DELAY:
				result = "Delay";
				break;
			case COLUMN_THROUGHPUT:
				result = "Throughput";
				break;
			default:
				result = "";
				break;
			}
			return result;
		}

		@Override
		public int getRowCount() {
			return Math.min(cycleCount, cycles.size());
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object result = null;
			Cycle cycle = cycles.get(row);
			if (cycle != null) {
				switch (col) {
				case COLUMN_CYCLE:
					result = cycle.toString();
					break;
				case COLUMN_TOKEN:
					result = cycle.tokenCount;
					break;
				case COLUMN_DELAY:
					result = new DecimalFormat("#.###").format(cycle.totalDelay);
					break;
				case COLUMN_THROUGHPUT:
					if (cycle.totalDelay == 0) {
						result = "∞";
					} else {
						result = new DecimalFormat("#.###").format(cycle.throughput);
					}
					break;
				default:
					result = null;
					break;
				}
			}
			return result;
		}
	}

	private final class CycleTableMouseListenerImplementation implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
			Cycle curCycle = cycles.get(cycleTable.getSelectedRow());
			if (selectedCycle != curCycle) {
				selectedCycle = curCycle;
			} else {
				selectedCycle = null;
				cycleTable.clearSelection();
			}
			editor.repaint();
			editor.requestFocus();
		}
		@Override
		public void mouseEntered(MouseEvent arg0) {
		}
		@Override
		public void mouseExited(MouseEvent arg0) {
		}
		@Override
		public void mousePressed(MouseEvent arg0) {
		}
		@Override
		public void mouseReleased(MouseEvent arg0) {
		}
	}

}

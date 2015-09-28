package org.workcraft.plugins.stg.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.FontHelper;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;
import org.workcraft.plugins.stg.VisualNamedTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.util.GUI;

public class EncodingConflictAnalyserTool extends AbstractTool {

	final private int COLUMN_COLOR = 0;
	final private int COLUMN_CORE = 1;

	private VisualSTG stg;
	private ArrayList<Core> cores;
	private Core selectedCore = null;
	private Color[] heightmapColors;
	private HashMap<String, Integer> heightmap;

	protected JPanel interfacePanel;
	protected JPanel controlPanel;
	protected JScrollPane infoPanel;
	protected JPanel statusPanel;
	private JTable coreTable;
	private JTable heightmapTable;

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		controlPanel = new JPanel();

		coreTable = new JTable(new CoreTableModel());
		TableColumnModel columnModel = coreTable.getColumnModel();
		columnModel.getColumn(COLUMN_COLOR).setPreferredWidth(100);
		columnModel.getColumn(COLUMN_CORE).setPreferredWidth(400);
		coreTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		coreTable.setRowHeight(FontHelper.getFontSizeInPixels(coreTable.getFont()));
		coreTable.setDefaultRenderer(Object.class, new CoreTableCellRendererImplementation());
		coreTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		coreTable.setAutoCreateColumnsFromModel(false);
		coreTable.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				int selectedRow = coreTable.getSelectedRow();
				if ((cores != null) && (selectedRow >= 0) && (selectedRow < cores.size())) {
					Core curCore = cores.get(selectedRow);
					if (selectedCore != curCore) {
						selectedCore = curCore;
					} else {
						selectedCore = null;
						coreTable.clearSelection();
					}
					editor.repaint();
					editor.requestFocus();
				}
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
		});
		infoPanel = new JScrollPane();
		infoPanel.setViewportView(coreTable);
		//infoPanel.setMinimumSize(new Dimension(1, 50));

		heightmapTable = new JTable(new HeightmapTableModel());
		heightmapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		heightmapTable.setRowHeight(FontHelper.getFontSizeInPixels(coreTable.getFont()));
		heightmapTable.setDefaultRenderer(Object.class, new HeightmapTableCellRendererImplementation());
		heightmapTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		heightmapTable.setAutoCreateColumnsFromModel(false);
		heightmapTable.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
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
		});
		statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		statusPanel.add(heightmapTable, BorderLayout.CENTER);
		//statusPanel.setMinimumSize(new Dimension(1, 50));

		interfacePanel = new JPanel();
		interfacePanel.setLayout(new BorderLayout());
		interfacePanel.add(controlPanel, BorderLayout.PAGE_START);
		interfacePanel.add(infoPanel, BorderLayout.CENTER);
		interfacePanel.add(statusPanel, BorderLayout.PAGE_END);
		interfacePanel.setPreferredSize(new Dimension(0, 0));
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}

	@Override
	public void activated(final GraphEditor editor) {
		stg = (VisualSTG)editor.getModel();
		coreTable.clearSelection();
		selectedCore = null;
		super.activated(editor);
		editor.getWorkspaceEntry().setCanModify(false);
	}

	@Override
	public void deactivated(final GraphEditor editor) {
		selectedCore = null;
		stg = null;
		coreTable.clearSelection();
	}

	@Override
	public String getLabel() {
		return "Encoding conflict analyser";
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/tool-csc_analysis.svg");
	}

	@Override
	public Decorator getDecorator(final GraphEditor editor) {
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				if (node instanceof VisualNamedTransition) {
					VisualNamedTransition t = (VisualNamedTransition)node;
					String name = stg.getNodeMathReference(node);
					if (selectedCore == null) {
						final Color color = ((heightmap != null) && heightmap.containsKey(name)) ? heightmapColors[heightmap.get(name)-1] : null;
						return new Decoration(){
							@Override
							public Color getColorisation() {
								return null;
							}
							@Override
							public Color getBackground() {
								return color;
							}
						};
					} else if (selectedCore.contains(name)) {
						return new Decoration(){
							@Override
							public Color getColorisation() {
								return null;
							}
							@Override
							public Color getBackground() {
								return selectedCore.getColor();
							}
						};
					}
				}
				return null;
			}
		};
	}

	public void setCores(ArrayList<Core> cores) {
		this.cores = cores;
    	ArrayList<Color> palette = new ArrayList<Color>();
		heightmap = new HashMap<>();
		float hue = 0.1f;
		float saturation = 0.5f;
		float brightness = 0.5f;
		for (Core core: cores) {
			Color color = Color.getHSBColor(hue, saturation, brightness);
			brightness +=  0.5 / cores.size();
			palette.add(color);
			for (String name: core) {
				int height = (heightmap.containsKey(name) ? heightmap.get(name) : 0);
				height++;
				heightmap.put(name, height);
			}
		}
		heightmapColors = palette.toArray(new Color[palette.size()]);
	}

	@SuppressWarnings("serial")
	private final class CoreTableCellRendererImplementation implements TableCellRenderer {
		private final JLabel label = new JLabel() {
			@Override
			public void paint(Graphics g) {
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
				super.paint(g);
			}
		};

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			JLabel result = null;
			label.setBorder(PropertyEditorTable.BORDER_RENDER);
			if ((cores != null) && (row >= 0) && (row < cores.size())) {
				Core core = cores.get(row);
				label.setText((String) value);
				if (column == COLUMN_COLOR) {
					label.setBackground(core.getColor());
				} else {
					if (isSelected) {
						label.setForeground(table.getSelectionForeground());
						label.setBackground(table.getSelectionBackground());
					} else {
						label.setForeground(table.getForeground());
						label.setBackground(table.getBackground());
					}
				}
				result = label;
			}
			return result;
		}
	}

	@SuppressWarnings("serial")
	private final class CoreTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			String result;
			switch (column) {
			case COLUMN_COLOR:
				result = "Color";
				break;
			case COLUMN_CORE:
				result = "Core";
				break;
			default:
				result = "";
				break;
			}
			return result;
		}

		@Override
		public int getRowCount() {
			if (cores != null) {
				return cores.size();
			}
			return 0;
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object result = null;
			HashSet<String> core = cores.get(row);
			if (core != null) {
				switch (col) {
				case COLUMN_CORE:
					result = core.toString();
					break;
				case COLUMN_COLOR:
					result = "";
					break;
				default:
					result = null;
					break;
				}
			}
			return result;
		}
	}

	@SuppressWarnings("serial")
	private final class HeightmapTableCellRendererImplementation implements TableCellRenderer {
		private final JLabel label = new JLabel() {
			@Override
			public void paint(Graphics g) {
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
				super.paint(g);
			}
		};

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int col) {
			JLabel result = null;
			label.setBorder(PropertyEditorTable.BORDER_RENDER);
			if ((heightmapColors != null) && (col >= 0) && (col < heightmapColors.length)) {
				label.setText((String) value);
				label.setBackground(heightmapColors[col]);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				result = label;
			}
			return result;
		}
	}

	@SuppressWarnings("serial")
	private final class HeightmapTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return ((cores == null) ? 0 : cores.size());
		}

		@Override
		public int getRowCount() {
			return 1;
		}

		@Override
		public Object getValueAt(int row, int col) {
			return Integer.toString(col + 1);
		}
	}

}

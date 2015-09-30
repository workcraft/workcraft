package org.workcraft.plugins.stg.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.FontHelper;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;
import org.workcraft.plugins.stg.VisualNamedTransition;
import org.workcraft.plugins.stg.VisualSTG;

public class EncodingConflictAnalyserTool extends AbstractTool {

	final static private int COLUMN_COLOR = 0;
	final static private int COLUMN_CORE = 1;

	private VisualSTG stg;
	private ArrayList<Core> cores;
	private ArrayList<Core> selectedCores;
	private Heightmap heightmap;

	private JPanel interfacePanel;
	private JPanel controlPanel;
	private JPanel infoPanel;
	private JPanel statusPanel;
	private JRadioButton coresRadio;
	private JTable coresTable;
	private JRadioButton heightmapRadio;
	private JTable heightmapTable;

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		controlPanel = new JPanel();

		coresRadio = new JRadioButton("Show selected cores");
		coresRadio.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					coresTable.selectAll();
					coresTable.setEnabled(true);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					coresTable.clearSelection();
					coresTable.setEnabled(false);
					selectedCores = null;
				}
				editor.repaint();
				editor.requestFocus();
			}
		});
		coresTable = new JTable(new CoreTableModel());
		coresTable.getColumnModel().getColumn(COLUMN_COLOR).setPreferredWidth(50);
		coresTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		coresTable.setRowHeight(FontHelper.getFontSizeInPixels(coresTable.getFont()));
		coresTable.setDefaultRenderer(Object.class, new CoreTableCellRendererImplementation());
		coresTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		ListSelectionModel coreSelectionModel = coresTable.getSelectionModel();
		coreSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		coreSelectionModel.addListSelectionListener(new ListSelectionListener() {
	    	@Override
	    	public void valueChanged(ListSelectionEvent e) {
	    		selectedCores = new ArrayList<>();
	    		for (int rowIdx: coresTable.getSelectedRows()) {
	    			Core core = cores.get(rowIdx);
	    			selectedCores.add(core);
	    		}
	    		editor.repaint();
	    		editor.requestFocus();
	    	}
	    });
		JScrollPane coresScroll = new JScrollPane();
		coresScroll.setViewportView(coresTable);
		infoPanel = new JPanel(new BorderLayout());
		infoPanel.add(coresRadio, BorderLayout.NORTH);
		infoPanel.add(coresScroll, BorderLayout.CENTER);

		heightmapRadio = new JRadioButton("Show core density map");
		heightmapTable = new JTable(new HeightmapTableModel());
		heightmapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		heightmapTable.setRowHeight(FontHelper.getFontSizeInPixels(coresTable.getFont()));
		heightmapTable.setDefaultRenderer(Object.class, new HeightmapTableCellRendererImplementation());
		heightmapTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		heightmapTable.setToolTipText("Core density colors");
		statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		statusPanel.add(heightmapRadio, BorderLayout.NORTH);
		statusPanel.add(heightmapTable, BorderLayout.SOUTH);

		interfacePanel = new JPanel();
		interfacePanel.setLayout(new BorderLayout());
		interfacePanel.add(controlPanel, BorderLayout.NORTH);
		interfacePanel.add(infoPanel, BorderLayout.CENTER);
		interfacePanel.add(statusPanel, BorderLayout.SOUTH);
		interfacePanel.setPreferredSize(new Dimension(0, 0));

		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(coresRadio);
		radioGroup.add(heightmapRadio);
		coresRadio.setSelected(true);
		heightmapRadio.setSelected(true);
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}

	@Override
	public void activated(final GraphEditor editor) {
		stg = (VisualSTG)editor.getModel();
		super.activated(editor);
		editor.getWorkspaceEntry().setCanModify(false);
	}

	@Override
	public void deactivated(final GraphEditor editor) {
		stg = null;
		coresTable.clearSelection();
	}

	@Override
	public String getLabel() {
		return "Encoding conflict analyser";
	}

	@Override
	public boolean requiresButton() {
		return false;
	}

	@Override
	public Decorator getDecorator(final GraphEditor editor) {
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				if (node instanceof VisualNamedTransition) {
					VisualNamedTransition t = (VisualNamedTransition)node;
					final String name = stg.getNodeMathReference(node);
					if (selectedCores == null) {
						final Color color = ((heightmap == null) ? null : heightmap.getColor(name));
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
					} else {
						final ArrayList<Color> palette = new ArrayList<>();
						for (Core core: selectedCores) {
							if (core.contains(name)) {
								palette.add(core.getColor());
							}
						}
						return new CoreDecoration(){
							@Override
							public Color getColorisation() {
								return null;
							}
							@Override
							public Color getBackground() {
								return null;
							}
							@Override
							public Color[] getColorisationPalette() {
								return palette.toArray(new Color[palette.size()]);
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
		selectedCores = null;
		heightmap = new Heightmap(cores);
		heightmapTable.setModel(new HeightmapTableModel());
		heightmapRadio.setSelected(true);
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
				label.setToolTipText(core.toString());
				label.setText((String)value);
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
				boolean isSelected, boolean hasFocus, int row, int column) {
			JLabel result = null;
			label.setBorder(PropertyEditorTable.BORDER_RENDER);
			if (heightmap != null) {
				label.setText((String) value);
				int height = heightmap.getMinHeight() + column;
				label.setBackground(heightmap.getColor(height));
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
			return ((heightmap == null) ? 0 : heightmap.getPaletteSize());
		}

		@Override
		public int getRowCount() {
			return 1;
		}

		@Override
		public Object getValueAt(int row, int col) {
			String s = Integer.toString(heightmap.getMinHeight() + col);
			if ((col == 0) && heightmap.isReduced()) {
				s = "<" + s;
			}
			return s;
		}
	}

}

package org.workcraft.plugins.stg.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;
import org.workcraft.plugins.stg.VisualNamedTransition;
import org.workcraft.plugins.stg.VisualStg;

public class EncodingConflictAnalyserTool extends AbstractTool {

    private static final int COLUMN_COLOR = 0;
    private static final int COLUMN_CORE = 1;

    private VisualStg stg;
    private ArrayList<Core> cores;
    private ArrayList<Core> selectedCores;
    private CoreDensityMap density;

    private JPanel interfacePanel;
    private JRadioButton coresRadio;
    private JTable coresTable;
    private JRadioButton densityRadio;
    private JTable densityTable;

    @Override
    public void createInterfacePanel(final GraphEditor editor) {
        JPanel controlPanel = new JPanel();

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
        coresTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        coresTable.getColumnModel().getColumn(COLUMN_COLOR).setMaxWidth(50);
        coresTable.setRowHeight(SizeHelper.getComponentHeightFromFont(coresTable.getFont()));
        coresTable.setDefaultRenderer(Object.class, new CoreTableCellRendererImplementation());
        coresTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        coresTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!coresRadio.isSelected()) {
                    coresRadio.setSelected(true);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

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

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(coresRadio, BorderLayout.NORTH);
        infoPanel.add(coresScroll, BorderLayout.CENTER);

        densityRadio = new JRadioButton("Show core density map");
        densityTable = new JTable(new HeightmapTableModel());
        densityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        densityTable.setRowHeight(SizeHelper.getComponentHeightFromFont(coresTable.getFont()));
        densityTable.setDefaultRenderer(Object.class, new HeightmapTableCellRendererImplementation());
        densityTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        densityTable.setToolTipText("Core density colors");
        densityTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!densityRadio.isSelected()) {
                    densityRadio.setSelected(true);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(densityRadio, BorderLayout.NORTH);
        statusPanel.add(densityTable, BorderLayout.SOUTH);

        interfacePanel = new JPanel();
        interfacePanel.setLayout(new BorderLayout());
        interfacePanel.add(controlPanel, BorderLayout.NORTH);
        interfacePanel.add(infoPanel, BorderLayout.CENTER);
        interfacePanel.add(statusPanel, BorderLayout.SOUTH);
        interfacePanel.setPreferredSize(new Dimension(0, 0));

        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(coresRadio);
        radioGroup.add(densityRadio);
        coresRadio.setSelected(true);
        densityRadio.setSelected(true);
    }

    @Override
    public JPanel getInterfacePanel() {
        return interfacePanel;
    }

    @Override
    public void activated(final GraphEditor editor) {
        editor.getWorkspaceEntry().setCanModify(true);
        stg = (VisualStg) editor.getModel();
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
                    final String name = stg.getNodeMathReference(node);
                    if (selectedCores == null) {
                        final Color color = (density == null) ? null : density.getColor(name);
                        return new Decoration() {
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
                        return new CoreDecoration() {
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
        density = new CoreDensityMap(cores);
        densityTable.setModel(new HeightmapTableModel());
        densityRadio.setSelected(true);
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
                    String signalName = core.getComment();
                    if (signalName != null) {
                        String text = "<html>"
                                + "Conflict core for signal '" + signalName + "'"
                                + "<br>  Configuration 1: " + core.getFirstConfiguration()
                                + "<br>  Configuration 2: " + core.getSecondConfiguration()
                                + "</html>";
                        label.setToolTipText(text);
                    }
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
            if (density != null) {
                label.setText((String) value);
                Color color = density.getLevelColor(column);
                label.setBackground(color);
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
            return (density == null) ? 0 : density.getPaletteSize();
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public Object getValueAt(int row, int col) {
            String result;
            if ((col == 0) && density.isReduced()) {
                result = "<" + Integer.toString(density.getLevelDensity(col) + 1);
            } else {
                result = Integer.toString(density.getLevelDensity(col));
            }
            return result;
        }
    }

}

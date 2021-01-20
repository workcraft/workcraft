package org.workcraft.plugins.stg.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.controls.FlatHeaderRenderer;
import org.workcraft.gui.tools.AbstractGraphEditorTool;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.stg.VisualNamedTransition;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;

public class EncodingConflictAnalyserTool extends AbstractGraphEditorTool {

    private static final int COLUMN_COLOR = 0;
    private static final int COLUMN_CORE = 1;

    private ArrayList<Core> cores;
    private ArrayList<Core> selectedCores;
    private CoreDensityMap density;

    private JRadioButton coresRadio;
    private JTable coresTable;
    private JRadioButton densityRadio;
    private JTable densityTable;
    private JPanel panel;

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        if (panel != null) {
            return panel;
        }

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
        coresTable.getTableHeader().setDefaultRenderer(new FlatHeaderRenderer());
        coresTable.getTableHeader().setReorderingAllowed(false);
        coresTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        coresTable.getColumnModel().getColumn(COLUMN_COLOR).setMaxWidth(50);
        coresTable.setRowHeight(SizeHelper.getComponentHeightFromFont(coresTable.getFont()));
        coresTable.setDefaultRenderer(Object.class, new CoreTableCellRenderer());
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

        JPanel coresPanel = new JPanel(new BorderLayout());
        coresPanel.add(coresRadio, BorderLayout.NORTH);
        coresPanel.add(coresScroll, BorderLayout.CENTER);

        densityRadio = new JRadioButton("Show core density map");
        densityTable = new JTable(new HeightmapTableModel());
        densityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        densityTable.setRowHeight(SizeHelper.getComponentHeightFromFont(coresTable.getFont()));
        densityTable.setDefaultRenderer(Object.class, new HeightmapTableCellRenderer());
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

        JPanel densityPanel = new JPanel();
        densityPanel.setLayout(new BorderLayout());
        densityPanel.add(densityRadio, BorderLayout.NORTH);
        densityPanel.add(densityTable, BorderLayout.SOUTH);

        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(coresRadio);
        radioGroup.add(densityRadio);
        coresRadio.setSelected(true);
        densityRadio.setSelected(true);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(coresPanel, BorderLayout.CENTER);
        panel.add(densityPanel, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        super.deactivated(editor);
        coresTable.clearSelection();
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(false);
        we.setCanSelect(false);
        we.setCanCopy(false);
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
                    String name = editor.getModel().getMathReference(node);
                    boolean noColor = (selectedCores == null) && (density == null);
                    final Color color = noColor ? null : density.getColor(name);

                    ArrayList<Color> colors = null;
                    if (selectedCores != null) {
                        colors = new ArrayList<>();
                        for (Core core: selectedCores) {
                            if (core.contains(name)) {
                                colors.add(core.getColor());
                            }
                        }
                    }
                    final Color[] palette = (colors == null) ? null : colors.toArray(new Color[colors.size()]);

                    return new CoreDecoration() {
                        @Override
                        public Color getColorisation() {
                            return null;
                        }
                        @Override
                        public Color getBackground() {
                            return color;
                        }
                        @Override
                        public Color[] getColorisationPalette() {
                            return palette;
                        }
                    };
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
    private final class CoreTableCellRenderer implements TableCellRenderer {
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
            label.setBorder(GuiUtils.getTableCellBorder());
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
            switch (column) {
            case COLUMN_COLOR:
                return "<html><b>Color</b></html>";
            case COLUMN_CORE:
                return "<html><b>Core</b></html>";
            default:
                return  "";
            }
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
    private final class HeightmapTableCellRenderer implements TableCellRenderer {
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
            label.setBorder(GuiUtils.getTableCellBorder());
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

package org.workcraft.plugins.stg.tools;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.controls.FlatHeaderRenderer;
import org.workcraft.gui.tools.AbstractGraphEditorTool;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.stg.VisualNamedTransition;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class EncodingConflictAnalyserTool extends AbstractGraphEditorTool {

    private static final int COLUMN_COLOR = 0;
    private static final int COLUMN_CORE = 1;

    private ArrayList<EncodingConflict> encodingConflicts;
    private ArrayList<EncodingConflict> selectedEncodingConflicts;
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

        coresRadio = new JRadioButton("<html>Show cores for selected conflicts (filled)" +
                "<br>and traces overlap for 1 conflict (unfilled)</html>");

        coresRadio.addItemListener(e -> updateCoreVisualisationMode(editor, e));
        coresTable = new JTable(new CoreTableModel());
        coresTable.getTableHeader().setDefaultRenderer(new FlatHeaderRenderer());
        coresTable.getTableHeader().setReorderingAllowed(false);
        coresTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        coresTable.getColumnModel().getColumn(COLUMN_COLOR).setMaxWidth(50);
        coresTable.setRowHeight(SizeHelper.getComponentHeightFromFont(coresTable.getFont()));
        coresTable.setDefaultRenderer(Object.class, new CoreTableCellRenderer());
        coresTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        coresTable.addMouseListener(new MouseAdapter() {
            final ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
            final int defaultInitialDelay = toolTipManager.getInitialDelay();
            final int defaultDismissDelay = toolTipManager.getDismissDelay();

            @Override
            public void mouseEntered(MouseEvent e) {
                toolTipManager.setInitialDelay(0);
                toolTipManager.setDismissDelay(Integer.MAX_VALUE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                toolTipManager.setInitialDelay(defaultInitialDelay);
                toolTipManager.setDismissDelay(defaultDismissDelay);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!coresRadio.isSelected()) {
                    coresRadio.setSelected(true);
                }
            }
        });

        ListSelectionModel coreSelectionModel = coresTable.getSelectionModel();
        coreSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        coreSelectionModel.addListSelectionListener(e -> updateSelectedCores(editor));
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
        densityTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!densityRadio.isSelected()) {
                    densityRadio.setSelected(true);
                }
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

    private void updateCoreVisualisationMode(GraphEditor editor, ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            coresTable.selectAll();
            coresTable.setEnabled(true);
        } else if (e.getStateChange() == ItemEvent.DESELECTED) {
            coresTable.clearSelection();
            coresTable.setEnabled(false);
            selectedEncodingConflicts = null;
        }
        editor.repaint();
        editor.requestFocus();
    }

    private void updateSelectedCores(GraphEditor editor) {
        selectedEncodingConflicts = new ArrayList<>();
        for (int row : coresTable.getSelectedRows()) {
            EncodingConflict encodingConflict = encodingConflicts.get(row);
            selectedEncodingConflicts.add(encodingConflict);
        }
        editor.repaint();
        editor.requestFocus();
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
        return node -> {
            if (node instanceof VisualNamedTransition) {
                String name = editor.getModel().getMathReference(node);

                Color coreDensityColor = null;
                Color singleConflictCoreColor = null;
                Color singleConflictOverlapColor = null;
                ArrayList<Color> multiCoreColors = null;
                if (selectedEncodingConflicts == null) {
                    if (density != null) {
                        coreDensityColor = density.getColor(name);
                    }
                } else {
                    if (selectedEncodingConflicts.size() == 1) {
                        EncodingConflict encodingConflict = selectedEncodingConflicts.iterator().next();
                        if (encodingConflict.getCore().contains(name)) {
                            singleConflictCoreColor = encodingConflict.getColor();
                        }
                        if (encodingConflict.getOverlay().contains(name)) {
                            singleConflictOverlapColor = encodingConflict.getColor();
                        }
                    } else {
                        multiCoreColors = new ArrayList<>();
                        for (EncodingConflict encodingConflict : selectedEncodingConflicts) {
                            if (encodingConflict.getCore().contains(name)) {
                                multiCoreColors.add(encodingConflict.getColor());
                            }
                        }
                    }
                }
                final Color finalCoreDencityColor = coreDensityColor;
                final Color finalSingleConflictOverlapColor = singleConflictOverlapColor;
                final Color finalSingleConflictCoreColor = singleConflictCoreColor;
                final Color[] finalMultiCoreColors = (multiCoreColors == null)
                        ? null : multiCoreColors.toArray(new Color[0]);

                return new EncodingConflictDecoration() {
                    @Override
                    public Color getCoreDencityColor() {
                        return finalCoreDencityColor;
                    }
                    @Override
                    public Color getSingleConflictOverlapColor() {
                        return finalSingleConflictOverlapColor;
                    }
                    @Override
                    public Color getSingleConflictCoreColor() {
                        return finalSingleConflictCoreColor;
                    }
                    @Override
                    public Color[] getMultipleConflictColors() {
                        return finalMultiCoreColors;
                    }
                };
            }
            return null;
        };
    }

    public void setEncodingConflicts(ArrayList<EncodingConflict> encodingConflicts) {
        this.encodingConflicts = encodingConflicts;
        selectedEncodingConflicts = null;
        density = new CoreDensityMap(encodingConflicts);
        densityTable.setModel(new HeightmapTableModel());
        densityRadio.setSelected(true);
    }

    private final class CoreTableCellRenderer implements TableCellRenderer {
        private final JLabel label = new JLabel() {
            @Override
            public void paint(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paint(g);
            }
        };

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {

            JLabel result = null;
            label.setBorder(GuiUtils.getTableCellBorder());
            if ((encodingConflicts != null) && (row >= 0) && (row < encodingConflicts.size())) {
                EncodingConflict encodingConflict = encodingConflicts.get(row);
                label.setText((String) value);
                if (col == COLUMN_COLOR) {
                    label.setBackground(encodingConflict.getColor());
                } else {
                    String description = encodingConflict.getDescription();
                    label.setToolTipText("<html>" + TextUtils.useHtmlLinebreaks(description) + "</html>");
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

    private final class CoreTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
            case COLUMN_COLOR:
                return "Color";
            case COLUMN_CORE:
                return "Core";
            default:
                return  "";
            }
        }

        @Override
        public int getRowCount() {
            if (encodingConflicts != null) {
                return encodingConflicts.size();
            }
            return 0;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Object result = null;
            EncodingConflict encodingConflict = encodingConflicts.get(row);
            if (encodingConflict != null) {
                switch (col) {
                case COLUMN_CORE:
                    result = encodingConflict.getCoreAsString();
                    break;
                case COLUMN_COLOR:
                    result = "";
                    break;
                default:
                    break;
                }
            }
            return result;
        }
    }

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
            return (col == 0) && density.isReduced()
                    ? "<" + (density.getLevelDensity(col) + 1)
                    : Integer.toString(density.getLevelDensity(col));
        }
    }

}

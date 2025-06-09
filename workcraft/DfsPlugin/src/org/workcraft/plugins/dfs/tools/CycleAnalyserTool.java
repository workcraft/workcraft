package org.workcraft.plugins.dfs.tools;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.controls.WrapHeaderRenderer;
import org.workcraft.gui.tools.AbstractGraphEditorTool;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.dfs.VisualDelayComponent;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.shared.IntDocument;
import org.workcraft.utils.DirectedGraphUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class CycleAnalyserTool extends AbstractGraphEditorTool {

    public static final String INFINITY_SYMBOL = Character.toString((char) 0x221E);

    private static final int THROUGHPUT_COLUMN = 0;
    private static final int TOKEN_COLUMN = 1;
    private static final int DELAY_COLUMN = 2;
    private static final int CYCLE_COLUMN = 3;

    private VisualDfs dfs;
    private ArrayList<Cycle> cycles;
    private double minDelay;
    private double maxDelay;
    protected Cycle selectedCycle = null;
    private int cycleCount = 10;

    protected JPanel controlPanel;
    protected JScrollPane infoPanel;
    private JTable cycleTable;
    private JLabel cycleCountLabel;
    private JPanel panel;

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        if (panel != null) {
            return panel;
        }
        controlPanel = new JPanel();
        cycleTable = new JTable(new CycleTableModel());
        cycleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableColumnModel columnModel = cycleTable.getColumnModel();
        TableColumn throughputColumn = columnModel.getColumn(THROUGHPUT_COLUMN);
        throughputColumn.setPreferredWidth(Math.round(throughputColumn.getPreferredWidth() * 1.6f));

        WrapHeaderRenderer wrapHeaderRenderer = new WrapHeaderRenderer();
        columnModel.getColumn(THROUGHPUT_COLUMN).setHeaderRenderer(wrapHeaderRenderer);
        columnModel.getColumn(TOKEN_COLUMN).setHeaderRenderer(wrapHeaderRenderer);
        columnModel.getColumn(DELAY_COLUMN).setHeaderRenderer(wrapHeaderRenderer);
        columnModel.getColumn(CYCLE_COLUMN).setHeaderRenderer(wrapHeaderRenderer);

        cycleTable.setRowHeight(SizeHelper.getComponentHeightFromFont(cycleTable.getFont()));
        cycleTable.setDefaultRenderer(Object.class, new CycleTableCellRenderer());
        cycleTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        cycleTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = cycleTable.getSelectedRow();
                if ((cycles != null) && (selectedRow >= 0) && (selectedRow < cycles.size())) {
                    Cycle curCycle = cycles.get(selectedRow);
                    if (selectedCycle != curCycle) {
                        selectedCycle = curCycle;
                    } else {
                        selectedCycle = null;
                        cycleTable.clearSelection();
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
        infoPanel = new JScrollPane(cycleTable);

        final JTextField cycleCountText = new JTextField();
        Dimension dimension = cycleCountText.getPreferredSize();
        dimension.width = 40;
        cycleCountText.setPreferredSize(dimension);
        cycleCountText.setDocument(new IntDocument(3));
        cycleCountText.setText(String.valueOf(cycleCount));
        cycleCountText.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        cycleCount = Integer.parseInt(cycleCountText.getText());
                        resetSelectedCycle(editor);
                    } catch (NumberFormatException e) {
                        cycleCountText.setText(String.valueOf(cycleCount));
                    }
                } else if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cycleCountText.setText(String.valueOf(cycleCount));
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
                cycleCountText.setText(String.valueOf(cycleCount));
            }

            @Override
            public void focusLost(FocusEvent arg0) {
                cycleCount = Integer.parseInt(cycleCountText.getText());
                resetSelectedCycle(editor);
            }
        });

        cycleCountLabel = new JLabel();
        cycleCountLabel.setText("Cycle count:");
        cycleCountLabel.setLabelFor(cycleCountText);

        controlPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        controlPanel.add(cycleCountLabel);
        controlPanel.add(cycleCountText);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(controlPanel, BorderLayout.PAGE_START);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        dfs = (VisualDfs) editor.getModel();
        cycleTable.clearSelection();
        selectedCycle = null;
        cycles = findCycles();
        if (cycleCountLabel != null) {
            cycleCountLabel.setText("Cycle count (out of " + cycles.size() + "):");
        }
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        super.deactivated(editor);
        cycles = null;
        selectedCycle = null;
        dfs = null;
        cycleTable.clearSelection();
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
        return "Cycle analyser";
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_Y;
    }

    @Override
    public Icon getIcon() {
        return GuiUtils.createIconFromSVG("images/dfs-tool-cycle_analysis.svg");
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return node -> {
            if (node instanceof VisualDelayComponent) {
                if (selectedCycle == null) {
                    double delay = ((VisualDelayComponent) node).getReferencedComponent().getDelay();
                    double range = maxDelay - minDelay;
                    double offset = delay - minDelay;
                    final Color fgColor = (range > 0 &&  offset > 0.8 * range) ? Color.RED : null;
                    return new Decoration() {
                        @Override
                        public Color getColorisation() {
                            return fgColor;
                        }
                    };
                } else if (selectedCycle.components.contains(node)) {
                    double delay = selectedCycle.getEffectiveDelay((VisualDelayComponent) node);
                    double range = selectedCycle.maxDelay - selectedCycle.minDelay;
                    double offset = delay - selectedCycle.minDelay;
                    int bgIintencity = 150;
                    if (range > 0) {
                        bgIintencity = (int) (bgIintencity + (255 - bgIintencity) * offset / range);
                    }
                    final Color fgColor = (range > 0 &&  offset > 0.8 * range) ? Color.RED : null;
                    final Color bgColor = new Color(bgIintencity, 0, 0);
                    return new Decoration() {
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
        };
    }

    private ArrayList<Cycle> findCycles() {
        ArrayList<Cycle> result = new ArrayList<>();
        Map<VisualDelayComponent, Set<VisualDelayComponent>> graph = new HashMap<>();
        // Update global min and max delay values
        Collection<VisualDelayComponent> allComponents = Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualDelayComponent.class);
        boolean first = true;
        for (VisualDelayComponent c: allComponents) {
            graph.put(c, dfs.getPostset(c, VisualDelayComponent.class));
            double delay = c.getReferencedComponent().getDelay();
            if (first || minDelay > delay) {
                minDelay = delay;
            }
            if (first || maxDelay < delay) {
                maxDelay = delay;
            }
            first = false;
        }
        for (List<VisualDelayComponent> cycle : DirectedGraphUtils.findSimpleCycles(graph)) {
            result.add(new Cycle(dfs, new LinkedHashSet<>(cycle)));
        }
        Collections.sort(result);
        return result;
    }

    private void resetSelectedCycle(final GraphEditor editor) {
        selectedCycle = null;
        cycleTable.tableChanged(null);
        editor.repaint();
    }

    private final class CycleTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case CYCLE_COLUMN -> "Cycle";
                case TOKEN_COLUMN -> "Spread tokens";
                case DELAY_COLUMN -> "Delay";
                case THROUGHPUT_COLUMN -> "Throughput";
                default -> "";
            };
        }

        @Override
        public int getRowCount() {
            if (cycles != null) {
                return Math.min(cycleCount, cycles.size());
            }
            return 0;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Object result = null;
            Cycle cycle = cycles.get(row);
            if (cycle != null) {
                switch (col) {
                    case THROUGHPUT_COLUMN:
                        if (cycle.totalDelay == 0) {
                            result = INFINITY_SYMBOL;
                        } else {
                            result = new DecimalFormat("#.###").format(cycle.throughput);
                        }
                        break;
                    case TOKEN_COLUMN:
                        result = cycle.tokenCount;
                        break;
                    case DELAY_COLUMN:
                        result = new DecimalFormat("#.###").format(cycle.totalDelay);
                        break;
                    case CYCLE_COLUMN:
                        result = cycle.toString();
                        break;
                    default:
                        break;
                }
            }
            return result;
        }
    }

    private final class CycleTableCellRenderer implements TableCellRenderer {
        private final JLabel label = new JLabel() {
            @Override
            public void paint(final Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paint(g);
            }
        };

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            label.setBorder(GuiUtils.getTableCellBorder());
            if ((cycles != null) && (row >= 0) && (row < cycles.size())) {
                label.setText(value.toString());
                label.setToolTipText(cycles.get(row).toString());
            }
            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
            } else {
                label.setBackground(table.getBackground());
            }
            return label;
        }
    }

}

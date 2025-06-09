package org.workcraft.plugins.son.gui;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.TimeEstimatorSettings;
import org.workcraft.plugins.son.algorithm.BFSEntireEstimationAlg;
import org.workcraft.plugins.son.algorithm.DFSEstimationAlg;
import org.workcraft.plugins.son.exception.*;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.ScenarioRef;
import org.workcraft.plugins.son.util.ScenarioSaveList;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TimeEstimatorDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    protected SON net;
    protected GraphEditor editor;
    protected TimeEstimatorSettings settings;
    protected Node selection;
    protected Granularity granularity;

    protected DefaultDurationPanel defaultDurationPanel;
    protected JScrollPane tabelPanel;
    protected ScenarioTable scenarioTable;

    protected JPanel buttonsPanel;
    protected JPanel durationPanel;
    protected JButton runButton;
    protected JButton cancelButton;
    protected JCheckBox setDuration;
    protected JCheckBox intermediate;
    protected JCheckBox entireEst;
    protected JCheckBox narrow;
    protected JCheckBox twoDir;
    protected Dimension buttonSize = new Dimension(80, 25);
    protected boolean modalResult;

    public TimeEstimatorDialog(Window owner, GraphEditor editor, TimeEstimatorSettings settings, Node selection, Granularity g) {
        super(Framework.getInstance().getMainWindow(), "Estimator Setting", ModalityType.TOOLKIT_MODAL);
        net = (SON) editor.getModel().getMathModel();
        this.editor = editor;
        this.settings = settings;
        this.selection = selection;
        this.granularity = g;

        creatCheckboxPanel();
        createScenarioTable();
        createButtonsPanel();

        setLayout(new BorderLayout(10, 10));
        add(durationPanel, BorderLayout.NORTH);
        add(tabelPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setParameters();
            }
        });
    }

    protected void creatCheckboxPanel() {
        defaultDurationPanel = new DefaultDurationPanel(settings.getDuration());
        defaultDurationPanel.setLayout(GuiUtils.createFlowLayout());

        JPanel entirePanel = new JPanel(new BorderLayout());
        entirePanel.setBorder(GuiUtils.getTitledBorder("Entire estimation"));
        entirePanel.setLayout(new GridLayout(3, 0));

        entireEst = new JCheckBox("Estimate time value for entire SON");
        entireEst.setSelected(false);
        entireEst.setLayout(GuiUtils.createFlowLayout());

        narrow = new JCheckBox("Narrow down specified values");
        narrow.setSelected(false);
        narrow.setLayout(GuiUtils.createFlowLayout());
        narrow.setEnabled(false);

        twoDir = new JCheckBox("Two directions search");
        twoDir.setSelected(false);
        twoDir.setLayout(GuiUtils.createFlowLayout());
        twoDir.setEnabled(false);

        entirePanel.add(entireEst);
        // entirePanel.add(narrow);
        entirePanel.add(twoDir);

        JPanel singlePanel = new JPanel(new BorderLayout());
        singlePanel.setBorder(GuiUtils.getTitledBorder("Single Node estimation"));
        singlePanel.setLayout(new GridLayout(2, 0));

        intermediate = new JCheckBox("Set values for intermediate nodes");
        intermediate.setSelected(false);
        intermediate.setLayout(GuiUtils.createFlowLayout());

        setDuration = new JCheckBox("Set default duration for all unspecifed nodes");
        setDuration.setSelected(false);
        setDuration.setLayout(GuiUtils.createFlowLayout());

        // singlePanel.add(intermediate);
        // singlePanel.add(setDuration);

        durationPanel = new JPanel();
        // durationPanel.setBorder(SizeHelper.getTitleBorder("Default Duration Setting"));
        durationPanel.setLayout(new BorderLayout());
        durationPanel.add(defaultDurationPanel, BorderLayout.NORTH);
        // durationPanel.add(singlePanel, BorderLayout.WEST);
        durationPanel.add(entirePanel, BorderLayout.EAST);

        entireEst.addActionListener(event -> {
            if (entireEst.isSelected()) {
                setDuration.setEnabled(false);
                intermediate.setEnabled(false);
                narrow.setEnabled(true);
                twoDir.setEnabled(true);
            } else {
                setDuration.setEnabled(true);
                intermediate.setEnabled(true);
                narrow.setEnabled(false);
                twoDir.setEnabled(false);
            }
        });
    }

    protected void createScenarioTable() {
        scenarioTable = new ScenarioTable();
        scenarioTable.setModel(new ScenarioListTableModel());
        scenarioTable.setNet(net);
        scenarioTable.updateColor(selection);
        ScenarioSaveList saveList = saveListFilter(net.importScenarios(), selection);
        scenarioTable.setSaveList(saveList);

        tabelPanel = new JScrollPane(scenarioTable);
        tabelPanel.setPreferredSize(new Dimension(1, 100));

        scenarioTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = scenarioTable.getSelectedColumn();
                int row = scenarioTable.getSelectedRow();

                ScenarioSaveList saveList = scenarioTable.getSaveList();

                if (column == 0 && row < saveList.size()) {
                    saveList.setPosition(row);
                    Object obj = scenarioTable.getValueAt(row, column);
                    if (obj instanceof ScenarioRef) {
                        scenarioTable.setScenarioRef((ScenarioRef) obj);
                        scenarioTable.setIsCellColor(true);
                        scenarioTable.updateTable();
                        scenarioTable.updateColor(selection);
                    }
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
    }

    private ScenarioSaveList saveListFilter(ScenarioSaveList saveList, Node selection) {
        ScenarioSaveList result = new ScenarioSaveList();
        for (ScenarioRef ref : saveList) {
            for (String str : ref) {
                if (str.equals(net.getNodeReference(selection))) {
                    result.add(ref);
                    break;
                }
            }
        }
        return result;
    }

    protected void createButtonsPanel() {
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        runButton = new JButton("Run");
        runButton.setPreferredSize(buttonSize);
        runButton.addActionListener(event -> {
            setParameters();
            if (defaultDurationPanel.isValidDuration()) {
                modalResult = true;
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                if (entireEst.isSelected()) {
                    // boolean isNarrow = narrow.isSelected() &&
                    // narrow.isEnabled();
                    boolean isTwodir = twoDir.isSelected() && twoDir.isEnabled();
                    setVisible(false);
                    BFSEntireEstimationAlg alg1 = null;
                    try {
                        alg1 = new BFSEntireEstimationAlg(net, getDefaultDuration(), granularity, getScenarioRef(),
                                isTwodir);
                        alg1.prepare();
                        alg1.estimateEntire();
                        alg1.complete();
                    } catch (AlternativeStructureException e21) {
                        JOptionPane.showMessageDialog(mainWindow, e21.getMessage(),
                                "Scenario selection error", JOptionPane.ERROR_MESSAGE);
                    } catch (TimeEstimationException | TimeOutOfBoundsException e11) {
                        JOptionPane.showMessageDialog(mainWindow, e11.getMessage(), "",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (SyncCycleException e12) {
                        JOptionPane.showMessageDialog(mainWindow, e12.getMessage(),
                                "Synchronous cycle error", JOptionPane.ERROR_MESSAGE);
                    }

                } else {
                    DFSEstimationAlg alg;
                    try {
                        alg = new DFSEstimationAlg(net, getDefaultDuration(), granularity, getScenarioRef());
                        setVisible(false);
                        alg.prepare();

                        try {
                            alg.estimateEndTime(selection);
                        } catch (TimeOutOfBoundsException | TimeEstimationException e) {
                            errMsg(e.getMessage());
                        }
                        try {
                            alg.estimateStartTime(selection);
                        } catch (TimeOutOfBoundsException | TimeEstimationException e) {
                            errMsg(e.getMessage());
                        }
                        try {
                            alg.estimatDuration(selection);
                        } catch (TimeInconsistencyException | TimeOutOfBoundsException e) {
                            errMsg(e.getMessage());
                        }

                        alg.complete(selection);

                    } catch (AlternativeStructureException e) {
                        errMsg(e.getMessage());
                    }
                }
            } else {
                defaultDurationPanel.getMin().setForeground(Color.RED);
                defaultDurationPanel.getMax().setForeground(Color.RED);
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(buttonSize);
        cancelButton.addActionListener(event -> {
            modalResult = false;
            setParameters();
            setVisible(false);
        });

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(runButton);
    }

    protected class ScenarioListTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            return "Scenario List";
        }

        @Override
        public int getRowCount() {
            return scenarioTable.getSaveList().size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (column == 0) {
                ScenarioSaveList saveList = scenarioTable.getSaveList();
                if (!saveList.isEmpty() && (row < saveList.size())) {
                    return saveList.get(row);
                }
            }
            return "";
        }
    }

    protected void errMsg(String msg) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        JOptionPane.showMessageDialog(mainWindow, msg, "Time estimation error", JOptionPane.ERROR_MESSAGE);
    }

    protected void setParameters() {
        settings.setPosition(scenarioTable.getSaveList().getPosition());
        settings.setDuration(defaultDurationPanel.getDefaultDuration());
    }

    public Interval getDefaultDuration() {
        return settings.getDuration();
    }

    public ScenarioTable getScenarioTable() {
        return scenarioTable;
    }

    public ScenarioRef getScenarioRef() {
        if (scenarioTable.getScenarioRef().isEmpty()) {
            return null;
        } else {
            return scenarioTable.getScenarioRef();
        }
    }

    public boolean reveal() {
        setVisible(true);
        return modalResult;
    }
}

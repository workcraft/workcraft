package org.workcraft.plugins.xmas.commands;

import org.workcraft.commands.Command;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SuppressWarnings("unchecked")
public class ConfigurationCommand implements Command {

    //private final Framework framework;

    //public SyncTool() {

        //this.framework = framework;
    //}

    @Override
    public String getDisplayName() {
        return "Configure Verif";
    }

    @Override
    public Section getSection() {
        return new Section("Verification");
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Xmas.class);
    }

    private JFrame mainFrame = null;
    private JComboBox<?> trcombob = null;
    private JComboBox<?> lvcombob = null;
    private JComboBox<String> hlcombob = null;
    private JComboBox<?> dycombob = null;
    private JComboBox<?> slcombob = null;

    private static final String[] trchoices = {
        "q1",
        "q2",
        "a1",
        "a2",
    };

    private static final String[] lvlchoices = {
        "normal",
        "advanced",
    };

    private static final String[] hlchoices = {
        "none",
        "sel",
    };

    private static final String[] solchoices = {
        "text",
        "popup",
    };

    private static final String[] solno = {
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
    };

    public void dispose() {
        mainFrame.setVisible(false);
    }

    private void initSettings() {
        Scanner sc = null;
        try {
            File vsettingsFile = XmasSettings.getTempVxmVsettingsFile();
            sc = new Scanner(vsettingsFile);
        } catch (FileNotFoundException e) {
            LogUtils.logError(e.getMessage());
        }
        while (sc.hasNextLine()) {
            Scanner line = new Scanner(sc.nextLine());
            Scanner nxt = new Scanner(line.next());
            String check = nxt.next();
            if (check.startsWith("trace")) {
                nxt = new Scanner(line.next());
                trcombob.setSelectedItem(nxt.next());
            } else if (check.startsWith("level")) {
                nxt = new Scanner(line.next());
                lvcombob.setSelectedItem(nxt.next());
            } else if (check.startsWith("display")) {
                nxt = new Scanner(line.next());
                dycombob.setSelectedItem(nxt.next());
            } else if (check.startsWith("highlight")) {
                nxt = new Scanner(line.next());
                hlcombob.setSelectedItem(nxt.next());
            } else if (check.startsWith("soln")) {
                nxt = new Scanner(line.next());
                slcombob.setSelectedItem(nxt.next());
            }
        }
    }

    public void writeOutput() {
        //JPanel panelmain=mainFrame.getContentPane().get();

        String trname = (String) trcombob.getSelectedItem();
        String lvname = (String) lvcombob.getSelectedItem();
        String dyname = (String) dycombob.getSelectedItem();
        String hlname = (String) hlcombob.getSelectedItem();
        String slname = (String) slcombob.getSelectedItem();

        PrintWriter writer = null;
        try {
            File vsettingsFile = XmasSettings.getTempVxmVsettingsFile();
            writer = new PrintWriter(vsettingsFile);
            writer.println("trace " + trname);
            writer.println("level " + lvname);
            writer.println("highlight " + hlname);
            writer.println("display " + dyname);
            writer.println("soln " + slname);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private int loaded = 0;

    @SuppressWarnings("PMD.AssignmentInOperand")
    @Override
    public void run(WorkspaceEntry we) {
        System.out.println("Running tests");
        int cntSyncNodes = 0;
        mainFrame = new JFrame("Configure Verification");
        JPanel panelmain = new JPanel();
        mainFrame.getContentPane().add(panelmain, BorderLayout.PAGE_START);
        panelmain.setLayout(new BoxLayout(panelmain, BoxLayout.PAGE_AXIS));

        System.out.println("loaded = " + loaded);
        List<JPanel> panellist = new ArrayList<>();
        cntSyncNodes = 1;
        for (int no = 0; no < cntSyncNodes; no++) {
            panellist.add(new JPanel());
            panellist.get(panellist.size() - 1).add(new JLabel(" Trace "));
            panellist.get(panellist.size() - 1).add(trcombob = new JComboBox<>(trchoices));
            panellist.get(panellist.size() - 1).add(new JLabel(" Level "));
            panellist.get(panellist.size() - 1).add(lvcombob = new JComboBox<>(lvlchoices));
            panellist.get(panellist.size() - 1).add(new JLabel(" Display "));
            panellist.get(panellist.size() - 1).add(dycombob = new JComboBox<>(solchoices));
            panellist.get(panellist.size() - 1).add(new JLabel(" Highlight "));
            panellist.get(panellist.size() - 1).add(hlcombob = new JComboBox<>(hlchoices));
            panellist.get(panellist.size() - 1).add(new JLabel(" Soln "));
            panellist.get(panellist.size() - 1).add(slcombob = new JComboBox<>(solno));
            //panellist.get(panellist.size()-1).add(new JLabel(" Soln "));
            //panellist.get(panellist.size()-1).add(new JTextField("1", 1));
        }
        lvcombob.addActionListener(event -> {
            JComboBox<String> comboBox = (JComboBox<String>) event.getSource();
            Object selected = comboBox.getSelectedItem();
            if ("normal".equals(selected)) {
                hlcombob.removeItemAt(1);
                hlcombob.addItem("local");
            } else if ("advanced".equals(selected)) {
                hlcombob.removeItemAt(1);
                hlcombob.addItem("rel");
            }
        });
        initSettings();
        loaded = 1;

        for (JPanel plist : panellist) {
            panelmain.add(plist);
        }

        JPanel panelb = new JPanel();
        panelb.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton okButton = new JButton("OK");
        panelb.add(Box.createHorizontalGlue());
        panelb.add(cancelButton);
        panelb.add(okButton);
        panelmain.add(panelb);

        cancelButton.addActionListener(event -> dispose());

        okButton.addActionListener(event -> {
            dispose();
            writeOutput();
        });

        mainFrame.pack();
        mainFrame.setVisible(true);
    }

}

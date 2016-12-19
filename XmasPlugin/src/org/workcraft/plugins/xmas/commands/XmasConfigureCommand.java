package org.workcraft.plugins.xmas.commands;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.workcraft.gui.graph.commands.Command;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class XmasConfigureCommand implements Command {

    //private final Framework framework;

    //public SyncTool() {

        //this.framework = framework;
    //}

    public String getDisplayName() {
        return "Configure Verif";
    }

    public String getSection() {
        return "Verification";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Xmas.class);
    }

    int cntSyncNodes = 0;
    JFrame mainFrame = null;
    JComboBox trcombob = null;
    JComboBox lvcombob = null;
    JComboBox hlcombob = null;
    JComboBox dycombob = null;
    JComboBox slcombob = null;

    String[] trchoices = {
        "q1",
        "q2",
        "a1",
        "a2",
    };

    String[] lvlchoices = {
        "normal",
        "advanced",
    };

    String[] hlchoices = {
        "none",
        "sel",
    };

    String[] solchoices = {
        "text",
        "popup",
    };

    String[] solno = {
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

    public List<String> slist = new ArrayList<>();

    private void initSettings() {
        Scanner sc = null;
        try {
            File vsettingsFile = XmasSettings.getTempVxmVsettingsFile();
            sc = new Scanner(vsettingsFile);
        } catch (FileNotFoundException e) {
            LogUtils.logErrorLine(e.getMessage());
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

    int loaded = 0;
    public List<String> slist1;
    public List<String> slist2;

    @Override
    public void run(WorkspaceEntry we) {
        System.out.println("Running tests");
        cntSyncNodes = 0;
        mainFrame = new JFrame("Configure Verification");
        JPanel panelmain = new JPanel();
        mainFrame.getContentPane().add(panelmain, BorderLayout.PAGE_START);
        panelmain.setLayout(new BoxLayout(panelmain, BoxLayout.PAGE_AXIS));

        System.out.println("loaded = " + loaded);
        List<JPanel> panellist = new ArrayList<>();
        cntSyncNodes = 1;
        for (int no = 0; no < cntSyncNodes; no = no + 1) {
            panellist.add(new JPanel());
            panellist.get(panellist.size() - 1).add(new JLabel(" Trace "));
            panellist.get(panellist.size() - 1).add(trcombob = new JComboBox(trchoices));
            panellist.get(panellist.size() - 1).add(new JLabel(" Level "));
            panellist.get(panellist.size() - 1).add(lvcombob = new JComboBox(lvlchoices));
            panellist.get(panellist.size() - 1).add(new JLabel(" Display "));
            panellist.get(panellist.size() - 1).add(dycombob = new JComboBox(solchoices));
            panellist.get(panellist.size() - 1).add(new JLabel(" Highlight "));
            panellist.get(panellist.size() - 1).add(hlcombob = new JComboBox(hlchoices));
            panellist.get(panellist.size() - 1).add(new JLabel(" Soln "));
            panellist.get(panellist.size() - 1).add(slcombob = new JComboBox(solno));
            //panellist.get(panellist.size()-1).add(new JLabel(" Soln "));
            //panellist.get(panellist.size()-1).add(new JTextField("1", 1));
        }
        lvcombob.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                JComboBox comboBox = (JComboBox) event.getSource();

                Object selected = comboBox.getSelectedItem();
                if (selected.toString().equals("normal")) {
                        hlcombob.removeItemAt(1);
                        hlcombob.addItem("local");
                } else if (selected.toString().equals("advanced")) {
                        hlcombob.removeItemAt(1);
                        hlcombob.addItem("rel");
                }
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

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }

        });

        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
                writeOutput();
            }

        });

        mainFrame.pack();
        mainFrame.setVisible(true);
    }

}

package org.workcraft.plugins.xmas.tools;

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

import org.workcraft.Tool;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.util.LogUtils;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;


public class VerConfTool implements Tool {

    //private final Framework framework;

    //public SyncTool() {

        //this.framework = framework;
    //}


    private float LEFT_ALIGNMENT;


    public String getDisplayName() {
        return "Configure Verif";
    }


    public String getSection() {
        return "Verification";
    }


    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, Xmas.class);
    }

    int cnt_syncnodes=0;
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
             "a2"
    };

    String[] lvlchoices = {
             "normal",
             "advanced"
    };

    String[] hlchoices = {
             "none",
             "sel"
    };

    String[] solchoices = {
             "text",
             "popup"
    };

    String[] solno = {
             "1",
             "2",
             "3",
             "4",
             "5",
             "6",
             "7",
             "8"
    };

    public void dispose() {
        mainFrame.setVisible(false);
    }

    public List<String> slist=new ArrayList<String>();

    private void init_settings() {
        Scanner sc = null;
        try {
            File vsettingsFile = XmasSettings.getTempVxmVsettingsFile();
            sc=new Scanner(vsettingsFile);
        } catch (FileNotFoundException e) {
            LogUtils.logErrorLine(e.getMessage());
        }
        while(sc.hasNextLine()) {
            Scanner line_=new Scanner(sc.nextLine());
            Scanner nxt=new Scanner(line_.next());
            String check=nxt.next();
            String str;
            if(check.startsWith("trace")) {
                nxt=new Scanner(line_.next());
                trcombob.setSelectedItem(nxt.next());
            }
            else if(check.startsWith("level")) {
                nxt=new Scanner(line_.next());
                lvcombob.setSelectedItem(nxt.next());
            }
            else if(check.startsWith("display")) {
                nxt=new Scanner(line_.next());
                dycombob.setSelectedItem(nxt.next());
            }
            else if(check.startsWith("highlight")) {
                nxt=new Scanner(line_.next());
                hlcombob.setSelectedItem(nxt.next());
            }
            else if(check.startsWith("soln")) {
                nxt=new Scanner(line_.next());
                slcombob.setSelectedItem(nxt.next());
            }
        }
    }

    public void write_output() {
        //JPanel panelmain=mainFrame.getContentPane().get();

        String trname = (String)trcombob.getSelectedItem();
        String lvname = (String)lvcombob.getSelectedItem();
        String dyname = (String)dycombob.getSelectedItem();
        String hlname = (String)hlcombob.getSelectedItem();
        String slname = (String)slcombob.getSelectedItem();

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

    public void run(WorkspaceEntry we) {
        System.out.println("Running tests");
        final VisualXmas vnet = (VisualXmas)we.getModelEntry().getVisualModel();

        cnt_syncnodes=0;

        Xmas cnet = (Xmas)we.getModelEntry().getMathModel();

        //SyncMenu dialog = new SyncMenu();

        int num_nodes=0;

        mainFrame = new JFrame("Configure Verification");
        JPanel panelmain = new JPanel();
        mainFrame.getContentPane().add(panelmain,BorderLayout.PAGE_START);
        panelmain.setLayout(new BoxLayout(panelmain,BoxLayout.PAGE_AXIS));

        System.out.println("loaded = " + loaded);
        List<JPanel> panellist = new ArrayList<JPanel>();
        cnt_syncnodes=1;
        for(int no = 0; no < cnt_syncnodes; no = no+1) {
            panellist.add(new JPanel());
            panellist.get(panellist.size()-1).add(new JLabel(" Trace "));
            panellist.get(panellist.size()-1).add(trcombob = new JComboBox(trchoices));
            panellist.get(panellist.size()-1).add(new JLabel(" Level "));
            panellist.get(panellist.size()-1).add(lvcombob = new JComboBox(lvlchoices));
            panellist.get(panellist.size()-1).add(new JLabel(" Display "));
            panellist.get(panellist.size()-1).add(dycombob = new JComboBox(solchoices));
            panellist.get(panellist.size()-1).add(new JLabel(" Highlight "));
            panellist.get(panellist.size()-1).add(hlcombob = new JComboBox(hlchoices));
            panellist.get(panellist.size()-1).add(new JLabel(" Soln "));
            panellist.get(panellist.size()-1).add(slcombob = new JComboBox(solno));
            //panellist.get(panellist.size()-1).add(new JLabel(" Soln "));
            //panellist.get(panellist.size()-1).add(new JTextField("1",1));
        }
        lvcombob.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                JComboBox comboBox = (JComboBox) event.getSource();

                Object selected = comboBox.getSelectedItem();
                if(selected.toString().equals("normal")) {
                        hlcombob.removeItemAt(1);
                        hlcombob.addItem("local");
                }
                else if(selected.toString().equals("advanced")) {
                        hlcombob.removeItemAt(1);
                        hlcombob.addItem("rel");
                }
            }
        });
        init_settings();
        loaded=1;

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
                int no=1;

                dispose();
                write_output();

            }

        });

        mainFrame.pack();
        mainFrame.setVisible(true);

    }

}

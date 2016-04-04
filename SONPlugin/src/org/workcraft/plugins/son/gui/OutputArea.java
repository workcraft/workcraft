package org.workcraft.plugins.son.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

public class OutputArea extends JFrame {

    private static final long serialVersionUID = 1L;
    private Font font = new Font("Calibri", Font.PLAIN, 15);

    private JTextArea textArea;
    private JMenuBar menu;
    private JMenu file;
    private JMenuItem export, exit;

    JToolBar toolBar = new JToolBar();

    private void createMenu() {
        menu = new JMenuBar();
        file = new JMenu(" File ");
        export = new JMenuItem("Export result");
        exit = new JMenuItem("Exit");

        menu.add(file);
        file.add(export);
        file.add(exit);
        toolBar.setFloatable(true);

        class SaveAs implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                export();
            }
        }

        class Exit implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                fileExit();
            }
        }

        export.addActionListener(new SaveAs());
        exit.addActionListener(new Exit());
    }

    private void export() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int r = chooser.showSaveDialog(this);
        if (r == JFileChooser.CANCEL_OPTION) {
            return;
        }
        File myfile = chooser.getSelectedFile();

        if (myfile != null && r == JFileChooser.APPROVE_OPTION) {

            Pattern pattern = Pattern.compile(".*\\.[a-z]+", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(myfile.getAbsolutePath());
            if (!matcher.find()) {
                myfile = new File(myfile.getAbsolutePath().concat(".txt"));
            }
        }
        if (myfile == null || myfile.getName().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a file name!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (myfile.exists()) {
            r = JOptionPane.showConfirmDialog(this, "A file with same name already exists. Do you want to overwrite it?");
            if (r != 0) {
                return;
            }
        }
        try {
            FileWriter fw = new FileWriter(myfile);
            fw.write(textArea.getText());
            setTitle(myfile.getName() + " - Output");
            fw.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save the file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void fileExit() {
        this.setVisible(false);
    }

    public OutputArea(JTextArea textArea, String title) {
        this.textArea = textArea;
        textArea.setEditable(false);
        textArea.setFont(font);

        Container contentPane = this.getContentPane();

        contentPane.setLayout(new BorderLayout());
        createMenu();
        contentPane.add(menu, BorderLayout.NORTH);

        contentPane.add(
                new JScrollPane(
                    textArea,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
                BorderLayout.CENTER);
        this.pack();
        this.setVisible(true);
        this.setTitle(title);

        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;
        setLocation(width / 2, height / 4);

    }

}

package org.workcraft.plugins.son.gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputArea extends JFrame {

    private static final Font font = new Font("Calibri", Font.PLAIN, 15);
    private final JToolBar toolBar = new JToolBar();
    private final JTextArea textArea;
    private JMenuBar menu;

    private void createMenu() {
        menu = new JMenuBar();
        JMenu file = new JMenu(" File ");
        JMenuItem export = new JMenuItem("Export result");
        JMenuItem exit = new JMenuItem("Exit");

        menu.add(file);
        file.add(export);
        file.add(exit);
        toolBar.setFloatable(true);
        export.addActionListener(event -> export());
        exit.addActionListener(event -> fileExit());
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
        pack();
        setVisible(true);
        setTitle(title);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2, screenSize.height / 4);
    }

}

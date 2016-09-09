package org.workcraft.plugins.stg.concepts;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class ConceptsWriterDialog extends JDialog {

    private static File lastFileUsed, lastDirUsed;

    private final JPanel content, btnPanel;
    private JTextArea conceptsText;
    private boolean changed = false, translate = false;

    public ConceptsWriterDialog() {
        super(Framework.getInstance().getMainWindow(), "Write and translate Concepts", ModalityType.APPLICATION_MODAL);
        //updateLastDirUsed();

        content = new JPanel();
        content.setLayout(new BorderLayout());

        btnPanel = new JPanel();
        btnPanel.setLayout(new BorderLayout());

        createScrollPane();
        createFileBtnPanel();
        createFinalBtnPanel();

        content.add(btnPanel, BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setContentPane(content);
        setMinimumSize(new Dimension(1000, 500));
        pack();
        this.setLocationRelativeTo(Framework.getInstance().getMainWindow());

    }

    private void createScrollPane() {
        JLabel conceptsLabel = new JLabel("Enter concepts: ");
        content.add(conceptsLabel, BorderLayout.NORTH);

        conceptsText = new JTextArea();
        conceptsText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));
        conceptsText.setText(getDefaultText());
        if (lastFileUsed != null && lastFileUsed.exists()) {
            try {
                conceptsText.setText(readFile(lastFileUsed));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        conceptsText.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                changed = true;
            }

            @Override
            public void keyPressed(KeyEvent e) {
                //Do nothing
            }

            @Override
            public void keyReleased(KeyEvent e) {
                //Do nothing
            }

        });

        JScrollPane scrollPane = new JScrollPane(conceptsText);

        content.add(scrollPane, BorderLayout.CENTER);
    }

    private void createFileBtnPanel() {
        JButton openFileBtn = GUI.createDialogButton("Open file");
        JButton saveFileBtn = GUI.createDialogButton("Save to file");
        JButton resetBtn = GUI.createDialogButton("Reset to default");
        JPanel fileBtnPanel = new JPanel();
        fileBtnPanel.add(openFileBtn);
        fileBtnPanel.add(saveFileBtn);
        fileBtnPanel.add(resetBtn);

        openFileBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("Haskell/Concept file (.hs)", "hs"));
                chooser.setCurrentDirectory(lastDirUsed);
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    try {
                        if (!f.exists()) {
                            throw new FileNotFoundException();
                        }

                        conceptsText.setText(readFile(f));
                        updateLastDirUsed();
                    } catch (FileNotFoundException e1) {
                        // TODO Auto-generated catch block
                        JOptionPane.showMessageDialog(null, e1.getMessage(),
                                "File not found error", JOptionPane.ERROR_MESSAGE);
                    }

                }
            }

        });

        saveFileBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("Haskell/Concept file (.hs)", "hs"));
                chooser.setCurrentDirectory(lastDirUsed);
                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    if (!f.getName().endsWith(".hs")) {
                        f = new File(f.getAbsolutePath() + ".hs");
                    }
                    PrintStream concepts;
                    try {
                        concepts = new PrintStream(f);
                        String t = conceptsText.getText();
                        concepts.print(t);
                        concepts.close();
                        lastFileUsed = f;
                        changed = false;
                        updateLastDirUsed();
                    } catch (FileNotFoundException ex) {
                        // TODO Auto-generated catch block
                        ex.printStackTrace();
                    }
                }
            }

        });

        resetBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                conceptsText.setText(getDefaultText());
                lastFileUsed = null;
                changed = false;
            }

        });

        btnPanel.add(fileBtnPanel, BorderLayout.WEST);
    }

    private void createFinalBtnPanel() {
        JButton translateBtn = GUI.createDialogButton("Translate");
        JButton cancelBtn = GUI.createDialogButton("Cancel");
        JPanel finalBtnPanel = new JPanel();

        translateBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                translate = true;
                setVisible(false);
            }

        });

        cancelBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                translate = false;
                setVisible(false);
            }

        });

        finalBtnPanel.add(translateBtn);
        finalBtnPanel.add(cancelBtn);

        btnPanel.add(finalBtnPanel, BorderLayout.EAST);
    }

    private void updateLastDirUsed() {
        if (lastFileUsed != null) {
            String lastFilePath = lastFileUsed.getAbsolutePath();
            String lastFileName = lastFileUsed.getName();
            String lastFileDir = lastFilePath.replace(lastFileName, "");
            lastDirUsed = new File(lastFileDir);
        } else {
            lastDirUsed = Framework.getInstance().getWorkingDirectory();
        }
    }

    private String getDefaultText() {
        return "module Concept where\n"
                + "\n"
                + "import Tuura.Concept.STG\n"
                + "\n"
                + "circuit :: (Eq a) => a -> a -> a -> CircuitConcept a \n"
                + "circuit a b c = \n";
    }

    private String readFile(File f) throws FileNotFoundException {

        Scanner s = new Scanner(f);
        String fileText = new String();
        if (s.hasNextLine()) {
            do {
                fileText = fileText + s.nextLine() + "\n";
            } while (s.hasNextLine());
            s.close();
            conceptsText.setText(fileText);

            lastFileUsed = f;
            changed = false;
            //updateLastDirUsed();
        }
        return fileText == "" ? "" : fileText;
    }

    public boolean getTranslate() {
        return translate;
    }

    public File getFile() {
        if (lastFileUsed != null && !changed) {
            return lastFileUsed;
        } else {
            try {
                lastFileUsed = File.createTempFile("concepts", ".hs");
                PrintWriter p = new PrintWriter(lastFileUsed);
                p.print(conceptsText.getText());
                p.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return lastFileUsed;
    }

}

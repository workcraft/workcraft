package org.workcraft.plugins.plato.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.util.GUI;
import org.workcraft.util.DialogUtils;

@SuppressWarnings("serial")
public class PlatoWriterDialog extends JDialog {

    private static File lastFileUsed, lastDirUsed;

    private final JPanel content, btnPanel;
    private JTextArea conceptsText;
    private JCheckBox dotLayoutCheckBox;
    private boolean changed = false, translate = false;
    private final boolean fst;
    private static DefaultListModel<String> includeList = new DefaultListModel<String>();

    public PlatoWriterDialog(boolean fst) {
        super(Framework.getInstance().getMainWindow(), "Write and translate Concepts", ModalityType.APPLICATION_MODAL);

        this.fst = fst;

        content = new JPanel();
        content.setLayout(new BorderLayout());

        btnPanel = new JPanel();
        btnPanel.setLayout(new BorderLayout());

        createScrollPane();
        createFileBtnPanel();
        createFinalBtnPanel();

        content.add(btnPanel, BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(event -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
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
        conceptsText.setMargin(SizeHelper.getTextMargin());
        conceptsText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));
        conceptsText.setText(getDefaultComponentText());
        if (lastFileUsed != null && lastFileUsed.exists()) {
            try {
                conceptsText.setText(readFile(lastFileUsed));
            } catch (FileNotFoundException e) {
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
                // Do nothing
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Do nothing
            }

        });

        JScrollPane scrollPane = new JScrollPane(conceptsText);

        content.add(scrollPane, BorderLayout.CENTER);
    }

    private void createFileBtnPanel() {
        JButton openFileBtn = GUI.createDialogButton("Open file");
        JButton saveFileBtn = GUI.createDialogButton("Save to file");
        JButton componentBtn = GUI.createDialogButton("New component");
        JButton systemBtn = GUI.createDialogButton("New system");
        JButton includeBtn = GUI.createDialogButton("Included files");
        dotLayoutCheckBox = new JCheckBox("Use dot layout");
        JPanel fileBtnPanel = new JPanel();
        fileBtnPanel.add(openFileBtn);
        fileBtnPanel.add(saveFileBtn);
        fileBtnPanel.add(componentBtn);
        fileBtnPanel.add(systemBtn);
        fileBtnPanel.add(includeBtn);
        if (!fst) {
            fileBtnPanel.add(dotLayoutCheckBox);
        }

        openFileBtn.addActionListener(event -> {
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
                    DialogUtils.showError(e1.getMessage());
                }

            }
        });

        saveFileBtn.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Haskell/Concept file (.hs)", "hs"));
            chooser.setCurrentDirectory(lastDirUsed);
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (!file.getName().endsWith(".hs")) {
                    file = new File(file.getAbsolutePath() + ".hs");
                }
                PrintStream concepts;
                try {
                    concepts = new PrintStream(file);
                    String text = conceptsText.getText();
                    concepts.print(text);
                    concepts.close();
                    lastFileUsed = file;
                    changed = false;
                    updateLastDirUsed();
                } catch (FileNotFoundException ex) {
                }
            }
        });

        componentBtn.addActionListener(event -> {
            conceptsText.setText(getDefaultComponentText());
            lastFileUsed = null;
            changed = false;
        });

        systemBtn.addActionListener(event -> {
            conceptsText.setText(getDefaultSystemText());
            lastFileUsed = null;
            changed = false;
        });

        final PlatoIncludesDialog dialog = new PlatoIncludesDialog(this, lastDirUsed, includeList);

        includeBtn.addActionListener(event -> dialog.setVisible(true));

        btnPanel.add(fileBtnPanel, BorderLayout.WEST);
    }

    private void createFinalBtnPanel() {
        JButton translateBtn = GUI.createDialogButton("Translate");
        JButton cancelBtn = GUI.createDialogButton("Cancel");
        JPanel finalBtnPanel = new JPanel();

        translateBtn.addActionListener(event -> {
            translate = true;
            setVisible(false);
        });

        cancelBtn.addActionListener(event -> {
            translate = false;
            setVisible(false);
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

    private String getDefaultComponentText() {
        String result = "module Concept where\n" + "\n" + "import Tuura.Concept.";
        result = result + (fst ? "FSM" : "STG");
        result = result + "\n\n" + "component a b c = \n" + "  where";
        return result;
    }

    private String getDefaultSystemText() {
        String result = "module Concept where\n" + "\n" + "import Tuura.Concept.";
        result = result + (fst ? "FSM" : "STG") + "\n\n";
        result = result + "data Signal = A | B | C deriving (Bounded, Enum, Eq)";
        result = result + "\n\n" + "system = \n" + "  where";
        return result;
    }

    private String readFile(File file) throws FileNotFoundException {

        Scanner scanner = new Scanner(file);
        String fileText = new String();
        if (scanner.hasNextLine()) {
            do {
                fileText = fileText + scanner.nextLine() + "\n";
            } while (scanner.hasNextLine());
            scanner.close();
            conceptsText.setText(fileText);

            lastFileUsed = file;
            changed = false;
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
                lastFileUsed = File.createTempFile("plato-", ".hs");
                lastFileUsed.deleteOnExit();
                PrintWriter writer = new PrintWriter(lastFileUsed);
                writer.print(conceptsText.getText());
                writer.close();
            } catch (IOException e) {
            }
        }
        return lastFileUsed;
    }

    public boolean getDotLayoutState() {
        return dotLayoutCheckBox.isSelected();
    }

    public Object[] getIncludeList() {
        Object[] list = includeList.toArray();
        if ((list.length == 1) && (list[0].toString() == "")) {
            return new Object[0];
        }
        return list;
    }

    public Boolean isSystem() {
        if (conceptsText.getText().contains("system = ")) {
            return true;
        }
        return false;
    }

}

package org.workcraft.plugins.plato.gui;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.Scanner;

@SuppressWarnings("serial")
public class WriterDialog extends JDialog {

    private static final DefaultListModel<String> includeList = new DefaultListModel<>();
    private static File lastFileUsed;
    private static File lastDirUsed;

    private final JPanel content;
    private final JPanel btnPanel;
    private JTextArea conceptsText;
    private JCheckBox dotLayoutCheckBox;
    private boolean changed = false;
    private final boolean fst;
    private boolean modalResult;

    public WriterDialog(Window owner, boolean fst) {
        super(owner, "Write and translate Concepts", ModalityType.APPLICATION_MODAL);

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
        setLocationRelativeTo(owner);
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
        JButton openFileBtn = GuiUtils.createDialogButton("Open file");
        JButton saveFileBtn = GuiUtils.createDialogButton("Save to file");
        JButton componentBtn = GuiUtils.createDialogButton("New component");
        JButton systemBtn = GuiUtils.createDialogButton("New system");
        JButton includeBtn = GuiUtils.createDialogButton("Included files");
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

        openFileBtn.addActionListener(event -> openFileAction());
        saveFileBtn.addActionListener(event -> saveFileAction());
        componentBtn.addActionListener(event -> componentAction());
        systemBtn.addActionListener(event -> systemAction());

        final IncludesDialog dialog = new IncludesDialog(this, lastDirUsed, includeList);

        includeBtn.addActionListener(event -> dialog.setVisible(true));

        btnPanel.add(fileBtnPanel, BorderLayout.WEST);
    }

    private void openFileAction() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Haskell/Concept file (.hs)", "hs"));
        fc.setCurrentDirectory(lastDirUsed);
        if (DialogUtils.showFileOpener(fc)) {
            File file = fc.getSelectedFile();
            try {
                if (!file.exists()) {
                    throw new FileNotFoundException();
                }
                conceptsText.setText(readFile(file));
                updateLastDirUsed();
            } catch (FileNotFoundException e) {
                DialogUtils.showError(e.getMessage());
            }
        }
    }

    private void saveFileAction() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Haskell/Concept file (.hs)", "hs"));
        fc.setCurrentDirectory(lastDirUsed);
        if (DialogUtils.showFileSaver(fc)) {
            File file = fc.getSelectedFile();
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
    }

    private void componentAction() {
        conceptsText.setText(getDefaultComponentText());
        lastFileUsed = null;
        changed = false;
    }

    private void systemAction() {
        conceptsText.setText(getDefaultSystemText());
        lastFileUsed = null;
        changed = false;
    }

    private void createFinalBtnPanel() {
        JButton translateBtn = GuiUtils.createDialogButton("Translate");
        JButton cancelBtn = GuiUtils.createDialogButton("Cancel");
        JPanel finalBtnPanel = new JPanel();

        translateBtn.addActionListener(event -> {
            modalResult = true;
            setVisible(false);
        });

        cancelBtn.addActionListener(event -> {
            modalResult = false;
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
        return fileText;
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
        if ((list.length == 1) && (list[0].toString().isEmpty())) {
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

    public boolean reveal() {
        setVisible(true);
        return modalResult;
    }

}

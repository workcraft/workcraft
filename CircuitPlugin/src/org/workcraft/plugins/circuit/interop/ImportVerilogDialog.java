package org.workcraft.plugins.circuit.interop;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.dialogs.ModalDialog;
import org.workcraft.gui.properties.Properties;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyEditorTable;
import org.workcraft.plugins.circuit.verilog.VerilogModule;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.FileFilters;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

public class ImportVerilogDialog extends ModalDialog<Collection<VerilogModule>> {

    class ImportProperties implements Properties {
        private final List<PropertyDescriptor> properties = new LinkedList<>();
        private final Map<VerilogModule, String> map = new HashMap<>();

        @Override
        public Collection<PropertyDescriptor> getDescriptors() {
            return properties;
        }

        public void add(VerilogModule module, String fileName) {
            properties.add(new PropertyDeclaration<VerilogModule, String>(
                    module, fileName, String.class) {
                @Override
                public void setter(VerilogModule object, String value) {
                    map.put(object, value);
                }

                @Override
                public String getter(VerilogModule object) {
                    return map.get(object);
                }
            });
            map.put(module, fileName);
        }
    }

    private VerilogModule topModule;
    private File dir;
    private ImportProperties importProperties;

    public ImportVerilogDialog(Window owner, Collection<VerilogModule> modules) {
        super(owner, "Import Verilog hierarchy", modules);
    }

    @Override
    public JPanel createControlsPanel() {
        JPanel result = super.createControlsPanel();
        result.setLayout(GuiUtils.createBorderLayout());

        JComboBox<VerilogModule> topModuleCombo = new JComboBox<>();
        // First add action listener, then populate the ComboBox
        topModuleCombo.addActionListener(l -> {
            topModule = (VerilogModule) topModuleCombo.getSelectedItem();
            //modulesTableModel.fireTableDataChanged();
        });
        JPanel topModulePanel = GuiUtils.createLabeledComponent(topModuleCombo, "Top module:");

        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        dir = mainWindow.getLastDirectory();
        JTextField dirText = new JTextField(dir.getPath());
        JPanel dirPanel = GuiUtils.createLabeledComponent(dirText, "Directory:");
        JButton dirSelectButton = new JButton("Browse...");
        dirPanel.add(dirSelectButton, BorderLayout.EAST);
        dirSelectButton.addActionListener(l -> {
            JFileChooser fc = mainWindow.createOpenDialog("Select directory", false, false, null);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setCurrentDirectory(dir);
            if (fc.showDialog(mainWindow, "Open") == JFileChooser.APPROVE_OPTION) {
                dir = fc.getSelectedFile();
                dirText.setText(dir.getPath());
            }
        });

        PropertyEditorTable otherModulesTable = new PropertyEditorTable(
                "<html><b>Module name</b></html>",
                "<html><b>File name</b></html>");

        JScrollPane otherModulesScroll = new JScrollPane(otherModulesTable);
        importProperties = new ImportProperties();
        for (VerilogModule module : getUserData()) {
            topModuleCombo.addItem(module);
            importProperties.add(module, module.name + FileFilters.DOCUMENT_EXTENSION);
        }
        otherModulesTable.assign(importProperties);

        result.add(topModulePanel, BorderLayout.NORTH);
        result.add(otherModulesScroll, BorderLayout.CENTER);
        result.add(dirPanel, BorderLayout.SOUTH);

        return result;
    }

    public VerilogModule getTopModule() {
        return topModule;
    }

    public Map<VerilogModule, String> getModuleFileNames() {
        return importProperties.map;
    }

    public File getDirectory() {
        return dir;
    }

}

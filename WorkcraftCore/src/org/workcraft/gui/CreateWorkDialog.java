package org.workcraft.gui;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.PluginUtils;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.plugins.shared.CommonFavoriteSettings;
import org.workcraft.util.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CreateWorkDialog extends JDialog {

    class WorkTypeCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (!isSelected && (index % 2 != 0)) {
                Color color = getBackground();
                setBackground(Coloriser.colorise(color, Color.LIGHT_GRAY));
                setOpaque(true);
            }
            return this;
        }
    }

    private static final long serialVersionUID = 1L;
    private JList workTypeList;
    private JButton okButton;
    private JButton cancelButton;
    private int modalResult;

    public CreateWorkDialog(MainWindow owner) {
        super(owner);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setModal(true);
        setTitle("New work");
        initComponents();
        setMinimumSize(new Dimension(300, 200));
    }

    static class ListElement implements Comparable<ListElement> {
        public final ModelDescriptor descriptor;

        ListElement(ModelDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public String toString() {
            String name = descriptor.getDisplayName();
            if (CommonFavoriteSettings.getIsFavorite(name)) {
                name = "<html><b>" + name + "</b></html>";
            }
            return name;
        }

        @Override
        public int compareTo(ListElement other) {
            return descriptor.getDisplayName().compareTo(other.descriptor.getDisplayName());
        }
    }

    private void initComponents() {
        workTypeList = new JList(new DefaultListModel());
        workTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        workTypeList.setVisibleRowCount(0);
        workTypeList.setBorder(SizeHelper.getEmptyBorder());
        workTypeList.setCellRenderer(new WorkTypeCellRenderer());

        workTypeList.addListSelectionListener(event -> okButton.setEnabled(workTypeList.getSelectedIndex() != -1));
        workTypeList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ((e.getClickCount() == 2) && (workTypeList.getSelectedIndex() != -1)) {
                    ok();
                }
            }
        });

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        JCheckBox favoriteModelsCheckbox = new JCheckBox(getFavoriteModelsCheckboxText());
        favoriteModelsCheckbox.setToolTipText(getFavoriteModelsCheckboxTooltip());
        favoriteModelsCheckbox.addActionListener(event -> toggleFavoriteModelsCheckbox());
        favoriteModelsCheckbox.setSelected(CommonFavoriteSettings.getFilterFavorites());
        filterPanel.add(favoriteModelsCheckbox);

        fillModelList();
        JScrollPane modelScroll = getModelScroll();

        okButton = GUI.createDialogButton("OK");
        okButton.setEnabled(false);
        okButton.addActionListener(event -> ok());

        cancelButton = GUI.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> cancel());

        JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        buttonsPane.add(okButton);
        buttonsPane.add(cancelButton);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(SizeHelper.getEmptyBorder());
        setContentPane(contentPane);
        contentPane.add(filterPanel, BorderLayout.NORTH);
        contentPane.add(modelScroll, BorderLayout.CENTER);
        contentPane.add(buttonsPane, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);

        setKeyboardShortcuts();
    }

    private String getFavoriteModelsCheckboxTooltip() {
        return "<html>These can be configured in global settings:<br><i>Edit->Preferences...->Common->New work favorites</i></html>";
    }

    private void fillModelList() {
        PluginManager pm = Framework.getInstance().getPluginManager();
        ArrayList<ListElement> elements = new ArrayList<>();
        for (PluginInfo<? extends ModelDescriptor> plugin: pm.getPlugins(ModelDescriptor.class)) {
            ModelDescriptor modelDescriptor = plugin.newInstance();
            String displayName = modelDescriptor.getDisplayName();
            if (!CommonFavoriteSettings.getFilterFavorites() || CommonFavoriteSettings.getIsFavorite(displayName)) {
                elements.add(new ListElement(modelDescriptor));
            }
        }
        Collections.sort(elements);
        DefaultListModel listModel = (DefaultListModel) workTypeList.getModel();
        listModel.clear();
        for (ListElement element: elements) {
            listModel.addElement(element);
        }
    }

    private JScrollPane getModelScroll() {
        int displayNameLength = 10;
        Collection<String> sortedDisplayNames = PluginUtils.getSortedModelDisplayNames();
        for (String displayName: sortedDisplayNames) {
            displayNameLength = Math.max(displayNameLength, displayName.length());
        }
        int width = SizeHelper.getBaseFontSize() * displayNameLength;
        int height = SizeHelper.getListRowSize() * sortedDisplayNames.size();

        JScrollPane result = new JScrollPane();
        result.setPreferredSize(new Dimension(width, height));
        result.setViewportView(workTypeList);
        return result;
    }

    private String getFavoriteModelsCheckboxText() {
        Collection<String> names = PluginUtils.getSortedModelDisplayNames();
        long allCount = names.size();
        long favoriteCount = names.stream().filter(name -> CommonFavoriteSettings.getIsFavorite(name)).count();
        return "<html>Filter <b>favorite</b> model types (" + favoriteCount + " out of " + allCount + ")</html>";
    }

    private void toggleFavoriteModelsCheckbox() {
        boolean filterFavoritesState = CommonFavoriteSettings.getFilterFavorites();
        CommonFavoriteSettings.setFilterFavorites(!filterFavoritesState);
        fillModelList();
    }

    private void setKeyboardShortcuts() {
        getRootPane().registerKeyboardAction(event -> ok(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(event -> cancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Assign number keys as shortcuts for model types (the order is 1234567890).
        for (int i = 0; i <= 9; i++) {
            final int index = i;
            int keyCode = (i < 9) ? KeyEvent.VK_1 + i : KeyEvent.VK_0;
            getRootPane().registerKeyboardAction(event -> ok(index),
                    KeyStroke.getKeyStroke(keyCode, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
    }

    private void ok(int index) {
        if (index < workTypeList.getModel().getSize()) {
            workTypeList.setSelectedIndex(index);
            ok();
        }
    }

    private void ok() {
        if (okButton.isEnabled()) {
            modalResult = 1;
            setVisible(false);
        }
    }

    private void cancel() {
        if (cancelButton.isEnabled()) {
            modalResult = 0;
            setVisible(false);
        }
    }

    public int getModalResult() {
        return modalResult;
    }

    public ModelDescriptor getSelectedModel() {
        return ((ListElement) workTypeList.getSelectedValue()).descriptor;
    }

}

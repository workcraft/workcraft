package org.workcraft.gui.dialogs;

import org.workcraft.Framework;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.builtin.settings.FavoriteCommonSettings;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class CreateWorkDialog extends ModalDialog<Void> {

    public static final String SHORTCUT_PLACEHOLDER = "SHORTCUT";

    class WorkTypeCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (!isSelected && (index % 2 != 0)) {
                Color color = getBackground();
                setBackground(ColorUtils.colorise(color, Color.LIGHT_GRAY));
                setOpaque(true);
            }
            String shortcut = KeyEvent.getKeyText(getKeyCode(index));
            String text = getText().replace(SHORTCUT_PLACEHOLDER, shortcut);
            setText(text);
            return this;
        }
    }

    static class ListElement implements Comparable<ListElement> {
        public final ModelDescriptor descriptor;

        ListElement(ModelDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public String toString() {
            String name = descriptor.getDisplayName();
            String shortcut = "<sup><font size='-2' color='#888888'>" + SHORTCUT_PLACEHOLDER + "</font></sup> ";
            if (FavoriteCommonSettings.getIsFavorite(name)) {
                name = "<b>" + name + "</b>";
            }
            return "<html>" + shortcut + name + "</html>";
        }

        @Override
        public int compareTo(ListElement other) {
            return descriptor.getDisplayName().compareTo(other.descriptor.getDisplayName());
        }
    }

    private JList<?> workTypeList;

    public CreateWorkDialog(MainWindow owner) {
        super(owner, "New work", null);

        // Assign number keys as shortcuts for model types (the order is 1234567890A..Z).
        for (int i = 0; i < 36; i++) {
            final int index = i;
            int keyCode = getKeyCode(i);
            getRootPane().registerKeyboardAction(event -> okAction(index),
                    KeyStroke.getKeyStroke(keyCode, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
        setOkEnableness(false);
    }

    private int getKeyCode(int index) {
        if (index < 9) return KeyEvent.VK_1 + index;
        if (index == 9) return KeyEvent.VK_0;
        return KeyEvent.VK_A + index - 10;
    }

    @Override
    public JPanel createContentPanel() {
        JPanel result = super.createContentPanel();
        result.setLayout(GuiUtils.createBorderLayout());

        workTypeList = new JList<>(new DefaultListModel<>());
        workTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        workTypeList.setVisibleRowCount(0);
        workTypeList.setBorder(GuiUtils.getEmptyBorder());
        workTypeList.setCellRenderer(new WorkTypeCellRenderer());

        workTypeList.addListSelectionListener(event -> setOkEnableness(workTypeList.getSelectedIndex() != -1));
        workTypeList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ((e.getClickCount() == 2) && (workTypeList.getSelectedIndex() != -1)) {
                    okAction();
                }
            }
        });

        JCheckBox favoriteModelsCheckbox = new JCheckBox(getFavoriteModelsCheckboxText());
        favoriteModelsCheckbox.setToolTipText(getFavoriteModelsCheckboxTooltip());
        favoriteModelsCheckbox.addActionListener(event -> toggleFavoriteModelsCheckbox());
        favoriteModelsCheckbox.setSelected(FavoriteCommonSettings.getFilterFavorites());
        fillModelList();

        result.add(favoriteModelsCheckbox, BorderLayout.NORTH);
        result.add(getModelScroll(), BorderLayout.CENTER);
        return result;
    }

    private String getFavoriteModelsCheckboxTooltip() {
        return "<html>These can be configured in global settings:<br><i>Edit->Preferences...->Common->New work favorites</i></html>";
    }

    private void fillModelList() {
        PluginManager pm = Framework.getInstance().getPluginManager();
        List<ModelDescriptor> descriptors = pm.getSortedModelDescriptors();
        ArrayList<ListElement> elements = getModelList(descriptors, true);
        if (!FavoriteCommonSettings.getFilterFavorites()) {
            ArrayList<ListElement> otherElements = getModelList(descriptors, false);
            elements.addAll(otherElements);
        }

        @SuppressWarnings("unchecked")
        DefaultListModel<ListElement> listModel = (DefaultListModel<ListElement>) workTypeList.getModel();
        listModel.clear();
        for (ListElement element : elements) {
            listModel.addElement(element);
        }
    }

    private ArrayList<ListElement> getModelList(List<ModelDescriptor> descriptors, boolean favorite) {
        ArrayList<ListElement> result = new ArrayList<>();
        for (ModelDescriptor descriptor : descriptors) {
            String displayName = descriptor.getDisplayName();
            if (FavoriteCommonSettings.getIsFavorite(displayName) == favorite) {
                result.add(new ListElement(descriptor));
            }
        }
        return result;
    }

    private JScrollPane getModelScroll() {
        int displayNameLength = 10;
        PluginManager pm = Framework.getInstance().getPluginManager();
        List<ModelDescriptor> descriptors = pm.getSortedModelDescriptors();
        for (ModelDescriptor descriptor : descriptors) {
            String displayName = descriptor.getDisplayName();
            displayNameLength = Math.max(displayNameLength, displayName.length());
        }
        int width = SizeHelper.getBaseFontSize() * displayNameLength;
        int height = SizeHelper.getListRowSize() * descriptors.size();

        JScrollPane result = new JScrollPane();
        result.setPreferredSize(new Dimension(width, height));
        result.setViewportView(workTypeList);
        return result;
    }

    private String getFavoriteModelsCheckboxText() {
        PluginManager pm = Framework.getInstance().getPluginManager();
        List<ModelDescriptor> descriptors = pm.getSortedModelDescriptors();
        long allCount = descriptors.size();
        long favoriteCount = descriptors.stream()
                .filter(o -> FavoriteCommonSettings.getIsFavorite(o.getDisplayName()))
                .count();

        return "<html>Filter <b>favorite</b> model types (" + favoriteCount + " out of " + allCount + ")</html>";
    }

    private void toggleFavoriteModelsCheckbox() {
        boolean filterFavoritesState = FavoriteCommonSettings.getFilterFavorites();
        FavoriteCommonSettings.setFilterFavorites(!filterFavoritesState);
        fillModelList();
    }

    private void okAction(int index) {
        if (index < workTypeList.getModel().getSize()) {
            workTypeList.setSelectedIndex(index);
            okAction();
        }
    }

    public ModelDescriptor getSelectedModel() {
        return ((ListElement) workTypeList.getSelectedValue()).descriptor;
    }

}

package org.workcraft.gui.tools;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.util.function.BiConsumer;

public class TableRowTransferHandler extends TransferHandler {

    private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class, "Integer Row Index");
    private final JTable table;
    private final BiConsumer<Integer, Integer> rowMover;

    public TableRowTransferHandler(JTable table,  BiConsumer<Integer, Integer> rowMover) {
        this.table = table;
        this.rowMover = rowMover;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        return new DataHandler(table.getSelectedRow(), localObjectFlavor.getMimeType());
    }

    @Override
    public boolean canImport(TransferSupport info) {
        boolean result = (info.getComponent() == table) && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
        table.setCursor(result ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
        return result;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferSupport info) {
        JTable target = (JTable) info.getComponent();
        JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
        int toRow = dl.getRow();
        int lastRow = table.getModel().getRowCount();
        if ((toRow < 0) || (toRow > lastRow)) {
            toRow = lastRow;
        }
        target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        try {
            int fromRow = (Integer) info.getTransferable().getTransferData(localObjectFlavor);
            if (toRow > fromRow) {
                toRow--;
            }
            if ((fromRow != -1) && (fromRow != toRow) && (rowMover != null)) {
                rowMover.accept(fromRow, toRow);
                target.getSelectionModel().addSelectionInterval(toRow, toRow);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable t, int act) {
        if ((act == TransferHandler.MOVE) || (act == TransferHandler.NONE)) {
            table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

}

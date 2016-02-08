package org.workcraft.gui;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;

public class ExceptionDialog {

    public static void show(Component owner, Throwable cause) {
        final StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));

        JOptionPane.showMessageDialog(owner, "Exception of type " + cause.getClass().getCanonicalName() + ": \n" + cause.getMessage() + "\n\n" + writer.toString());
    }

}

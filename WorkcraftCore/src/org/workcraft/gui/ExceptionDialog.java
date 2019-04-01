package org.workcraft.gui;

import org.workcraft.utils.DialogUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionDialog {

    public static void show(Throwable cause) {
        final StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        String name = cause.getClass().getName();
        DialogUtils.showMessage("Exception of type " + name + ": \n"
                + cause.getMessage() + "\n\n" + writer.toString());
    }

}

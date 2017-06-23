package org.workcraft.gui;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.workcraft.util.DialogUtils;

public class ExceptionDialog {

    public static void show(Throwable cause) {
        final StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        String name = cause.getClass().getCanonicalName();
        DialogUtils.showMessage("Exception of type " + name + ": \n"
                + cause.getMessage() + "\n\n" + writer.toString());
    }

}

package org.workcraft.gui.actions;

import org.workcraft.Framework;

public abstract class ScriptedAction extends Action {

    public static final String tryOperation(String operation) {
        return "try\n" +
                "{\n" +
                operation + "\n" +
                "}\n" +
                "catch (err)\n" +
                "{\n" +
                "  if (!(err.javaException instanceof Packages.org.workcraft.exceptions.OperationCancelledException)) {\n" +
                "    throw err.javaException;\n" +
                "  }\n" +
                "}";
    }

    protected abstract String getScript();

    @Override
    public void run() {
        if (getScript() != null) {
            final Framework framework = Framework.getInstance();
            framework.execJavaScript(getScript());
        }
    }

    public String getUndoScript() {
        return null;
    }

    public String getRedoScript() {
        return null;
    }

}

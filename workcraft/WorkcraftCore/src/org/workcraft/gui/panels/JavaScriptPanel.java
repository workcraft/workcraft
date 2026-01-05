package org.workcraft.gui.panels;

import org.mozilla.javascript.Context;
import org.workcraft.Framework;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class JavaScriptWindow extends JPanel {
    private static final String SCRIPT_SUBMIT = "script-submit";
    private static final KeyStroke ctrlEnter = KeyStroke.getKeyStroke("ctrl ENTER");

    private final JTextPane txtScript;
    private boolean isInitState;

    private final class ScriptSubmitAction extends AbstractAction {
        private final class ThreadExtension extends Thread {
            @Override
            public void run() {
                try {
                    final Framework framework = Framework.getInstance();
                    Object result = framework.execJavaScript(txtScript.getText());
                    Context.enter();
                    String out = Context.toString(result);
                    Context.exit();
                    if (!"undefined".equals(out)) {
                        System.out.println(out);
                    }
                    txtScript.setText("");
                } catch (org.mozilla.javascript.WrappedException e) {
                    Throwable we = e.getWrappedException();
                    System.err.println(we.getClass().getName() + ' ' + we.getMessage());
                } catch (org.mozilla.javascript.RhinoException e) {
                    System.err.println(e.getMessage());
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent action) {
            if (!txtScript.getText().isEmpty()) {
                new ThreadExtension().start();
            }
        }
    }

    public JavaScriptWindow() {
        txtScript = new JTextPane();
        txtScript.setBorder(GuiUtils.getEmptyBorder());
        txtScript.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                String text = txtScript.getText().trim();
                if (text.isEmpty()) {
                    resetScript();
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                if (isInitState) {
                    isInitState = false;
                    txtScript.setText("");
                }
            }
        });

        // Set action for Ctrl-Enter -- submit script
        InputMap input = txtScript.getInputMap();
        ActionMap actions = txtScript.getActionMap();
        input.put(ctrlEnter, SCRIPT_SUBMIT);
        actions.put(SCRIPT_SUBMIT, new ScriptSubmitAction());

        JPanel panelInput = new JPanel();
        panelInput.setLayout(new BorderLayout());
        panelInput.add(txtScript, BorderLayout.CENTER);
        panelInput.setMinimumSize(new Dimension(100, 100));

        setLayout(new BorderLayout());
        add(panelInput, BorderLayout.CENTER);
        resetScript();
    }

    private void resetScript() {
        isInitState = true;
        txtScript.setText("// Write a script and press " + DesktopApi.getMenuKeyName() + "-Enter to execute it.");
    }

}

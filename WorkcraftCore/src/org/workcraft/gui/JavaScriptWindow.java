package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import org.mozilla.javascript.Context;
import org.syntax.jedit.JEditTextArea;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;
import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;

@SuppressWarnings("serial")
public class JavaScriptWindow extends JPanel {

    private final JEditTextArea txtScript;
    private boolean isInitState;

    public JavaScriptWindow() {
        txtScript = new JEditTextArea();
        txtScript.setBorder(SizeHelper.getEmptyBorder());
        txtScript.setTokenMarker(new JavaScriptTokenMarker());
        txtScript.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (DesktopApi.isMenuKeyDown(e))) {
                    execScript();
                }
            }
        });
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

        JPanel panelInput = new JPanel();
        panelInput.setLayout(new BorderLayout());
        panelInput.add(txtScript, BorderLayout.CENTER);
        panelInput.setMinimumSize(new Dimension(100, 100));

        setLayout(new BorderLayout());
        add(panelInput, BorderLayout.CENTER);
        resetScript();
    }

    public void execScript() {
        if (txtScript.getText().length() > 0) {
            try {
                final Framework framework = Framework.getInstance();
                Object result = framework.execJavaScript(txtScript.getText());

                Context.enter();
                String out = Context.toString(result);
                Context.exit();
                if (!out.equals("undefined")) {
                    System.out.println(out);
                }
                resetScript();
            } catch (org.mozilla.javascript.WrappedException e) {
                Throwable we = e.getWrappedException();
                System.err.println(we.getClass().getName() + " " + we.getMessage());
            } catch (org.mozilla.javascript.RhinoException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void resetScript() {
        isInitState = true;
        txtScript.setText("// Write a script and press " + DesktopApi.getMenuKeyMaskName() + "-Enter to execute it.");
    }

}

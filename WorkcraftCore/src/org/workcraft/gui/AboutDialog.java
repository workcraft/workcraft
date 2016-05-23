package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.workcraft.Info;
import org.workcraft.util.GUI;

public class AboutDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    public AboutDialog(final MainWindow owner) {
        super(owner);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setModal(true);
        setTitle("About");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ok();
            }
        });

        Dimension parentSize = owner.getSize();
        this.setSize(600, 320);
        Dimension mySize = getSize();
        this.setLocation(((parentSize.width - mySize.width) / 2) + 0, ((parentSize.height - mySize.height) / 2) + 0);

        owner.getLocationOnScreen();

        BufferedImage logoImage = null;
        try {
            logoImage = GUI.loadImageFromResource("images/logo.png");
        } catch (IOException e) {
            logoImage = null;
        }

        JLabel logoLabel;
        if (logoImage != null) {
            logoLabel = new JLabel(new ImageIcon(logoImage), SwingConstants.CENTER);
        } else {
            logoLabel = new JLabel("Workcraft", SwingConstants.CENTER);
            Font font = logoLabel.getFont();
            logoLabel.setFont(font.deriveFont(72.0f));
        }
        logoLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JEditorPane infoPane = new JEditorPane();
        infoPane.setFocusable(false);

        infoPane.setContentType("text/html");

        infoPane.setEditable(false);
        infoPane.setOpaque(false);

        String homepage = Info.getHomepage();
        infoPane.setText("<p center>" + Info.getFullTitle() + "</p>"
                + "<p center>" + Info.getCopyright() + "</p>"
                + "<p center><a href='" + homepage + "'>" + homepage + "</a></p>");

        infoPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent event) {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
                    try {
                        URI uri = event.getURL().toURI();
                        DesktopApi.browse(uri);
                    } catch (URISyntaxException e) {
                        System.out.println(e);
                    }
                }
            }
        });

        JButton okButton = new JButton();
        okButton.setPreferredSize(new Dimension(100, 25));
        okButton.setText("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });

        JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsPane.add(okButton);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(10, 10));
        contentPane.add(logoLabel, BorderLayout.NORTH);
        contentPane.add(infoPane, BorderLayout.CENTER);
        contentPane.add(buttonsPane, BorderLayout.SOUTH);
        setContentPane(contentPane);
        getRootPane().setDefaultButton(okButton);

        getRootPane().registerKeyboardAction(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ok();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ok();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

    }

    private void ok() {
        setVisible(false);
    }

}

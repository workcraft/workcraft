package org.workcraft.plugins.son.test;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

public class OptionComponent extends JPanel {
    String text;
    GeneralPath arrow;
    int left;
    boolean firstTime = true;
    boolean isSelected = false;

    OptionComponent(String text) {
        this.text = text;
        setBackground(UIManager.getColor("Menu.background"));
        setForeground(UIManager.getColor("Menu.foreground"));
        setOpaque(false);
        addMouseListener(ml);
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        int h = getHeight();
        Font font = UIManager.getFont("Menu.font");
        g2.setFont(font);
        FontRenderContext frc = g2.getFontRenderContext();
        Rectangle2D r = font.getStringBounds(text, frc);
        float sx = 5f;
        float sy = (float)((h + r.getHeight())/2) -
                       font.getLineMetrics(text, frc).getDescent();
        g2.drawString(text, sx, sy);
        double x = sx + r.getWidth() + sx;
        if(isSelected) {
            g2.setPaint(Color.gray);
            g2.draw(new Line2D.Double(x, 0, x, h));
            g2.setPaint(Color.white);
            g2.draw(new Line2D.Double(x+1, 0, x+1, h));
            g2.setPaint(Color.gray);
            g2.draw(new Rectangle2D.Double(0, 0, getSize().width-1, h-1));
        }
        float ax = (float)(x + sx);
        if(firstTime)
            createArrow(ax, h);
        g2.setPaint(UIManager.getColor("Menu.foreground"));
        g2.fill(arrow);
        ax += 10f + sx;
        if(firstTime) {
            setSize((int)ax, h);         // initial sizing
            setPreferredSize(getSize());
            setMaximumSize(getSize());   // resizing behavior
            left = (int)x + 1;           // for mouse listener
            firstTime = false;
        }
    }

    private void createArrow(float x, int h) {
        arrow = new GeneralPath();
        arrow.moveTo(x, h/3f);
        arrow.lineTo(x + 10f, h/3f);
        arrow.lineTo(x + 5f, h*2/3f);
        arrow.closePath();


    }

    class MenuArrowIcon implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setPaint(Color.BLACK);
            g2.translate(x, y);
            g2.drawLine( 2, 3, 6, 3 );
            g2.drawLine( 3, 4, 5, 4 );
            g2.drawLine( 4, 5, 4, 5 );
            g2.translate(-x, -y);
        }
        @Override public int getIconWidth()  { return 10; }
        @Override public int getIconHeight() { return 10; }
    }

    private JMenuBar getMenuBar() {
        JMenu menu = new JMenu("menu");
        JMenuItem item = new JMenuItem("menu item");
        menu.add(item);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        menuBar.add(this);
        return menuBar;
    }

    public static void main(String[] args) {
        OptionComponent test = new OptionComponent("draw");
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setJMenuBar(test.getMenuBar());
        f.getContentPane();
        f.setSize(200,100);
        f.setLocation(200,200);
        f.setVisible(true);
    }

    private MouseListener ml = new MouseAdapter() {
        JPopupMenu popupMenu = getPopupMenu();

        public void mousePressed(MouseEvent e) {
            if(e.getX() <= left)
                System.out.println("Use default drawing tool.");
            else
                popupMenu.show(OptionComponent.this, 0, getHeight());
        }

        public void mouseEntered(MouseEvent e) {
            isSelected = true;
            repaint();
        }

        public void mouseExited(MouseEvent e) {
            isSelected = false;
            repaint();
        }
    };

    private JPopupMenu getPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        String[] ids = { "pencil", "brush", "fill" };
        for(int j = 0; j < ids.length; j++) {
            JMenuItem item = new JMenuItem(ids[j]);
            popupMenu.add(item);
        }
        return popupMenu;
    }
}
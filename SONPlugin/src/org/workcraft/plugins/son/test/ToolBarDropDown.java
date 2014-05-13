package org.workcraft.plugins.son.test;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ToolBarDropDown {

  public static void main(String[] args) {
    final Display display = new Display();
    final Shell shell = new Shell(display);
    final ToolBar toolBar = new ToolBar(shell, SWT.NONE);
    final Menu menu = new Menu(shell, SWT.POP_UP);
    for (int i = 0; i < 8; i++) {
      MenuItem item = new MenuItem(menu, SWT.PUSH);
      item.setText("Item " + i);
    }
    final ToolItem item = new ToolItem(toolBar, SWT.DROP_DOWN);
    item.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        if (event.detail == SWT.ARROW) {
          Rectangle rect = item.getBounds();
          Point pt = new Point(rect.x, rect.y + rect.height);
          pt = toolBar.toDisplay(pt);
          menu.setLocation(pt.x, pt.y);
          menu.setVisible(true);
        }
      }
    });
    toolBar.pack();
    shell.pack();
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    menu.dispose();
    display.dispose();
  }
}
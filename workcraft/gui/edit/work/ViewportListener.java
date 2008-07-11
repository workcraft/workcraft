package org.workcraft.gui.edit.work;

/**
 * The <code>ViewportListener</code> interface defines methods for classes that wish
 * to be notified of the changes in viewport parameters.
 * @author Ivan Poliakov
 *
 */
public interface ViewportListener {
	/**
	 * Called when viewport parameters (width, height or position) change.
	 * @param sender
	 * The viewport that has sent the notification.
	 */
	public void shapeChanged (Viewport sender);


	/**
	 * Called when viewport parameters (pan or zoom) change.
	 * @param sender
	 * The viewport that has sent the notification
	 */
	public void viewChanged (Viewport sender);
}
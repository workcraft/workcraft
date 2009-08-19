/**
 *
 */
package org.workcraft.dom.visual;

import java.util.Set;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.framework.EventListener0;
import org.workcraft.framework.EventListener1;
import org.workcraft.framework.EventListener2;

public interface VisualModelEventDispatcher {
	public void addComponentAddedListener(EventListener1<VisualComponent> listener);
	public void removeComponentAddedListener(EventListener1<VisualComponent> listener);

	public void addComponentRemovedListener(EventListener1<VisualComponent> listener);
	public void removeComponentRemovedListener(EventListener1<VisualComponent> listener);

	public void addComponentPropertyChangedListener(EventListener2<VisualComponent, String> listener);
	public void removeComponentPropertyChangedListener(EventListener2<VisualComponent, String> listener);

	public void addConnectionAddedListener(EventListener1<VisualConnection> listener);
	public void removeConnectionAddedListener(EventListener1<VisualConnection> listener);

	public void addConnectionRemovedListener(EventListener1<VisualConnection> listener);
	public void removeConnectionRemovedListener(EventListener1<VisualConnection> listener);

	public void addConnectionPropertyChangedListener(EventListener2<VisualConnection, String> listener);
	public void removeConnectionPropertyChangedListener(EventListener2<VisualConnection, String> listener);

	public void addLayoutChangedListener(EventListener0 listener);
	public void removeLayoutChangedListener(EventListener0 listener);

	public void addSelectionChangedListener(EventListener1<Set<VisualNode>> listener);
	public void removeSelectionChangedListener(EventListener1<Set<VisualNode>> listener);
}
/**
 *
 */
package org.workcraft.framework;

public interface EventListener2<T1, T2> {
	public void eventFired(T1 sender, T2 arg);
}
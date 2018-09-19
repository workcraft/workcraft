package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.visual.connections.VisualConnection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConnectionUtils {

    public static HashMap<VisualConnection, VisualConnection.ScaleMode> replaceConnectionScaleMode(
            Collection<VisualConnection> connections, VisualConnection.ScaleMode mode) {

        HashMap<VisualConnection, VisualConnection.ScaleMode> connectionToScaleModeMap = new HashMap<>();
        for (VisualConnection vc: connections) {
            connectionToScaleModeMap.put(vc, vc.getScaleMode());
            vc.setScaleMode(mode);
        }
        return connectionToScaleModeMap;
    }

    public static void restoreConnectionScaleMode(HashMap<VisualConnection, VisualConnection.ScaleMode> connectionToScaleModeMap) {
        if (connectionToScaleModeMap != null) {
            for (Map.Entry<VisualConnection, VisualConnection.ScaleMode> entry : connectionToScaleModeMap.entrySet()) {
                VisualConnection vc = entry.getKey();
                VisualConnection.ScaleMode scaleMode = entry.getValue();
                vc.setScaleMode(scaleMode);
            }
        }
    }

}

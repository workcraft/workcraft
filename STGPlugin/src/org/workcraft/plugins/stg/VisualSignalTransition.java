/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.stg;

import java.awt.Color;
import java.awt.event.KeyEvent;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.shared.CommonSignalSettings;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_T)
@DisplayName("Signal Transition")
@SVGIcon("images/icons/svg/signal-transition.svg")
public class VisualSignalTransition extends VisualNamedTransition implements StateObserver {

    public VisualSignalTransition(SignalTransition signalTransition) {
        super(signalTransition);
    }

    @Override
    public String getName() {
        String signalName = getReferencedTransition().getSignalName();
        if (signalName == null) {
            signalName = "";
        }
        final StringBuffer result = new StringBuffer(signalName);
        switch (getReferencedTransition().getDirection()) {
        case PLUS:
            result.append("+");
            break;
        case MINUS:
            result.append("-");
            break;
        case TOGGLE:
            if (CommonSignalSettings.getShowToggle()) {
                result.append("~");
            }
            break;
        }
        return result.toString();
    }

    @Override
    public Color getColor() {
        switch (getType()) {
        case INPUT:    return CommonSignalSettings.getInputColor();
        case OUTPUT:   return CommonSignalSettings.getOutputColor();
        case INTERNAL: return CommonSignalSettings.getInternalColor();
        default:       return CommonSignalSettings.getDummyColor();
        }
    }

    @NoAutoSerialisation
    public SignalTransition getReferencedTransition() {
        return (SignalTransition) getReferencedComponent();
    }

    @NoAutoSerialisation
    public void setType(SignalTransition.Type type) {
        getReferencedTransition().setSignalType(type);
//        updateRenderedText();
    }

    @NoAutoSerialisation
    public SignalTransition.Type getType() {
        return getReferencedTransition().getSignalType();
    }

    @NoAutoSerialisation
    public void setDirection(SignalTransition.Direction direction) {
        getReferencedTransition().setDirection(direction);
        updateRenderedText();
    }

    @NoAutoSerialisation
    public SignalTransition.Direction getDirection() {
        return getReferencedTransition().getDirection();
    }

    @NoAutoSerialisation
    public void setSignalName(String name) {
        getReferencedTransition().setSignalName(name);
        updateRenderedText();
    }

    @NoAutoSerialisation
    public String getSignalName() {
        return getReferencedTransition().getSignalName();
    }

    @NoAutoSerialisation
    public Type getSignalType() {
        return getReferencedTransition().getSignalType();
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        // FIXME: type, direction and name of the signal cannot be copied as it breaks template functionality
//        if (src instanceof VisualSignalTransition) {
//            VisualSignalTransition srcSignalTransition = (VisualSignalTransition)src;
//            setType(srcSignalTransition.getType());
//            setDirection(srcSignalTransition.getDirection());
//        }
    }

    @Override
    public void mixStyle(Stylable... srcs) {
        super.mixStyle(srcs);
        // Type priority: OUTPUT > INPUT > INTERNAL
        boolean foundOutput = false;
        boolean foundInput = false;
        boolean foundInternal = false;
        // Direction priority: TOGGLE > PLUS == MINUS
        boolean foundToggle = false;
        boolean foundPlus = false;
        boolean foundMinus = false;
        for (Stylable src: srcs) {
            if (src instanceof VisualSignalTransition) {
                VisualSignalTransition srcSignalTransition = (VisualSignalTransition) src;
                if (srcSignalTransition.getType() == Type.OUTPUT) {
                    foundOutput = true;
                }
                if (srcSignalTransition.getType() == Type.INPUT) {
                    foundInput = true;
                }
                if (srcSignalTransition.getType() == Type.INTERNAL) {
                    foundInternal = true;
                }
                if (srcSignalTransition.getDirection() == Direction.TOGGLE) {
                    foundToggle = true;
                }
                if (srcSignalTransition.getDirection() == Direction.PLUS) {
                    foundPlus = true;
                }
                if (srcSignalTransition.getDirection() == Direction.MINUS) {
                    foundMinus = true;
                }
            }
        }
        if (foundOutput) {
            setType(Type.OUTPUT);
        } else if (foundInput) {
            setType(Type.INPUT);
        } else if (foundInternal) {
            setType(Type.INTERNAL);
        }
        if (foundToggle || (foundPlus && foundMinus)) {
            setDirection(Direction.TOGGLE);
        } else if (foundPlus) {
            setDirection(Direction.PLUS);
        } else if (foundMinus) {
            setDirection(Direction.MINUS);
        }
    }

}

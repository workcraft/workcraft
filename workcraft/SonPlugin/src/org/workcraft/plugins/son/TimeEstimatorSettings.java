package org.workcraft.plugins.son;

import org.workcraft.plugins.son.util.Interval;

public class TimeEstimatorSettings {

    private Interval duration;
    private int position;

    public TimeEstimatorSettings() {
        this(new Interval(0, 0), 0);
    }

    public TimeEstimatorSettings(Interval duration, int position) {
        this.duration = duration;
        this.position = position;
    }

    public Interval getDuration() {
        return duration;
    }

    public void setDuration(Interval duration) {
        this.duration = duration;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}

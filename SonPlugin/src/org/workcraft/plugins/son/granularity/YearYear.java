package org.workcraft.plugins.son.granularity;

public class YearYear extends AbstractTimeGranularity {

    @Override
    public Integer plusTD(int time, int duration) {
        int result = time + duration;
        if (result > 9999) {
            return 9999;
        }
        return result;
    }

    @Override
    public Integer subtractTD(Integer time, Integer duration) {
        int result = time - duration;
        if (result < 0000) {
            return 0000;
        }
        return result;
    }

    @Override
    public Integer subtractTT(Integer start, Integer end) {
        int result = end - start;
        if (result < 0000) {
            return 0000;
        }
        return result;
    }

}

package function.external.base;

import global.Data;

/**
 *
 * @author nick
 */
public class VariantAFCommand {

    public boolean isList = false;
    public boolean isInclude = false;
    public float maxAF = Data.NO_FILTER;
    public float minAF = Data.NO_FILTER;
    public float maxMAF = Data.NO_FILTER;
    public float minMAF = Data.NO_FILTER;

    public boolean isAFValid(float value) {
        return isMaxAFValid(value)
                && isMinAFValid(value)
                && isMaxMAFValid(value)
                && isMinMAFValid(value);

    }

    public boolean isAFValid(float max, float min) {
        return isMaxAFValid(max)
                && isMinAFValid(max)
                && isMaxMAFValid(max, min)
                && isMinMAFValid(max, min);

    }

    private boolean isMaxAFValid(float value) {
        if (maxAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxAF
                || value == Data.FLOAT_NA;
    }

    private boolean isMinAFValid(float value) {
        if (minAF == Data.NO_FILTER) {
            return true;
        }

        return value > minAF;
    }

    private boolean isMaxMAFValid(float value) {
        if (maxMAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxMAF
                || value >= (1 - maxMAF)
                || value == Data.FLOAT_NA;
    }

    private boolean isMinMAFValid(float value) {
        if (minMAF == Data.NO_FILTER) {
            return true;
        }

        return value > minMAF
                && value < (1 - minMAF);
    }

    private boolean isMaxMAFValid(float max, float min) {
        if (maxMAF == Data.NO_FILTER) {
            return true;
        }
        // maxAF <= cutoff or minAF >= 1 - cutoff
        return max <= maxMAF
                || min >= (1 - maxMAF)
                || max == Data.FLOAT_NA
                || min == Data.FLOAT_NA;
    }

    private boolean isMinMAFValid(float max, float min) {
        if (minMAF == Data.NO_FILTER) {
            return true;
        }

        // maxAF > cutoff and minAF < 1- cutoff
        return max > minMAF
                && min < (1 - minMAF);
    }
}

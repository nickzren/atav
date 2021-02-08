package function.external.gnomad;

import function.external.base.VariantAFCommand;
import global.Data;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author nick
 */
public class GnomADCommand extends VariantAFCommand {

    public static boolean isIncludeGeneMetrics = false;

    public Set<String> popSet = new HashSet<String>(Arrays.asList("global"));
    public float rfTpProbabilitySnv = Data.NO_FILTER;
    public float rfTpProbabilityIndel = Data.NO_FILTER;
    public boolean isFilterPass = false;

    // pop af filter
    public String maxPopAFStr = Data.NO_FILTER_STR;
    public float[] maxPopAFArray;
    private boolean isMaxPopAFValid = true;

    // pop maf filter
    public String maxPopMAFStr = Data.NO_FILTER_STR;
    public float[] maxPopMAFArray;
    private boolean isMaxPopMAFValid = true;

    public boolean isRfTpProbabilityValid(float value, boolean isSnv) {
        if (isSnv) {
            return isRfTpProbabilitySnvValid(value);
        } else {
            return isRfTpProbabilityIndelValid(value);
        }
    }

    private boolean isRfTpProbabilitySnvValid(float value) {
        if (rfTpProbabilitySnv == Data.NO_FILTER) {
            return true;
        }

        return value >= rfTpProbabilitySnv
                || value == Data.FLOAT_NA;
    }

    private boolean isRfTpProbabilityIndelValid(float value) {
        if (rfTpProbabilityIndel == Data.NO_FILTER) {
            return true;
        }

        return value >= rfTpProbabilityIndel
                || value == Data.FLOAT_NA;
    }

    public boolean isFilterPass(String filter) {
        if (!isFilterPass) {
            return true;
        }

        return filter.equals("PASS") || filter.equals(Data.STRING_NA);
    }

    public void initMaxPopAF(String[] pop) {
        maxPopAFArray = new float[pop.length];

        HashMap<String, Integer> popIndexMap = new HashMap<>();
        for (int i = 0; i < pop.length; i++) {
            maxPopAFArray[i] = Data.NO_FILTER;
            popIndexMap.put(pop[i], i);
        }

        for (String popAF : maxPopAFStr.split(",")) { // pop:af,pop:af
            String[] tmp = popAF.split(":"); // pop:af

            Integer index = popIndexMap.get(tmp[0]);
            float af = Float.parseFloat(tmp[1]);
            if (index != null) {
                maxPopAFArray[index] = af;
            }
        }
    }

    public void initMaxPopMAF(String[] pop) {
        maxPopMAFArray = new float[pop.length];

        HashMap<String, Integer> popIndexMap = new HashMap<>();
        for (int i = 0; i < pop.length; i++) {
            maxPopMAFArray[i] = Data.NO_FILTER;
            popIndexMap.put(pop[i], i);
        }

        for (String popAF : maxPopMAFStr.split(",")) { // pop:maf,pop:maf
            String[] tmp = popAF.split(":"); // pop:maf

            Integer index = popIndexMap.get(tmp[0]);
            float maf = Float.parseFloat(tmp[1]);
            if (index != null) {
                maxPopMAFArray[index] = maf;
            }
        }
    }

    public void resetPopAFValid() {
        isMaxPopAFValid = true;
        isMaxPopMAFValid = true;
    }

    public void checkPopAFValid(int index, float value) {
        checkMaxPopAFValid(index, value);
        checkMaxPopMAFValid(index, value);
    }

    private void checkMaxPopAFValid(int index, float value) {
        if (!maxPopAFStr.equals(Data.NO_FILTER_STR)) {
            float maxAF = maxPopAFArray[index];

            if (maxAF != Data.NO_FILTER && isMaxPopAFValid) {
                isMaxPopAFValid &= value <= maxAF || value == Data.FLOAT_NA;
            }
        }
    }

    private void checkMaxPopMAFValid(int index, float value) {
        if (!maxPopMAFStr.equals(Data.NO_FILTER_STR)) {
            float maxMAF = maxPopMAFArray[index];

            if (maxMAF != Data.NO_FILTER && isMaxPopMAFValid) {
                isMaxPopMAFValid &= value <= maxMAF || value >= (1 - maxMAF)
                        || value == Data.FLOAT_NA;
            }
        }
    }

    /*
        1. NA return true
        2. filter not applied return true
        3. when filter applied, pass all return true
     */
    public boolean isPopAFValid() {
        return isMaxPopAFValid()
                && isMaxPopMAFValid();
    }

    private boolean isMaxPopAFValid() {
        if (maxPopAFStr.equals(Data.NO_FILTER_STR)) {
            return true;
        }

        return isMaxPopAFValid;
    }

    private boolean isMaxPopMAFValid() {
        if (maxPopMAFStr.equals(Data.NO_FILTER_STR)) {
            return true;
        }

        return isMaxPopMAFValid;
    }
}

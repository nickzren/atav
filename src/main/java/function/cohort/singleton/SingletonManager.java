package function.cohort.singleton;

import function.cohort.base.Sample;
import function.cohort.base.SampleManager;
import global.Index;
import utils.ErrorManager;
import utils.LogManager;
import java.util.ArrayList;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class SingletonManager {

    public static final String[] COMP_HET_FLAG = {
        "COMPOUND HETEROZYGOTE", // 0
        "POSSIBLY COMPOUND HETEROZYGOTE", // 1
        "NO FLAG"
    };

    static ArrayList<Singleton> singletonList = new ArrayList<>();

    public static void init() {
        initSingletonFromInputSamples();
    }

    private static void initSingletonFromInputSamples() {
        for (Sample sample : SampleManager.getList()) {
            if (sample.isCase()) {
                Singleton singleton = new Singleton(sample);

                singletonList.add(singleton);
            }
        }

        if (singletonList.isEmpty()) {
            ErrorManager.print("Missing singleton from sample file.", ErrorManager.INPUT_PARSING);
        } else {
            LogManager.writeAndPrint("Total singletons: " + singletonList.size());
        }
    }

    public static ArrayList<Singleton> getList() {
        return singletonList;
    }

    public static String getCompHetFlag(byte cGeno1, byte cGeno2) {
        if (cGeno1 == Index.HET && cGeno2 == Index.HET) {
            return COMP_HET_FLAG[1];
        } else {
            return COMP_HET_FLAG[2];
        }
    }

    /*
     * The number of people who have BOTH of the variants divided by the total
     * number of covered people. freq[0] Frequency of Variant #1 & #2
     * (co-occurance) in cases. freq[1] Frequency of Variant #1 & #2
     * (co-occurance) in ctrls
     */
    public static float[] getCoOccurrenceFreq(SingletonOutput output1, SingletonOutput output2) {
        float[] freq = new float[2];

        int quanlifiedCaseCount = 0, qualifiedCtrlCount = 0;
        int totalCaseCount = 0, totalCtrlCount = 0;

        for (Sample sample : SampleManager.getList()) {
            boolean isCoQualifiedGeno = isCoQualifiedGeno(output1, output2, sample.getIndex());

            if (sample.isCase()) {
                totalCaseCount++;
                if (isCoQualifiedGeno) {
                    quanlifiedCaseCount++;
                }
            } else {
                totalCtrlCount++;
                if (isCoQualifiedGeno) {
                    qualifiedCtrlCount++;
                }
            }
        }

        freq[Index.CTRL] = MathManager.devide(qualifiedCtrlCount, totalCtrlCount);
        freq[Index.CASE] = MathManager.devide(quanlifiedCaseCount, totalCaseCount);

        return freq;
    }

    private static boolean isCoQualifiedGeno(SingletonOutput output1,
            SingletonOutput output2, int index) {
        byte geno1 = output1.getCalledVariant().getGT(index);
        byte geno2 = output2.getCalledVariant().getGT(index);

        return output1.isQualifiedGeno(geno1)
                && output2.isQualifiedGeno(geno2);
    }
}

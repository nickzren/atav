package function.genotype.vargeno;

import global.Index;
import function.genotype.base.SampleManager;
import global.Data;

/**
 *
 * @author nick
 */
public class SampleVariantCount {

    private static int[][] genoSnvCount; // ref het hom
    private static int[][] genoIndelCount;

    private static int[] sampleSnvCount; // all
    private static int[] sampleIndelCount;

    public static String getTitle() {
        return "Sample Name,"
                + "Total Sample Variant,"
                + "Total Sample SNV,"
                + "Total Ref SNV,"
                + "Total Het SNV,"
                + "Total Hom SNV,"
                + "Total Sample INDEL,"
                + "Total Ref INDEL,"
                + "Total Het INDEL,"
                + "Total Hom INDEL";
    }

    public static void init() {
        genoSnvCount = new int[5][SampleManager.getListSize()];
        genoIndelCount = new int[5][SampleManager.getListSize()];

        sampleSnvCount = new int[SampleManager.getListSize()];
        sampleIndelCount = new int[SampleManager.getListSize()];
    }

    public static void update(boolean isSnv, int geno, int sampleIndex) {
        if (geno == Data.NA) {
            return;
        }

        if (isSnv) {
            genoSnvCount[geno][sampleIndex]++;
            sampleSnvCount[sampleIndex]++;
        } else {
            genoIndelCount[geno][sampleIndex]++;
            sampleIndelCount[sampleIndex]++;
        }
    }

    private static int getTotalVariant(int sampleIndex) {
        return sampleSnvCount[sampleIndex]
                + sampleIndelCount[sampleIndex];
    }

    public static String getString(int sampleIndex) {
        StringBuilder sb = new StringBuilder();

        sb.append(getTotalVariant(sampleIndex)).append(",");
        sb.append(sampleSnvCount[sampleIndex]).append(",");
        sb.append(genoSnvCount[Index.REF][sampleIndex]).append(",");
        sb.append(genoSnvCount[Index.HET][sampleIndex]).append(",");
        sb.append(genoSnvCount[Index.HOM][sampleIndex]).append(",");
        sb.append(sampleIndelCount[sampleIndex]).append(",");
        sb.append(genoIndelCount[Index.REF][sampleIndex]).append(",");
        sb.append(genoIndelCount[Index.HET][sampleIndex]).append(",");
        sb.append(genoIndelCount[Index.HOM][sampleIndex]);

        return sb.toString();
    }
}

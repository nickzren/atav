package function.genotype.collapsing;

import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CollapsingRegionSummary extends CollapsingSummary {
    
    public static final String title
            = "Rank,"
            + "Region Name,"
            + "Total Variant,"
            + "Total SNV,"
            + "Total Indel,"
            + "Qualified Case,"
            + "Unqualified Case,"
            + "Qualified Case Freq,"
            + "Qualified Ctrl,"
            + "Unqualified Ctrl,"
            + "Qualified Ctrl Freq,"
            + "Enriched Direction,"
            + "Fet P"
            + "\n";

    public CollapsingRegionSummary(String name) {
        super(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("'").append(name).append("'").append(",");
        sb.append(totalVariant).append(",");
        sb.append(totalSnv).append(",");
        sb.append(totalIndel).append(",");
        sb.append(qualifiedCase).append(",");
        sb.append(unqualifiedCase).append(",");
        sb.append(FormatManager.getDouble(qualifiedCaseFreq)).append(",");
        sb.append(qualifiedCtrl).append(",");
        sb.append(unqualifiedCtrl).append(",");
        sb.append(FormatManager.getDouble(qualifiedCtrlFreq)).append(",");
        sb.append(enrichedDirection).append(",");
        sb.append(FormatManager.getDouble(fetP));

        return sb.toString();
    }
}

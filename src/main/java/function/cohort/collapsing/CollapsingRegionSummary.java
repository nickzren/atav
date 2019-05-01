package function.cohort.collapsing;

import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CollapsingRegionSummary extends CollapsingSummary {

    public static String getTitle() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Rank");
        sj.add("Region Name");
        sj.add("Total Variant");
        sj.add("Total SNV");
        sj.add("Total Indel");
        sj.add("Qualified Case");
        sj.add("Unqualified Case");
        sj.add("Qualified Case Freq");
        sj.add("Qualified Ctrl");
        sj.add("Unqualified Ctrl");
        sj.add("Qualified Ctrl Freq");
        sj.add("Enriched Direction");
        sj.add("Fet P");
        
        return sj.toString();
    }

    public CollapsingRegionSummary(String name) {
        super(name);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("'" + name + "'");
        sj.add(FormatManager.getInteger(totalVariant));
        sj.add(FormatManager.getInteger(totalSnv));
        sj.add(FormatManager.getInteger(totalIndel));
        sj.add(FormatManager.getInteger(qualifiedCase));
        sj.add(FormatManager.getInteger(unqualifiedCase));
        sj.add(FormatManager.getFloat(qualifiedCaseFreq));
        sj.add(FormatManager.getInteger(qualifiedCtrl));
        sj.add(FormatManager.getInteger(unqualifiedCtrl));
        sj.add(FormatManager.getFloat(qualifiedCtrlFreq));
        sj.add(enrichedDirection);
        sj.add(FormatManager.getDouble(fetP));

        return sj.toString();
    }
}

package function.cohort.collapsing;

import function.annotation.base.TranscriptManager;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CollapsingTranscriptSummary extends CollapsingSummary {

    private String id;

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Rank");
        sj.add("Transcript Name");
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

    public CollapsingTranscriptSummary(String id) {
        super(TranscriptManager.getId(id));
        
        this.id = id;
    }

    public String getId() {
        return id;
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

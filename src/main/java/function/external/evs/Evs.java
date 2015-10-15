package function.external.evs;

import global.Data;
import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class Evs {

    private String chr;
    private int pos;
    private String ref;
    private String alt;
    private boolean isSnv;

    // from coverage table
    private int eaCoveredSamples;
    private int eaAverageCoverage;
    private int aaCoveredSamples;
    private int aaAverageCoverage;
    private int allCoveredSamples;
    private int allAverageCoverage;

    // from maf table
    private float eaMaf;
    private float aaMaf;
    private float allMaf;
    private String eaGenotypeCount;
    private String aaGenotypeCount;
    private String allGenotypeCount;
    private String filterStatus;

    public Evs(String id) {
        initBasic(id);

        initCoverage();

        initMaf();
    }

    private void initBasic(String id) {
        String[] tmp = id.split("-");
        chr = tmp[0];
        pos = Integer.valueOf(tmp[1]);
        ref = tmp[2];
        alt = tmp[3];

        isSnv = true;

        if (ref.length() > 1
                || alt.length() > 1) {
            isSnv = false;
        }
    }

    private void initCoverage() {
        try {
            String sql = EvsManager.getSql4Cvg(chr, pos);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                eaCoveredSamples = rs.getInt("EASampleCovered");
                eaAverageCoverage = rs.getInt("EAAvgCoverage");
                aaCoveredSamples = rs.getInt("AASampleCovered");
                aaAverageCoverage = rs.getInt("AAAvgCoverage");
                allCoveredSamples = rs.getInt("ALLSampleCovered");
                allAverageCoverage = rs.getInt("AllAvgCoverage");
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private void initMaf() {
        try {
            String sql = EvsManager.getSql4Maf(isSnv, chr, pos, ref, alt);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                eaMaf = rs.getFloat("ea_maf");
                aaMaf = rs.getFloat("aa_maf");
                allMaf = rs.getFloat("all_maf");
                eaGenotypeCount = rs.getString("ea_genotype_count");
                aaGenotypeCount = rs.getString("aa_genotype_count");
                allGenotypeCount = rs.getString("all_genotype_count");
                filterStatus = rs.getString("FilterStatus");
            } else {
                eaMaf = Data.NA;
                if (eaCoveredSamples > 0) {
                    eaMaf = 0;
                }

                aaMaf = Data.NA;
                if (aaCoveredSamples > 0) {
                    aaMaf = 0;
                }

                allMaf = Data.NA;
                if (allCoveredSamples > 0) {
                    allMaf = 0;
                }

                eaGenotypeCount = "NA";
                aaGenotypeCount = "NA";
                allGenotypeCount = "NA";
                filterStatus = "NA";
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private float getMaxMaf() {
        float maf = Data.NA;

        if (EvsCommand.evsPop.contains("ea")
                && eaMaf != Data.NA) {
            maf = Math.max(eaMaf, maf);
        }

        if (EvsCommand.evsPop.contains("aa")
                && aaMaf != Data.NA) {
            maf = Math.max(aaMaf, maf);
        }

        if (EvsCommand.evsPop.contains("all")
                && allMaf != Data.NA) {
            maf = Math.max(allMaf, maf);   
        }
        
        return maf;
    }

    public boolean isValid() {
        if (EvsCommand.isEvsStatusValid(filterStatus)
                && EvsCommand.isEvsAllCoverageValid(allAverageCoverage)
                && EvsCommand.isEvsMafValid(getMaxMaf())) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatManager.getInteger(eaCoveredSamples)).append(",");
        sb.append(FormatManager.getInteger(eaAverageCoverage)).append(",");
        sb.append(FormatManager.getInteger(aaCoveredSamples)).append(",");
        sb.append(FormatManager.getInteger(aaAverageCoverage)).append(",");
        sb.append(FormatManager.getInteger(allCoveredSamples)).append(",");
        sb.append(FormatManager.getInteger(allAverageCoverage)).append(",");
        sb.append(FormatManager.getFloat(eaMaf)).append(",");
        sb.append(eaGenotypeCount).append(",");
        sb.append(FormatManager.getFloat(aaMaf)).append(",");
        sb.append(aaGenotypeCount).append(",");
        sb.append(FormatManager.getFloat(allMaf)).append(",");
        sb.append(allGenotypeCount).append(",");
        sb.append(filterStatus).append(",");

        return sb.toString();
    }
}
